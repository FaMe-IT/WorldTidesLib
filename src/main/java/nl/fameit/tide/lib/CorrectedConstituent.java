package nl.fameit.tide.lib;

/**
 * Constituent value corrected for nodal influences
 */
public class CorrectedConstituent {
    public final double speed;
    public final double phase;
    public final double amplitude;

    public CorrectedConstituent(final double speed, final double phase, double amplitude) {
        this.speed = speed;
        this.phase = phase;
        this.amplitude = amplitude;
    }

}
