// code by jph
package ch.alpine.sonata.tri;

/** native storage format of chords in scores */
public final class IntrinsicFormat extends TriadFormat {
  public static final TriadFormat ABSOLUTE = new IntrinsicFormat();

  private IntrinsicFormat() {
    for (Triad triad : Triad.values())
      put(triad, triad.toString());
  }
}
