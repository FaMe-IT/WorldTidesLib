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
 * Complex number with floats for the {@link re} (real) and {@link im} (imaginary) parts.
 */
public class ComplexFloat {
    /**
     * Real part of the complex number
     */
    public float re;
    /**
     * Imaginary part of the complex number
     */
    public float im;

    /**
     * Default ocnstructor.
     */
    public ComplexFloat() {
    }

    /**
     * Constructor to initialize real and imaginary parts.
     *
     * @param re Real part of the complex number
     * @param im Imaginary part of the complex number
     */
    public ComplexFloat(float re, float im) {
        this.re = re;
        this.im = im;
    }

    /**
     * Check if the complex number is zero.
     *
     * @return true if 0, otherwise false
     */
    public boolean isZero() {
        return re == 0f && im == 0f;
    }

    /**
     * Create a string representation of the complex number.
     *
     * @return string representation
     */
    @Override
    public String toString() {

        return String.format("%.3f%+.3fi", re, im);
    }

    /**
     * Get the amplitude of the complex number.
     *
     * @return amplitude
     */
    public double getAmplitude() {
        return Math.sqrt(re * re + im * im);
    }

    /**
     * Get the phase of the complex number. It will be in the range (0..2*Pi).
     *
     * @return phase
     */
    public double getPhase() {
        double phase = Math.atan2(-im, re);
        if (phase < 0) phase += 2 * Math.PI;
        return phase;
    }
}
