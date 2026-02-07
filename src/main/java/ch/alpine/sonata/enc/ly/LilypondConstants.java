// code by jph
package ch.alpine.sonata.enc.ly;

import java.awt.Color;
import java.util.List;
import java.util.Objects;

import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.sca.Round;
import sys.mat.IntRange;

public enum LilypondConstants {
  ;
  public static String of(boolean value) {
    return value //
        ? "##t"
        : "##f";
  }

  public static final List<String> twoSidedBook(int inner, int outer) {
    return List.of( //
        "two-sided = ##t", //
        "inner-margin = " + inner + "\\mm", //
        "outer-margin = " + outer + "\\mm"); //
  }

  /** The level of indentation for the first system in a score. */
  public static final String indentZero = "indent = 0\\mm";
  /** toc with dots */
  public static final String tocItemWithDotsMarkup = "tocItemMarkup = \\tocItemWithDotsMarkup";

  public static String system_system_spacing_padding(int i) {
    return "system-system-spacing.padding = #" + i;
  }

  static String rgbColor(Color color) {
    float[] myFloat = color.getRGBComponents(new float[4]);
    StringBuilder stringBuilder = new StringBuilder("#(rgb-color");
    for (int c0 : IntRange.positive(3)) {
      stringBuilder.append(" ");
      stringBuilder.append(Round._3.apply(RealScalar.of(myFloat[c0])));
    }
    stringBuilder.append(")");
    return stringBuilder.toString();
  }

  static String inQuotes(String string) {
    return '\"' + string.replace("\"", "''") + '\"';
  }

  static String asMarkup(boolean fillLine, String prefix, String string) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("\\markup ");
    if (fillLine)
      stringBuilder.append("\\fill-line ");
    stringBuilder.append("{ ");
    if (Objects.nonNull(prefix) && !prefix.isEmpty())
      stringBuilder.append(prefix + " ");
    stringBuilder.append(inQuotes(string));
    stringBuilder.append("}");
    // \markup \with-color #white { ... }
    return stringBuilder.toString();
  }

  public static String layoutStaff(int size) {
    return "#(layout-set-staff-size " + size + ")";
  }
}
