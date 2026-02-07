// code by jph
package ch.alpine.sonata.xml;

import java.util.Objects;

import ch.alpine.bridge.lang.EnumValue;
import ch.alpine.sonata.Clef;
import ch.alpine.sonata.Dynamic;
import ch.alpine.sonata.KeyMode;
import ch.alpine.sonata.Ornament;

public enum XmlMeta {
  ;
  public static Ornament fromXmlNotationOrnament(String token) {
    if (token.equalsIgnoreCase("inverted-mordent"))
      return Ornament.PRALL;
    if (token.equalsIgnoreCase("mordent"))
      return Ornament.MORDENT;
    if (token.equalsIgnoreCase("turn"))
      return Ornament.TURN;
    if (token.equalsIgnoreCase("inverted-turn"))
      return Ornament.REVERSE_TURN;
    if (token.equalsIgnoreCase("trill-mark"))
      return Ornament.DOWN_TRILL;
    return null;
  }

  public static Dynamic fromXmlDynamics(String token) {
    if (token.equals("fz") || token.equals("sf") || token.equals("sfz"))
      return Dynamic.F;
    return EnumValue.match(Dynamic.class, token);
  }

  public static String toModeString(KeyMode keyMode) {
    final String string;
    switch (keyMode) {
    case IONIAN:
      string = "major";
      break;
    case AEOLIAN:
      string = "minor";
      break;
    default:
      string = null;
      break;
    }
    return Objects.isNull(string) //
        ? ""
        : "<mode>" + string + "</mode>";
  }

  public static String toClefString(Clef clef) {
    switch (clef) {
    case FRENCH:
      return "<clef><sign>G</sign><line>1</line></clef>";
    case TREBLE:
      return "<clef><sign>G</sign><line>2</line></clef>";
    case SOPRANO:
      return "<clef><sign>C</sign><line>1</line></clef>";
    case MEZZOSOPRANO:
      return "<clef><sign>C</sign><line>2</line></clef>";
    case ALTO:
      return "<clef><sign>C</sign><line>3</line></clef>";
    case TENOR:
      return "<clef><sign>C</sign><line>4</line></clef>";
    case BARITONE:
      return "<clef><sign>C</sign><line>5</line></clef>";
    case BASS:
      return "<clef><sign>F</sign><line>4</line></clef>";
    case SUBBASS:
      return "<clef><sign>F</sign><line>5</line></clef>";
    default:
      System.out.println(clef + " is not supported yet");
    }
    return "";
  }
}
