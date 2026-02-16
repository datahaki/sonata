// code by jph
package ch.alpine.sonata.xml;

import java.io.Serializable;
import java.util.List;

import ch.alpine.tensor.Rational;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.ext.Serialization;
import ch.alpine.tensor.io.MathematicaFormat;

public class XmlSize implements Serializable {
  public static final List<Scalar> MULTIPLIER = List.of( //
      // 1, 1 + 1/2, 1 + 1/2 + 1/4, ...
      Rational.of(1, 1), //
      Rational.of(3, 2), //
      Rational.of(7, 4), //
      Rational.of(15, 8));
  // ---
  public int duration;
  public XmlType type = null;
  public int dots = 0;
  public Scalar ratio = RealScalar.ONE;

  public Scalar getTuplet() {
    return ratio;
  }

  public boolean hasTimeModification() {
    return !getTuplet().equals(RealScalar.ONE);
  }

  public Scalar getCombinedFraction() {
    return type.fraction.multiply(MULTIPLIER.get(dots)).multiply(getTuplet());
  }

  public String toLyString() {
    return type.toLyString() + ".".repeat(dots);
  }

  public XmlSize copy() {
    try {
      return Serialization.copy(this);
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Override
  public String toString() {
    return MathematicaFormat.concise("XmlSize", duration, type, dots, ratio);
  }
}
