// code by jph
package ch.alpine.sonata.enc.mxl;

import java.util.Objects;

import ch.alpine.sonata.Figure;
import ch.alpine.sonata.FiguredBass;

/** <suffix>slash</suffix>
 * <suffix>backslash</suffix>
 * <suffix>sharp</suffix>
 * <suffix>double-sharp</suffix>
 * <prefix>flat</prefix>
 * <suffix>flat-flat</suffix>
 * <suffix>natural</suffix>
 * <suffix>cross</suffix> */
class XmlFiguredBass {
  static String format(FiguredBass figuredBass) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("<figured-bass>");
    for (Figure figure : figuredBass.figuresReversed()) {
      stringBuilder.append("<figure>");
      // ---
      String value = null; // TODO TPF depends on context!
      switch (figure.suffix) {
      case INCR:
        value = "sharp"; // cross backslash
        break;
      case DECR:
        value = "flat";
        break;
      default:
      }
      final boolean hasSuffix = Objects.nonNull(value);
      if (figure.number == 3 && hasSuffix) {
        // figure-number can also be omitted (in case of 3)
      } else
        stringBuilder.append("<figure-number>" + figure.number + "</figure-number>");
      if (hasSuffix)
        stringBuilder.append("<suffix>" + value + "</suffix>");
      // ---
      stringBuilder.append("</figure>");
    }
    stringBuilder.append("</figured-bass>");
    return stringBuilder.toString();
  }
}
