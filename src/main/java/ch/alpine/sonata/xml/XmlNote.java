// code by jph
package ch.alpine.sonata.xml;

import java.awt.Color;
import java.util.Objects;

import ch.alpine.sonata.Alter;
import ch.alpine.sonata.Diatone;
import ch.alpine.sonata.Dynamic;
import ch.alpine.sonata.Note;
import ch.alpine.sonata.Ornament;
import ch.alpine.sonata.Tone;
import sys.mat.Ratio;

/** presently, used by Xml and LilyPond format
 * for read and write */
public class XmlNote {
  public static final int OCTAVE_XML_TO_NATIVE = 1;
  // ---
  /** determined by print-object="no" */
  public boolean isHidden;
  public boolean isGrace;
  public boolean isChord;
  // ---
  public Diatone step = null; // == null, in case of rest
  public Alter alter = Alter.NEUTRAL;
  public int octave;
  /** in MusicXML format duration can be ambiguous: for instance: when quarter.duration=256 then 16th 3/2 tuplet states duration of 42, or 43 because
   * 256/4/3*2=42.666667 */
  public boolean tie = false;
  public int voice;
  public XmlSize xmlSize = new XmlSize();
  public XmlBeam xmlBeam = null;
  /** time-modification */
  public Color color = Color.BLACK; // ignored by Finale 2011, as well as Sibelius 7
  public Ornament ornament = null;
  public Dynamic dynamics = null;
  public boolean lyric_dash = false;
  public String lyric = "";

  public boolean isRest() {
    return Objects.isNull(step);
  }

  String getAccidental() {
    switch (alter) {
    case FLAT2:
      return "flat-flat";
    case FLAT:
      return "flat";
    case SHARP:
      return "sharp";
    case SHARP2:
      return "double-sharp";
    }
    return null;
  }

  public Note toNote() {
    return new Note(Tone.from(step, alter, octave + OCTAVE_XML_TO_NATIVE), isGrace ? 1 : xmlSize.duration);
  }

  public String toInfoString() {
    return (isRest() ? "rest " + xmlSize.duration : toNote().toString()) + " in " + voice + (isChord ? "CHORD" : "");
  }

  public String toXmlString(final boolean tie_stop) {
    if (voice <= 0)
      throw new RuntimeException("voice has to be positive");
    // ---
    StringBuilder myStringBuffer = new StringBuilder();
    // --- dynamics, and some ornaments require to be prepended before <note>
    {
      boolean hasDirectionDynamics = Objects.nonNull(dynamics);
      if (hasDirectionDynamics) {
        myStringBuffer.append("<direction>");
        myStringBuffer.append("<direction-type>");
        myStringBuffer.append("<dynamics>");
        myStringBuffer.append("<" + dynamics + "/>");
        myStringBuffer.append("</dynamics>");
        myStringBuffer.append("</direction-type>");
        myStringBuffer.append("<voice>" + voice + "</voice>");
        // myStringBuffer.append("<staff>1</staff>"); // <staff>1</staff>
        myStringBuffer.append("</direction>");
      }
    }
    // ---
    // this looks really ugly when imported
    // {
    // if (hasDirectionOrnament()) {
    // myStringBuffer.append("<direction>");
    // myStringBuffer.append("<direction-type>");
    // myStringBuffer.append("<other-direction print-object=\"no\">");
    // myStringBuffer.append(toXmlDirectionOrnament(myOrnament));
    // myStringBuffer.append("</other-direction>"); //
    // myStringBuffer.append("</direction-type>");
    // myStringBuffer.append("<voice>" + voice + "</voice>");
    // // myStringBuffer.append("<staff>1</staff>");
    // myStringBuffer.append("</direction>");
    // }
    // }
    // --- note color does not work when importing to Sibelius or Finale
    // String.format("<note color=\"#%02X%02X%02X\">", myColor.getRed(), myColor.getGreen(), myColor.getBlue());
    myStringBuffer.append("<note>");
    if (isRest()) {
      myStringBuffer.append("<rest/>");
    } else {
      myStringBuffer.append("<pitch>");
      myStringBuffer.append("<step>" + step + "</step>");
      if (!alter.isNeutral())
        myStringBuffer.append("<alter>" + alter.delta() + "</alter>");
      myStringBuffer.append("<octave>" + octave + "</octave>");
      myStringBuffer.append("</pitch>");
    }
    myStringBuffer.append("<duration>" + xmlSize.duration + "</duration>");
    if (tie_stop)
      myStringBuffer.append("<tie type=\"stop\"/>"); // Sibelius 7 does not use stop
    if (tie)
      myStringBuffer.append("<tie type=\"start\"/>");
    // ---
    myStringBuffer.append("<voice>" + voice + "</voice>");
    // myStringBuffer.append("<staff>1</staff>"); // <staff>1</staff>
    myStringBuffer.append("<type>" + xmlSize.type + "</type>");
    myStringBuffer.append("<dot/>".repeat(xmlSize.dots));
    // ---
    if (xmlSize.hasTimeModification()) {
      myStringBuffer.append("<time-modification>");
      Ratio ratio = Ratio.of(xmlSize.ratio);
      myStringBuffer.append("<actual-notes>" + ratio.den() + "</actual-notes>");
      myStringBuffer.append("<normal-notes>" + ratio.num() + "</normal-notes>");
      myStringBuffer.append("</time-modification>");
    }
    // ---
    if (0 < xmlSize.type.beams && xmlBeam != null)
      for (int beam = 1; beam <= xmlSize.type.beams; ++beam)
        myStringBuffer.append("<beam number=\"" + beam + "\">" + xmlBeam + "</beam>");
    boolean hasNotationOrnament = hasNotationOrnament();
    final String myOrnamentString = hasNotationOrnament ? toXmlNotationOrnament(ornament) : null;
    boolean hasFermata = ornament != null && ornament.equals(Ornament.FERMATA);
    if (tie_stop || tie || hasNotationOrnament || hasFermata) {
      myStringBuffer.append("<notations>");
      // TIE ---
      if (tie_stop)
        myStringBuffer.append("<tied type=\"stop\"/>"); // Sibelius 7 does not use stop
      if (tie)
        myStringBuffer.append("<tied type=\"start\"/>");
      // ORNAMENTS ---
      if (hasNotationOrnament)
        myStringBuffer.append("<ornaments>" + myOrnamentString + "</ornaments>");
      // FERMATA ---
      if (hasFermata)
        myStringBuffer.append("<fermata type=\"upright\"/>");
      // <fermata type="upright">angled</fermata>
      // <fermata type="upright">square</fermata>
      myStringBuffer.append("</notations>");
    }
    // ---
    if (!lyric.isEmpty()) { // lyric != null &&
      String[] mySplit = lyric.split("\\|");
      for (String myString : mySplit) {
        myStringBuffer.append("<lyric>");
        myStringBuffer.append("<syllabic>single</syllabic>"); // other options: middle
        myStringBuffer.append("<text>" + myString + "</text>"); // requires formatting
        myStringBuffer.append("</lyric>");
      }
    }
    // ---
    myStringBuffer.append("</note>");
    return myStringBuffer.toString();
  }

