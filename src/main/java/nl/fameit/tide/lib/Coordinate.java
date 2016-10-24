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

package nl.fameit.tide.lib;

/**
 * Coordinate in the world as latitude and longitude
 */
public class Coordinate {
    public final double longitude;
    public final double latitude;

    /**
     * Initialize latitude and longitude.
     */
    public Coordinate() {
        latitude = 0;
        longitude = 0;
    }

    /**
     * Initialize latitude and longitude.
     *
     * @param latitude  latitude in degrees north (positive) or south (negative) from -90 to 90 degrees
     * @param longitude longitude in degrees east (positive) or west (negative) from -180 to 180 degrees
     */
    public Coordinate(final double latitude, final double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Initialize latitude and longitude from another {@link Coordinate}.
     *
     * @param coordinate {@link Coordinate} to copy the coordinates from
     */
    public Coordinate(final Coordinate coordinate) {
        this.latitude = coordinate.latitude;
        this.longitude = coordinate.longitude;
    }
}
