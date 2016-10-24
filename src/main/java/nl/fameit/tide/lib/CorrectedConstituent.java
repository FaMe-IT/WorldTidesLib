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
 * Constituent value corrected for nodal influences
 */
public class CorrectedConstituent {
    /**
     * speed/frequency of the constituent (in radians/second)
     */
    public final double speed;
    /**
     * phase of the constituent (in radians)
     */
    public final double phase;
    /**
     * amplitude of the constituent (in meters)
     */
    public final double amplitude;

    /**
     * constructor to initialize the corrected constituent
     *
     * @param speed     speed/frequency in radians/second
     * @param phase     phase in radians
     * @param amplitude amplitude in meters
     */
    public CorrectedConstituent(final double speed, final double phase, double amplitude) {
        this.speed = speed;
        this.phase = phase;
        this.amplitude = amplitude;
    }

}
