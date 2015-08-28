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

/**
 * Contains the coordinates and values for the tidal constituents.
 */

public class Constituents {
// Also a Database entity => be carefull with changes

    /**
     * latitude for the tidal constituents in degrees north (positive) or south (negative) from -90 to 90 degrees
     */
    private float latitude;
    /**
     * longitude for the tidal constituents in degrees east (positive) or west (negative) from -180 to 180 degrees
     */
    private float longitude;
    /**
     * array of values for the tidal constituents
     */
    private ComplexFloat[] values;

    private Constituents() {

    }

    /**
     * Initializes coordinate and constituent array size (but not values).
     *
     * @param coordinate     coordinate
     * @param nrConstituents the number of tidal constituents
     */
    public Constituents(LatLon coordinate, int nrConstituents) {
        this.values = new ComplexFloat[nrConstituents];
        this.latitude = coordinate.lat;
        this.longitude = coordinate.lon;
    }

    /**
     * Check if the constituents are zero (not properly initialized).
     *
     * @param constituents constituents to check
     * @return true if the constituents are zero, false if they are properly initialized
     */
    public static boolean isZero(Constituents constituents) {
        return constituents == null || constituents.get(0) == null || constituents.get(0).isZero();
    }

    /**
     * Set a single constituent value.
     *
     * @param constituent index of the constituent to be set
     * @param value       new value for the constituent
     */
    public void set(int constituent, ComplexFloat value) {
        values[constituent] = value;
    }

    /**
     * Get a single constituent value.
     *
     * @param constituent index of the constituent to get
     * @return constituent value
     */
    public ComplexFloat get(int constituent) {
        return values[constituent];
    }

    /**
     * Copy the constituents from the rhs to this object.
     *
     * @param rhs {@link Constituents} to copy the values from
     */
    public void copyConstituents(Constituents rhs) {
        for (int i = 0; i < rhs.values.length; i++) {
            this.values[i] = rhs.values[i];
        }
    }

    /**
     * Get all values as an array.
     *
     * @return constituent values
     */
    public ComplexFloat[] getValues() {
        return values;
    }

    /**
     * Calculate the distance between the coordinates of two {@link Constituents}.
     *
     * @param rhs other object to calculate the distance to
     * @return distance in meters
     */
    public double distance(Constituents rhs) {
        return getLatLon().distance(rhs.getLatLon());
    }

    /**
     * Get the coordinate.
     *
     * @return coordinate
     */
    public LatLon getLatLon() {
        return new LatLon(latitude, longitude);
    }
}
