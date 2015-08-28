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
 * Tidal constants in radians/second for the frequency and in radians for the phase with JAN 1, 1992, 00:00 UTC as datum.
 * <P>
 * References:<P>
 * ftp://ftp.flaterco.com/xtide/NAVO.xls<P>
 * ftp://ftp.flaterco.com/xtide/Constituents-2006.pdf<P>
 * RODNEY'S CONSTITUENT.H, 2/23/96<P>
 * RICHARD RAY'S  "ARGUMENTS" AND "ASTROL", FOR JAN 1, 1992, 00:00 GREENWICH TIME CORRECTED JULY 12, 2000
 */
public enum TidalConstant {

    // order must not be changed as teh database works with fixed indexes into this array
    C_m2(1.405189025757300E-4, 1.73155754567656E0),
    C_s2(1.454441043328608E-4, 0.00000000000000E0),
    C_k1(7.292115854682399E-5, 0.173003673872453E0),
    C_o1(6.759774402890599E-5, 1.55855387180411E0),
    C_n2(1.378796995659399E-4, 6.05072124295143E0),
    C_p1(7.252294578603680E-5, 6.11018163330713E0),
    C_k2(1.458423170936480E-4, 3.48760000133470E0),
    C_q1(6.495854101911592E-5, 5.87771756907898E0),
    C_2n2(1.352404965561499E-4, 4.08669963304672E0),
    C_mu2(1.355937008185992E-4, 3.46311509135312E0),
    C_nu2(1.382329038283892E-4, 5.42713670125784E0),
    C_l2(1.431581055855200E-4, 0.553986501991483E0),
    C_t2(1.452450074605617E-4, 5.284193133561309E-2),
    C_j1(7.556036155661405E-5, 2.13702528377717E0),
    C_m1(7.028195553703394E-5, 2.43657509963318E0),
    C_oo1(7.824457306474201E-5, 1.92904612953059E0),
    C_rho1(6.531174528156522E-5, 5.25413302738539E0),
    C_mf(5.323414517918014E-6, 1.75604245565814E0),
    C_mm(2.639203009790057E-6, 1.96402160990471E0),
    C_ssa(3.982127607872015E-7, 3.48760000133470E0),
    C_m4(2.810378051514600E-4, 3.46311509135312E0),
    C_ms4(2.859630069085908E-4, 1.73155754567656E0),
    C_mn4(2.783986021416699E-4, 1.49909348144841E0),
    C_m6(4.215567077271900E-4, 5.19467263702969E0),
    C_m8(5.620756103029200E-4, 0.643044875526663E0),
    C_mk3(2.134400611225540E-4, 1.90456121954902E0),
    C_s6(4.363323129985823E-4, 0.00000000000000E0),
    C_2sm2(1.503693060899916E-4, 4.55162776150302E0),
    C_2mk3(2.081166466046360E-4, 3.29011141748067E0);

    /**
     * The datum/epoch for the tidal constants (JAN 1, 1992, 00:00 UTC) in milliseconds since the java epoch (midnight, January 1, 1970 UTC)
     */
    public final static long epoch = 694224000000L;

    /**
     * the frequency in radians/second
     */
    public final double omega;
    /**
     * the phase in radians
     */
    public final double phase;

    /**
     * Initialize the frequency and phase.
     *
     * @param omega frequency in radians/second
     * @param phase phase in radians
     */
    private TidalConstant(double omega, double phase) {
        this.omega = omega;
        this.phase = phase;
    }

    /**
     * Get a tidal constant by name.
     *
     * @param name name of teh tidal constant
     * @return tidal constant
     */
    public static TidalConstant valueFromName(String name) {
        return valueOf("C_" + name);
    }

    /**
     * Get the name of the tidal constant.
     *
     * @return name of the tidal constant
     */
    @Override
    public String toString() {
        return this.name().substring(2);
    }

}