  @SuppressWarnings("unused")
  private boolean hasDirectionOrnament() { // unused because it looks ugly ...
    return ornament != null //
        && toXmlDirectionOrnament(ornament) != null;
  }

  private static String toXmlDirectionOrnament(Ornament ornament) {
    final String myString;
    switch (ornament) {
    case ACCENT:
    case MARCATO:
      myString = ornament.toString() + " above";
      break;
    case UP_PRALL:
      myString = "Cadence";
      break;
    case DOWN_MORDENT:
      myString = "Double cadence-mordent";
      break;
    // Trill -> tr
    default:
      myString = null;
    }
    return myString;
  }

  private boolean hasNotationOrnament() {
    return ornament != null //
        && toXmlNotationOrnament(ornament) != null;
  }

  /** <!ELEMENT ornaments (((trill-mark | turn | delayed-turn | inverted-turn | delayed-inverted-turn | vertical-turn | shake | wavy-line | mordent |
   * inverted-mordent | schleifer | tremolo | other-ornament), accidental-mark*)*)>
   * 
   * @param ornament
   * @return */
  private static String toXmlNotationOrnament(Ornament ornament) {
    final String string;
    switch (ornament) {
    case PRALL:
      string = "inverted-mordent";
      break;
    case MORDENT:
    case TURN:
      string = ornament.toString().toLowerCase();
      break;
    case REVERSE_TURN:
      string = "inverted-turn";
      break;
    case DOWN_TRILL:
    case UP_TRILL:
      string = "trill-mark";
      break;
    default:
      return null;
    }
    return "<" + string + "/>";
  }

  /** time modification has to be consolidated outside of function
   * 
   * @return */
  public String toLyString() {
    StringBuilder stringBuffer = new StringBuilder();
    if (isRest())
      // option to replace with capital 'R' outside in case entire measure is rest
      stringBuffer.append("r");
    else {
      stringBuffer.append(step.lowerCase());
      stringBuffer.append((0 < alter.delta() ? "is" : "es").repeat(Math.abs(alter.delta())));
      if (3 < octave)
        stringBuffer.append("\'".repeat(octave - 3));
      else if (octave < 3)
        stringBuffer.append(",".repeat(3 - octave));
    }
    // use c\longa and c\breve for longa and breve
    stringBuffer.append(xmlSize.toLyString());
    // order of tie and ornament seems to be irrelevant
    if (tie)
      stringBuffer.append('~');
    if (ornament != null)
      stringBuffer.append(toLyOrnament(ornament));
    if (dynamics != null)
      stringBuffer.append("\\" + dynamics);
    return stringBuffer.toString();
  }

  public static String toLyOrnament(Ornament ornament) {
    switch (ornament) {
    case DOWN_TRILL:
    case UP_TRILL:
      return "\\trill";
    case UP_ACCIACCATURA:
    case DOWN_ACCIACCATURA:
      // \acciaccatura d8 c8 // more elaborate to encode since pitch need to be defined
      return "";
    default:
      return "\\" + ornament.toString().replace("_", "").toLowerCase();
    }
  }

  public String getLyric() {
    StringBuilder stringBuilder = new StringBuilder(lyric);
    if (lyric_dash && !lyric.endsWith("-"))
      stringBuilder.append("-");
    return stringBuilder.toString();
  }
}
