/*
   Copyright © 2014-2015 FaMe IT, The Netherlands

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

package nl.fameit.tide;

import java.util.EnumMap;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * Calculates the tide and tidal extremes (high and low tides) from the tidal constituents.
 */
public class TideCalculator {

    Constituents constituents;
    TidalConstant[] tidalConstants;
    EnumMap<TidalConstant, NodalCorrection> nodalCorrections;

    /**
     * Default constructor
     *
     * @param constituents   values for the tidal constituents
     * @param tidalConstants tidal constants (must be in the same order as the constituents)
     */
    public TideCalculator(final Constituents constituents, final TidalConstant[] tidalConstants) {
        this.constituents = constituents;
        this.tidalConstants = tidalConstants;
        this.nodalCorrections = null;
    }

    /**
     * Calculate the current tide (height). Optionally updates the nodal corrections.
     *
     * @param timeInMillis time for which to calculate the tide in milliseconds since the java epoch (midnight, January 1, 1970 UTC)
     * @param updateNodal  if true the nodal (long term) correction will be updated
     * @return height (in meters)
     */
    public double calculateTide(long timeInMillis, boolean updateNodal) {

        if (updateNodal == true || nodalCorrections == null)
            nodalCorrections = NodalCorrection.getNodalCorrections(timeInMillis);

        double height = 0;
        double T = (timeInMillis - TidalConstant.epoch) / 1000; // T is time in seconds since January 1st, 1992

        for (int i = 0; i < constituents.getValues().length; i++) {
            ComplexFloat constituent = constituents.get(i);
            TidalConstant k = tidalConstants[i];
            NodalCorrection nodalCorrection = nodalCorrections.get(k);

            double angle = T * k.omega + k.phase + nodalCorrection.u;
            height += nodalCorrection.f * (constituent.re * Math.cos(angle) - constituents.get(i).im * Math.sin(angle));
        }
        return height;
    }

    /**
     * Calculate the tide (height), its first and second derivative
     * The nodal constants will not be updated.
     *
     * @param timeInMillis time for which to calculate the tide and its derivatives in milliseconds since the java epoch (midnight, January 1, 1970 UTC)
     * @return array, with the tide [0] (in meters), its first derivative [1] (in meters/millisecond) and second derivative [2] (in meters/millisecond^2)
     */
    private double[] calculateTideDerivates(long timeInMillis) {
        double height = 0;
        double dheight = 0;
        double d2height = 0;
        double T = ((double) timeInMillis - TidalConstant.epoch) / 1000.0; // T is time in seconds since January 1st, 1992

        for (int i = 0; i < constituents.getValues().length; i++) {
            ComplexFloat constituent = constituents.get(i);
            TidalConstant k = tidalConstants[i];
            NodalCorrection nodalCorrection = nodalCorrections.get(k);

            double angle = T * k.omega + k.phase + nodalCorrection.u;
            double cosa = Math.cos(angle);
            double sina = Math.sin(angle);

            height += nodalCorrection.f * (constituent.re * cosa - constituents.get(i).im * sina);
            dheight += nodalCorrection.f * k.omega / 1000 * (-constituent.re * sina - constituents.get(i).im * cosa);
            d2height += nodalCorrection.f * k.omega * k.omega / 1000 / 1000 * (-constituent.re * Math.cos(angle) + constituents.get(i).im * Math.sin(angle));
        }

        return new double[]{height, dheight, d2height};
    }

    /**
     * Describes an Extreme (high tide or low tide)
     */
    static public class Extreme implements Comparable<Extreme> {
        /**
         * time at which the extreme occurs in milliseconds since the unix epoch (midnight, January 1, 1970 UTC)
         */
        public long timeInMillis;
        /**
         * the height of the extreme in meters
         */
        public double height;
        /**
         * true if it is a maximum (high tide), false when it is a minimum (low tide)
         */
        public boolean maximum;
        /**
         * the difference between the last time step and previous to last time step in milliseconds; this is a measure for the error in the result
         */
        public long errorInMillis;
        /**
         * the number of iteration steps used to calculate the extreme
         */
        public int steps;

