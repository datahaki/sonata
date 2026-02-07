// code by jph
package ch.alpine.sonata.enc.md;

import java.util.List;
import java.util.stream.Collectors;

import ch.alpine.sonata.enc.st.Stage2Reader;
import ch.alpine.sonata.scr.Score;
import sys.mat.TokenStream;

/** methods are valid for {@link MusedataReader} and {@link Stage2Reader} */
public class MusedataMeta {
  /** in all encountered examples corresponds to 4/4 */
  private static final List<String> RATIO_PACIFY = List.of( //
      "0/0", "12/12", "24/24", "48/48", "96/96");

  /** @param string of the form 3/4, 12/12, ...
   * @return */
  public static String getSafeRatio(String string) {
    return RATIO_PACIFY.contains(string) ? "4/4" : string;
  }

  /** converts special characters in title, and lyrics; used extensively by {@link Stage2Reader}
   * 
   * @param string
   * @return */
  public static String format(String string) { // only used by Stage2Format
    // ---
    string = string.replace("\\A3", "\u00C4"); // Ae
    string = string.replace("\\a8", "\u00E0"); // a\ ariodant_04
    string = string.replace("\\a3", "\u00E4"); // ae
    // ---
    string = string.replace("\\3A", "\u00C4"); // Ae
    string = string.replace("\\8a", "\u00E0"); // a\ ariodant_04
    string = string.replace("\\3a", "\u00E4"); // ae
    // ---
    string = string.replace("\\E8", "\u00C8"); // E\
    string = string.replace("\\e8", "\u00E8"); // e\ ariodant_04
    string = string.replace("\\e7", "\u00E9"); // e/ ariodant_53
    // ---
    string = string.replace("\\8e", "\u00E8"); // e\ ariodant_04
    string = string.replace("\\7e", "\u00E9"); // e/ ariodant_53
    // ---
    string = string.replace("\\i8", "\u00EC"); // i\
    // ---
    string = string.replace("\\8i", "\u00EC"); // i\
    // ---
    string = string.replace("\\O3", "\u00D6"); // Oe
    string = string.replace("\\o8", "\u00F2"); // o\ ariodant_04
    string = string.replace("\\o3", "\u00F6"); // oe
    // ---
    string = string.replace("\\3O", "\u00D6"); // Oe (not used)
    string = string.replace("\\8o", "\u00F2"); // o\ ariodant_04
    string = string.replace("\\3o", "\u00F6"); // oe
    // ---
    string = string.replace("\\U3", "\u00DC"); // Ue bwv0045_03
    string = string.replace("\\u8", "\u00F9"); // u\ ariodant_03
    string = string.replace("\\u3", "\u00FC"); // ue cantata001_03
    // ---
    string = string.replace("\\3U", "\u00DC"); // Ue
    string = string.replace("\\8u", "\u00F9"); // u\ ariodant_03
    string = string.replace("\\3u", "\u00FC"); // ue cantata001_02
    // ---
    string = string.replace("\\0/", "\u00DF"); // sz cantata135_04 (abundant)
    // myString = myString.replace("\\s2", "\u00DF"); // sz Bach_Cantatas.bwv0058_02 (only once so converted to \0/) //
    // ---
    /** connects two syllables on one tone; used in Italian arias */
    string = string.replace("\\0+", " "); // ariodant_03 (abundant)
    // ---
    /** leading double quotation marks ``, terminating ones are simply put as " */
    string = string.replace("\\0\"", "\""); // cantata020_02, bwv0165_05
    string = string.replace("\\\"0", "\""); // cantata135_06, bwv0173_01
    // ---
    string = string.replace("\\0>", "fl"); // Judas Maccabeus
    // ---
    string = string.replace("_", "");
    string = composeLyric(string);
    // ---
    if (0 <= string.indexOf('\\'))
      System.out.println(MusedataMeta.class.getSimpleName() + "::format incomplete: " + string);
    return string.trim();
  }

  private static String composeLyric(String string) {
    String[] split = string.split("\\|");
    StringBuilder stringBuilder = new StringBuilder();
    for (String token : split) {
      token = token.trim();
      if (token.isEmpty() || token.equals("-")) {
        // void
      } else {
        if (0 < stringBuilder.length())
          stringBuilder.append('|');
        stringBuilder.append(token);
      }
    }
    return stringBuilder.toString();
  }

  // used in MD Stage2
  public static int getTicks(String string) {
    return Integer.parseInt(string.substring(5, 8).trim());
  }

  // used in MD Stage2
  public static void handleLine(Score score, int line_count, String string) {
    switch (line_count) {
    case 4: {
      score.comment = TokenStream.of(string).collect(Collectors.joining(" "));
      break;
    }
    case 6:
      score.title = format(string);
      break;
    case 7:
      String trim = string.trim();
      if (!trim.isEmpty()) {
        if (!score.title.isEmpty())
          score.title += " - ";
        score.title += format(trim);
      }
      break;
    default:
      break;
    }
  }
}
