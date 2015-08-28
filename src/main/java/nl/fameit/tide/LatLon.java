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
 * Coordinate in the world as latitude and longitude
 */
public class LatLon {
    private float latitude;
    /**
     * longitude for the tidal constituents in degrees east (positive) or west (negative) from -180 to 180 degrees
     */
    public float lon;
    /**
     * latitude for the tidal constituents in degrees north (positive) or south (negative) from -90 to 90 degrees
     */
    public float lat;

    /**
     * Initialize latitude and longitude.
     *
     * @param lat latitude in degrees north (positive) or south (negative) from -90 to 90 degrees
     * @param lon longitude in degrees east (positive) or west (negative) from -180 to 180 degrees
     */
    public LatLon(float lat, float lon) {
        this.lon = lon;
        this.lat = lat;
    }

    /**
     * Initialize latitude and longitude from another {@link LatLon}.
     *
     * @param latlon {@link LatLon} to copy teh cooridnates from
     */
    public LatLon(LatLon latlon) {
        this.lat = latlon.lat;
        this.lon = latlon.lon;
    }

    /**
     * Wrap around the coordinates so they fit in the range +/-90 degrees (for latitude) and +/-180 degrees (for longitude).
     *
     * @param latEdge
     * @param lonEdge
     */
    public void wrap(float latEdge, float lonEdge) {
        lat = (lat - latEdge) % 180;
        if (lat < 0) lat += 180f;
        lat += latEdge;

        lon = (lon - lonEdge) % 360f;
        if (lon < 0) lon += 360f;
        lon += lonEdge;
    }

    /**
     * Degrees to radians.
     *
     * @param deg angle in degrees
     * @return angle in radians
     */
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /**
     * Calculate the distance in meters between two coordinates
     * @param rhs   second coordinate
     * @return distance in meters
     */
    public double distance(LatLon rhs) {

        final int R = 6371; // Radius of the earth

        double latDistance = deg2rad(rhs.lat - lat);
        double lonDistance = deg2rad(rhs.lon - lon);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(deg2rad(lat)) * Math.cos(deg2rad(rhs.lat)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000; // convert to meters

    }

    public boolean equals(LatLon rhs) {
        return lat == rhs.lat && lon == rhs.lon;
    }


}