        private Extreme(final long timeInMillis, final double height, final boolean maximum, final long errorInMillis, final int steps) {
            this.timeInMillis = timeInMillis;
            this.height = height;
            this.maximum = maximum;
            this.errorInMillis = errorInMillis;
            this.steps = steps;
        }

        /**
         * compares {@link Extreme} objects based on their timeInMillis
         */
        @Override
        public int compareTo(final Extreme rhs) {
            return Long.valueOf(timeInMillis).compareTo(rhs.timeInMillis);
        }
    }

    /**
     * Calculates an extreme (high tide or low tide) in the time interval timeInMillis - timeInMillis+intervalInMillis.
     * If no extreme is present returns null.
     * If multiple extremes are present returns an arbitrary one.
     * The method uses a Newton-Rapson Approximation; in case the Newton-Rapson exceeds the time interval it switch to bisecting the interval
     *
     * @param timeInMillis     start time of the interval in milliseconds since the java epoch (midnight, January 1, 1970 UTC)
     * @param intervalInMillis length of the time interval in milliseconds
     * @param maxErrorInMillis the maximum difference between two approximations before returning (may be 0)
     * @param maxsteps         the maximum number of approximations before returning
     * @return null in case no extreme is found, otherwise an Extreme
     */
    private Extreme calculateExtreme(final long timeInMillis, final long intervalInMillis, long maxErrorInMillis, int maxsteps) {

        long a = timeInMillis;
        long b = timeInMillis + intervalInMillis;

        double[] fa = calculateTideDerivates(a);
        if (fa[1] == 0)
            return new Extreme(a, fa[0], fa[2] < 0, 0, 0);

        double[] fb = calculateTideDerivates(b);
        if (fb[1] == 0)
            return new Extreme(b, fb[0], fb[2] < 0, 0, 0);

        if (fa[1] * fb[1] > 0)
            return null;

        long x;
        long xn = (a + b) / 2;

        double[] fx;

        int steps = 0;
        do {
            x = xn;
            fx = calculateTideDerivates(x);

            // Is the root (sign change in the derivate) in the interval a...x or in x...b
            if (fa[1] * fx[1] < 0) {
                b = x;
                fb = fx.clone();
            } else {
                a = x;
                fa = fx.clone();
            }

            xn = x - (long) (fx[1] / fx[2]);

            // If xn outside a-b then revert to bisecting
            if (xn < a || xn > b) {
                xn = (a + b) / 2;
            }

            steps++;
        } while (Math.abs(xn - x) > maxErrorInMillis && steps < maxsteps);

        fx = calculateTideDerivates(xn);
        return new Extreme(xn, fx[0], fx[2] < 0, xn - x, steps);
    }

    /**
     * Calculates all extremes (high and low tides) in the interval timeInMillis...timeInMillis+intervalInMillis.
     * The nodal corrections are only calculated once to speed up calculations.
     *
     * @param timeInMillis     start time of the interval for which to calculate the extremes in milliseconds since the java epoch (midnight, January 1, 1970 UTC)
     * @param intervalInMillis length of the interval for which to calculate the extremes
     * @return extremes in the specified interval as {@link Extreme} objects
     */
    public Extreme[] calculateExtremes(long timeInMillis, long intervalInMillis) {
        nodalCorrections = NodalCorrection.getNodalCorrections(timeInMillis);

        long step = 3 * 3600000L;
        SortedSet<Extreme> extremes = new TreeSet<>();

        for (long time = timeInMillis; time < timeInMillis + intervalInMillis; time += step) {
            Extreme extreme = calculateExtreme(time, Math.min(step, (timeInMillis + intervalInMillis) - time), 0, 10);
            if (extreme != null) {
                extremes.add(extreme);
            }
        }
        return extremes.toArray(new Extreme[extremes.size()]);
    }

}
