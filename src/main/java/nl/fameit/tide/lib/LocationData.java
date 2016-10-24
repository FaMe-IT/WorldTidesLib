/*
   Copyright © 2016 FaMe IT, The Netherlands

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package nl.fameit.tide.lib;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Constituent and datum information for a location plus the various calculation routines, which use this data
 */
public class LocationData implements Serializable {
    /**
     * name of the tide station or null or "" when not a tide station
     */
    public final String name;
    /**
     * the copyright message associated with this data
     */
    public final String copyright;
    /**
     * the coordinate of the data
     */
    public final Coordinate coordinate;
    /**
     * the start of the interval for which the corrected constituents are valid
     */
    public final long epochStart;
    /**
     * the end of the interval for which the corrected constituents are valid
     */
    public final long epochEnd;
    /**
     * map of constituents which have already had their nodal corrections applied
     */
    public final LinkedHashMap<String, CorrectedConstituent> constituents;
    /**
     * map of vertical datums
     */
    public final LinkedHashMap<String, Double> datums;

    /**
     * @param json JSON object containing all location data
     */
    public LocationData(JSONObject json) {
        this.name = json.optString("station");
        this.copyright = json.getString("copyright");
        this.coordinate = new Coordinate(json.getDouble("responseLat"), json.getDouble("responseLon"));
        this.epochStart = json.getLong("start_dt");
        this.epochEnd = json.getLong("end_dt");

        constituents = new LinkedHashMap<>();
        final JSONArray jsonConstituents = json.getJSONArray("correctedConstituents");
        for (int i = 0; i < jsonConstituents.length(); i++) {
            final JSONObject jsonConstituent = jsonConstituents.getJSONObject(i);
            final CorrectedConstituent constituent = new CorrectedConstituent(jsonConstituent.getDouble("speed") / 180 * Math.PI / 3600, jsonConstituent.getDouble("phase") / 180 * Math.PI, jsonConstituent.getDouble("amplitude"));
            constituents.put(jsonConstituent.getString("name"), constituent);
        }

        datums = new LinkedHashMap<>();
        final JSONArray jsonDatums = json.optJSONArray("datums");
        if (jsonDatums != null) {
            for (int i = 0; i < jsonDatums.length(); i++) {
                final JSONObject jsonDatum = jsonDatums.getJSONObject(i);
                datums.put(jsonDatum.getString("name"), jsonDatum.getDouble("height"));
            }
        }
    }

    /**
     * calculate height for a single time
     *
     * @param T in seconds
     * @return height in meters
     */
    private double calculateSingle(final long T) {
        double res = 0.0F;

        for (final CorrectedConstituent constituent : constituents.values()) {
            res += constituent.amplitude * Math.cos((T - epochStart) * constituent.speed + constituent.phase);
        }
        return res;
    }

    /**
     * calculate height and first and second order derivatives for a single item
     *
     * @param T in seconds
     * @return height, derivative of height and second derivative of height in meters
     */
    private double[] calculateDerivates(final long T) {
        final double[] res = {0.0F, 0.0F, 0.0F};
        for (final CorrectedConstituent constituent : constituents.values()) {

            final double phase = (T - epochStart) * constituent.speed + constituent.phase;
            final double cos = Math.cos(phase);
            final double sin = Math.sin(phase);

            double amp = constituent.amplitude;
            res[0] += amp * cos;
            amp *= -constituent.speed;
            res[1] += amp * sin;
            amp *= constituent.speed;
            res[2] += amp * cos;
        }
        return res;
    }

    /**
     * calculate tides (heights) for an interval with a single nodal correction
     *
     * @param start start time of the interval for which to calculate the heights in seconds since the java epochStart (midnight, January 1, 1970 UTC)
     * @param end   end time of the interval for which to calculate the heights in seconds since the java epochStart (midnight, January 1, 1970 UTC)
     * @param step  step size in seconds for which to calculate the heights
     * @param datum datum to add to all values (can be null)
     * @return hashmap of time, height pairs
     */
    public LinkedHashMap<Long, Double> calculate(final long start, final long end, final long step, String datum) {
        if (start < epochStart || end > epochEnd)
            throw new IllegalArgumentException("start or end are outside valid range.");
        if (datum != null && !datums.containsKey(datum))
            throw new IllegalArgumentException("datum does not exist.");

        // the returned datums are with respect to the requested datum, while corrected constituents always refer to MSL
        double offset = datums.getOrDefault(datum, 0.0) - datums.getOrDefault("MSL", 0.0);

        final LinkedHashMap<Long, Double> res = new LinkedHashMap<>((int) ((end - start) / step + 1L));
        for (long time = start; time <= end; time += step) {
            res.put(time, calculateSingle(time) - offset);
        }
        return res;
    }


