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

/**
 * Nodal (long term) correction for tidal constants.
 */
public class NodalCorrection {
    /**
     * nodal correction frequency
     */
    public double f;
    /**
     * nodal correction phase
     */
    public double u;

    NodalCorrection(double t1, double t2) {
        f = Math.sqrt(t1 * t1 + t2 * t2);
        u = Math.atan(t1 / t2);
    }

    NodalCorrection(NodalCorrection nodalCorrection, int fac) {
        f = Math.pow(nodalCorrection.f, fac);
        u = fac * u;
    }

    NodalCorrection(NodalCorrection nodalCorrection1, NodalCorrection nodalCorrection2) {
        f = nodalCorrection1.f * nodalCorrection2.f;
        u = nodalCorrection1.u + nodalCorrection2.u;
    }

    /**
     * Square (multiple by each self).
     *
     * @param val value to square
     * @return squared value (=val*val)
     */
    private double sqr(double val) {
        return val * val;
    }

    /**
     * Calculate nodal (long term) corrections for tidal constants at a certain time.
     *
     * @param timeInMillis time to calculate corrections at in milliseconds since the java epoch (midnight, January 1, 1970 UTC)
     * @return array of nodal corrections for all tidal constants
     */
    public static EnumMap<TidalConstant, NodalCorrection> getNodalCorrections(long timeInMillis) {
        // Datum/Epoch for the nodal corrections (midnight January 1st, 2000 UTC)
        final long epoch = 946684800000L;

        EnumMap<TidalConstant, NodalCorrection> nodalCorrections = new EnumMap<TidalConstant, NodalCorrection>(TidalConstant.class);
        double N; // in rad: mean longitude of lunar node
        double P; // in rad: mean longitude of lunar perigee

        //Compute time argument in centuries relative to J2000
        double Tc = (timeInMillis - epoch) / (100 * 365.25 * 24 * 3600 * 1000);

        N = ((2.22222 - 6 * Tc + 2.0708 - 3) * Tc - 1934.136261) * Tc + 125.04452;
        P = ((-1.249172 - 5 * Tc - 1.032 - 2) * Tc + 4069.0137287) * Tc + 83.3532465;

        if (N < 0)
            N += 360;
        N *= Math.PI / 180;

        if (P < 0)
            P += 360;
        P *= Math.PI / 180;

        double sinn = Math.sin(N);
        double cosn = Math.cos(N);
        double sin2n = Math.sin(2 * N);
        double cos2n = Math.cos(2 * N);
        double sin3n = Math.sin(3 * N);

        double sinp = Math.sin(P);
        double cosp = Math.cos(P);
        double sin2p = Math.sin(2 * P);
        double cos2p = Math.cos(2 * P);

        double sinpn = Math.sin(P - N);
        double cospn = Math.cos(P - N);
        double sin2pn = Math.sin(2 * P - N);
        double cos2pn = Math.cos(2 * P - N);

        // nodal correction terms (f and u are calculated from these)
        nodalCorrections.put(TidalConstant.C_m2, new NodalCorrection(-0.03731 * sinn + 0.00052 * sin2n, 1.0 - 0.03731 * cosn + 0.00052 * cos2n));
        nodalCorrections.put(TidalConstant.C_s2, new NodalCorrection(0, 1));
        nodalCorrections.put(TidalConstant.C_k1, new NodalCorrection(-0.1554 * sinn + 0.0031 * sin2n, 1.0 + 0.1158 * cosn - 0.0028 * cos2n));
        nodalCorrections.put(TidalConstant.C_o1, new NodalCorrection(0.1886 * sinn - 0.0058 * sin2n - 0.0065 * sin2n, 1.0 + 0.1886 * cosn - 0.0058 * cos2n - 0.0065 * cos2p));
        nodalCorrections.put(TidalConstant.C_n2, nodalCorrections.get(TidalConstant.C_m2));
        nodalCorrections.put(TidalConstant.C_p1, new NodalCorrection(-0.0112 * sinn, 1.0 - 0.0112 * cosn));
        nodalCorrections.put(TidalConstant.C_k2, new NodalCorrection(-0.3108 * sinn - 0.0324 * sin2n, 1.0 + 0.2853 * cosn + 0.0324 * cos2n));
        nodalCorrections.put(TidalConstant.C_q1, new NodalCorrection(0.1886 * sinn, 1.0 + 0.1886 * cosn));
        nodalCorrections.put(TidalConstant.C_2n2, nodalCorrections.get(TidalConstant.C_m2));
        nodalCorrections.put(TidalConstant.C_mu2, nodalCorrections.get(TidalConstant.C_m2));
        nodalCorrections.put(TidalConstant.C_nu2, nodalCorrections.get(TidalConstant.C_m2));
        nodalCorrections.put(TidalConstant.C_l2, new NodalCorrection(-0.250 * sin2p - 0.110 * sin2pn, 1.0 - 0.250 * cos2p - 0.110 * cos2pn - 0.037 * cosn));
        nodalCorrections.put(TidalConstant.C_t2, new NodalCorrection(0, 1));
        nodalCorrections.put(TidalConstant.C_j1, new NodalCorrection(-0.227 * sinn, 1.0 + 0.169 * cosn));
        nodalCorrections.put(TidalConstant.C_m1, new NodalCorrection(-0.2294 * sinn - 0.3594 * sin2p - 0.0664 * sin2pn, 1.0 + 0.1722 * cosn + 0.3594 * cos2p + 0.0664 * cos2pn)); //! This assumes m1 argument includes p.
        nodalCorrections.put(TidalConstant.C_oo1, new NodalCorrection(-0.640 * sinn - 0.134 * sin2n - 0.150 * sin2p, 1.0 + 0.640 * cosn + 0.134 * cos2n + 0.150 * cos2p));
        nodalCorrections.put(TidalConstant.C_rho1, nodalCorrections.get(TidalConstant.C_q1));
        nodalCorrections.put(TidalConstant.C_mf, new NodalCorrection(-0.04324 * sin2p - 0.41465 * sinn - 0.03873 * sin2n, 1.0 + 0.04324 * cos2p + 0.41465 * cosn + 0.03873 * cos2n));
        nodalCorrections.put(TidalConstant.C_mm, new NodalCorrection(-.0534 * sin2p - .0219 * sin2pn, 1.0 - .1308 * cosn - .0534 * cos2p - .0219 * cos2pn));
        nodalCorrections.put(TidalConstant.C_ssa, new NodalCorrection(0, 1));
        nodalCorrections.put(TidalConstant.C_m4, new NodalCorrection(nodalCorrections.get(TidalConstant.C_m2), 2));
        nodalCorrections.put(TidalConstant.C_ms4, new NodalCorrection(-0.03731 * sinn + 0.00052 * sin2n, 1.0 - 0.03731 * cosn + 0.00052 * cos2n));
        nodalCorrections.put(TidalConstant.C_mn4, nodalCorrections.get(TidalConstant.C_m4));
        nodalCorrections.put(TidalConstant.C_m6, new NodalCorrection(nodalCorrections.get(TidalConstant.C_m2), 3));
        nodalCorrections.put(TidalConstant.C_m8, new NodalCorrection(nodalCorrections.get(TidalConstant.C_m2), 4));
        nodalCorrections.put(TidalConstant.C_mk3, new NodalCorrection(nodalCorrections.get(TidalConstant.C_m2), nodalCorrections.get(TidalConstant.C_k1)));
        nodalCorrections.put(TidalConstant.C_s6, new NodalCorrection(0, 1));
        nodalCorrections.put(TidalConstant.C_2sm2, nodalCorrections.get(TidalConstant.C_m2));
        nodalCorrections.put(TidalConstant.C_2mk3, new NodalCorrection(0, 1));

        return nodalCorrections;
    }

}
