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


/**
 * Describes an Extreme (high tide or low tide)
 */
public class Extreme implements Comparable<Extreme> {
    /**
     * time at which the extreme occurs in milliseconds since the unix epoch (midnight, January 1, 1970 UTC)
     */
    public final long time;
    /**
     * the height of the extreme in meters
     */
    public final double height;
    /**
     * true if it is a maximum (high tide), false when it is a minimum (low tide)
     */
    public final boolean maximum;
    /**
     * the difference between the last time step and previous to last time step in milliseconds; this is a measure for the error in the result
     */
    public final long error;
    /**
     * the number of iteration steps used to calculate the extreme
     */
    public final int steps;

    public Extreme(final long time, final double height, final boolean maximum, final long error, final int steps) {
        this.time = time;
        this.height = height;
        this.maximum = maximum;
        this.error = error;
        this.steps = steps;
    }

    /**
     * compares {@link Extreme} objects based on their time
     */
    @Override
    public int compareTo(final Extreme rhs) {
        return Long.valueOf(time).compareTo(rhs.time);
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof Extreme && compareTo((Extreme) o) == 0;
    }

    @Override
    public String toString() {
        return String.format("%s: %tc %+.2f (%d %d)", this.maximum ? "High" : "Low ", this.time*1000, this.height, this.error, this.steps);
    }
}
