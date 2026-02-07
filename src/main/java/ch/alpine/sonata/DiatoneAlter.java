// code by jph
package ch.alpine.sonata;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import ch.alpine.tensor.ext.PackageTestAccess;

public class DiatoneAlter implements Serializable {
  private static final List<DiatoneAlter> LIST = Stream.of(Diatone.values()) //
      .flatMap(diatone -> Stream.of(Alter.values()).map(alter -> new DiatoneAlter(diatone, alter))) //
      .toList();

  public static DiatoneAlter from(Diatone diatone, Alter alter) {
    return LIST.get(diatone.ordinal() * 5 + alter.ordinal());
  }

  @PackageTestAccess
  static List<DiatoneAlter> all() {
    return LIST;
  }

  // ---
  private final Diatone diatone;
  private final Alter alter;
  private final Natur natur;

  private DiatoneAlter(Diatone diatone, Alter alter) {
    this.diatone = diatone;
    this.alter = alter;
    natur = Natur.fromPitch(diatone.white_delta() + alter.delta());
  }

  public Diatone diatone() {
    return diatone;
  }

  public Alter alter() {
    return alter;
  }

  public Natur natur() {
    return natur;
  }

  /** Value suitable for display in html.
   * Characters are subject to change in later versions.
   * 
   * @return */
  public String toStringUnicode() {
    return diatone + alter().getAccidentalUnicode();
  }

  @Override
  public int hashCode() {
    return Objects.hash(diatone, alter);
  }

  @Override
  public boolean equals(Object object) {
    return object instanceof DiatoneAlter diatoneAlter //
        && diatone.equals(diatoneAlter.diatone) //
        && alter.equals(diatoneAlter.alter);
  }

  @Override
  public String toString() {
    return diatone + alter().getAccidental();
  }
}
