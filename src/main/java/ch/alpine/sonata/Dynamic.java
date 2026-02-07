// code by jph
package ch.alpine.sonata;

/** Dynamic uses the same velocity map as "Logic Pro 9 dynamics"
 * https://en.wikipedia.org/wiki/Dynamics_(music)
 * 
 * Not supported:
 * 
 * Sudden changes in dynamics may be notated by adding the word subito (Italian for suddenly) as a prefix or suffix to the new dynamic notation.
 * Accented notes (notes to emphasize or play louder compared to surrounding notes) can be notated
 * sforzando, sforzato, forzando or forzato (abbreviated sfz, sf, or fz) ("forcing" or "forced" accent).
 * 
 * The fortepiano notation fp indicates a forte followed immediately by piano.
 * Sforzando piano (sfzp or sfp) indicates a sforzando followed immediately by piano.
 * 
 * Rinforzando, rfz or rf (literally "reinforcing") indicates that several notes, or a short phrase, are to be emphasized. */
public enum Dynamic {
  /** pianississimo and meaning "very very soft" */
  PPP("pianississimo"),
  /** pianissimo and meaning "very soft" */
  PP("pianissimo"),
  /** piano, meaning "soft" */
  P("piano"),
  /** mezzo-piano, meaning "moderately soft" */
  MP("mezzo-piano"),
  /** mezzo-forte, meaning "moderately loud" */
  MF("mezzo-forte"),
  /** forte, meaning "loud" */
  F("forte"),
  /** fortissimo and meaning "very loud" */
  FF("fortissimo"),
  /** fortississimo and meaning "very very loud" */
  FFF("fortississimo");

  public final String string;
  public final int velocity;

  private Dynamic(String string) {
    this.string = string;
    this.velocity = Math.min(16 + 16 * ordinal(), 127);
  }

  public double volume() {
    return velocity / 127.;
  }

  public boolean leq(Dynamic dynamics) {
    return ordinal() <= dynamics.ordinal();
  }
}