    /**
     * Calculates an extreme (high tide or low tide) in the time interval time - time+intervalInMillis.
     * If no extreme is present returns null.
     * If multiple extremes are present returns an arbitrary one.
     * The method uses a Newton-Rapson Approximation; in case the Newton-Rapson exceeds the time interval it switch to bisecting the interval
     *
     * @param time     start time of the interval in seconds since the java epochStart (midnight, January 1, 1970 UTC)
     * @param length   length of the time interval in seconds
     * @param maxError the maximum difference between two approximations before returning (may be 0)
     * @param maxsteps the maximum number of approximations before returning
     * @param offset   the vertical offset to add to the extreme
     * @return null in case no extreme is found, otherwise an Extreme
     */
    private Extreme calculateExtreme(final long time, final long length, final long maxError, final int maxsteps, double offset) {

        long a = time;
        long b = time + length;

        double[] fa = calculateDerivates(a);
        // lower side of interval is the extreme
        if (fa[1] == 0.0F) {
            return new Extreme(a, fa[0] - offset, fa[2] < 0.0F, 0L, 0);
        }

        final double[] fb = calculateDerivates(b);
        // upper side of interval is the extreme
        if (fb[1] == 0.0F) {
            return new Extreme(b, fb[0] - offset, fb[2] < 0.0F, 0L, 0);
        }

        // error: lower and upper side both have a positive or a negative slope: so no extreme in between (assuming no local peak)
        if (fa[1] * fb[1] > 0.0F) {
            return null;
        }

        long x;
        long xn = (a + b) / 2L;
        double[] fx;

        int steps = 0;
        do {
            x = xn;
            fx = calculateDerivates(x);

            // Is the root (sign change in the derivate) in the interval a...x or in x...b
            if (fa[1] * fx[1] < 0.0F) {
                b = x;
                // fb = fx.clone();
            } else {
                a = x;
                fa = fx.clone();
            }

            xn = x - (long) (fx[1] / fx[2]);

            // If xn outside a-b then revert to bisecting
            if (xn < a || xn > b) {
                xn = (a + b) / 2L;
            }

            steps++;
        } while (Math.abs(xn - x) > maxError && steps < maxsteps);

        fx = calculateDerivates(xn);
        return new Extreme(xn, fx[0] - offset, fx[2] < 0.0F, xn - x, steps);
    }

    /**
     * Calculates all extremes (high and low tides) in the interval time...time+intervalInMillis.
     * The nodal corrections are only calculated once to speed up calculations.
     *
     * @param start start time of the interval for which to calculate the extremes in seconds since the java epochStart (midnight, January 1, 1970 UTC)
     * @param end   end time of the interval for which to calculate the extremes in seconds since the java epochStart (midnight, January 1, 1970 UTC)
     * @param datum datum to add to all values (can be null)
     * @return extremes in the specified interval as {@link Extreme} objects
     */
    public List<Extreme> calculateExtremes(final long start, final long end, String datum) {
        if (start < epochStart || end > epochEnd)
            throw new IllegalArgumentException("start or end are outside valid range.");

        if (datum != null && !datums.containsKey(datum))
            throw new IllegalArgumentException("datum " + datum + "does not exist.");

        // the returned datums are with respect to the requested datum, while corrected constituents always refer to MSL
        double offset = datums.getOrDefault(datum, 0.0) - datums.getOrDefault("MSL", 0.0);

        final long lunar_hour = 3726; // 1 lunar hour in seconds
        final Set<Extreme> extremes = new TreeSet<>();

        for (long time = start; time < end; time += lunar_hour) {
            final Extreme extreme = calculateExtreme(time, Math.min(lunar_hour, end - time), 0L, 10, offset);
            if (extreme != null) {
                extremes.add(extreme);
            }
        }

        // remove false extremes by selecting maximum extreme in 6 hour interval
        Extreme[] extremearray = extremes.toArray(new Extreme[extremes.size()]);

        List<Extreme> res = new ArrayList<>();

        for (int i = 0; i < extremearray.length; i++) {
            boolean include = true;
            for (int j = i + 1; j < extremearray.length && extremearray[i].time + 3 * lunar_hour * 2 > extremearray[j].time; j++) {
                if ((extremearray[i].maximum && extremearray[i].height < extremearray[j].height) ||
                        (!extremearray[i].maximum && extremearray[i].height > extremearray[j].height)) {
                    include = false;
                }
            }

            for (int j = i - 1; j >= 0 && extremearray[i].time - 3 * lunar_hour * 2 < extremearray[j].time; j--) {
                if ((extremearray[i].maximum && extremearray[i].height < extremearray[j].height) ||
                        (!extremearray[i].maximum && extremearray[i].height > extremearray[j].height)) {
                    include = false;
                }
            }

            if (include)
                res.add(extremearray[i]);
        }

        return res;
    }

    /**
     * Return the string representation of {@link LocationData} (coordinate and optionally tide station name)
     *
     * @return string representation of the location
     */
    public String toString() {
        if (name == null || name.isEmpty())
            return String.format("%6.3f, %6.3f", coordinate.latitude, coordinate.longitude);
        else
            return String.format("%6.3f, %6.3f (%s)", coordinate.latitude, coordinate.longitude, name);
    }
}
