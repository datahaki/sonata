// code by jph
package ch.alpine.sonata.xml;

import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.chq.IntegerQ;
import sys.mat.Ratio;

/** valid xml note types are specifier at
 * 
 * <a href="http://www.musicxml.com/for-developers/musicxml-dtd/note-elements/">link</a> */
public enum XmlType {
  _128th(RationalScalar.of(1, 128), 5), //
  _64th(RationalScalar.of(1, 64), 4), //
  _32nd(RationalScalar.of(1, 32), 3), //
  /** semiquaver */
  _16th(RationalScalar.of(1, 16), 2),
  /** quaver */
  _eighth(RationalScalar.of(1, 8), 1),
  /** crochet */
  _quarter(RationalScalar.of(1, 4), 0),
  /** minim */
  _half(RationalScalar.of(1, 2), 0),
  /** semibreve */
  _whole(RationalScalar.of(1, 1), 0),
  /** A double whole note (American), breve (international), or double note is a note lasting two times as long as a whole note. */
  _breve(RationalScalar.of(2, 1), 0),
  /** A longa or (American) quadruple/sextuple whole note is a musical note that could be either twice or three times as long as a breve. */
  _long(RationalScalar.of(4, 1), 0), //
  _maxima(RationalScalar.of(8, 1), 0); // supported by ly

  public final Scalar fraction;
  final Ratio ratio;
  final int beams;

  private XmlType(Scalar fraction, int beams) {
    this.fraction = fraction;
    ratio = Ratio.of(fraction);
    this.beams = beams;
  }

  public boolean isAvailable(int quarter, Scalar multiplier) {
    return IntegerQ.of(multiplier.multiply(fraction.multiply(RealScalar.of(4 * quarter))));
  }

  public int duration(int quarter, Scalar multiplier) {
    return Scalars.intValueExact(multiplier.multiply(fraction.multiply(RealScalar.of(4 * quarter))));
  }

  @Override
  public String toString() {
    return name().substring(1);
  }

  public String toLyString() {
    if (ratio.num() == 1)
      return "" + ratio.den();
    switch (ratio.num()) {
    case 4:
      return "\\longa";
    default:
      return '\\' + toString();
    }
  }

  public static XmlType from(String string) {
    return valueOf("_" + string);
  }
}
