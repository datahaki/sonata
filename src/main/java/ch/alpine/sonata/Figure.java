// code by jph
package ch.alpine.sonata;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

import sys.mat.IntRange;

/** class represents figure within {@link FiguredBass} */
public class Figure implements Serializable {
  public static final IntRange VALID_RANGE = IntRange.closed(1, 9);
  public static final Pattern PATTERN = Pattern.compile("\\d+[\\+#/\\\\-f]?");
  // ---
  public static final Figure SEKUNDE = new Figure(2);
  public static final Figure TERZ = new Figure(3);
  public static final Figure QUARTE = new Figure(4);
  public static final Figure QUINTE = new Figure(5);
  public static final Figure SEXTE = new Figure(6);
  public static final Figure SEPTIME = new Figure(7);
  // ---
  public final int number;
  public final Suffix suffix;

  public Figure(int number) {
    this(number, Suffix.NONE);
  }

  public Figure(int number, Suffix suffix) {
    this.number = VALID_RANGE.requireInside(number);
    this.suffix = Objects.requireNonNull(suffix);
  }

  public int delta_diatonic() {
    return number - 1;
  }

  public static Figure fromString(String string) {
    if (string.matches("\\d+"))
      return new Figure(Integer.parseInt(string));
    // ---
    final int index = string.length() - 1;
    return new Figure( //
        Integer.parseInt(string.substring(0, index)), //
        Suffix.fromAbbreviation(string.charAt(index)));
  }

  @Override
  public String toString() {
    return number + suffix.string;
  }

  @Override
  public int hashCode() {
    return number * 3 + suffix.ordinal();
  }

  @Override
  public boolean equals(Object object) {
    return object instanceof Figure figure //
        && number == figure.number //
        && suffix.equals(figure.suffix);
  }
}
