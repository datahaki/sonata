// code by jph
package ch.alpine.sonata.enc.st;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import ch.alpine.sonata.Ornament;
import ch.alpine.sonata.enc.md.MusedataReader;

/** used in {@link MusedataReader} and {@link Stage2Reader} */
public enum Stage2Ornaments {
  ;
  public static Ornament getFromLine(String myString) {
    final int length = myString.length();
    if (31 < length && !myString.substring(31, Math.min(33, length)).trim().isEmpty())
      return get(new StringTokenizer(myString.substring(31)).nextToken()); // get only first
    return null;
  }

  private static Ornament get(String token) {
    List<Ornament> list = new LinkedList<>();
    boolean panic = false;
    boolean leave = false;
    for (char myChar : token.toCharArray()) {
      switch (myChar) {
      case 'w':
        list.add(Ornament.PRALL);
        break;
      case 'M':
        list.add(Ornament.MORDENT);
        break;
      /** N = MULTIPLE MORDENT, m. 15: Repeat the mordent several times; and this may include an anticipatory mordent, before the beat, or possibly one on the
       * preceding B4 eighth to finish off the trill. (See Neumann, p. 441.) */
      case 'N': // [not 100%] single example (see bwv0780)
        list.add(Ornament.PRALL_MORDENT); // DownMordent
        break;
      case 'r': // [not 100%] (see bwv0851_2, bwv0879_1)
        list.add(Ornament.TURN);
        break;
      /***************************************************************************************/
      /** J = COMPOUND SLIDE-TRILL, m. 16: The first "Doppelt-Cadence" of the "Explication", a trill preceded by a slide from the lower auxiliary up through the
       * main note. A downward hook is at the start of a long-trill symbol. */
      case 'J': // not sure since only one sample (see bwv0780)
        list.add(Ornament.UP_PRALL);
        break;
      /** T = COMPOUND TURN-TRILL, m. 15: This mark appears to be the second "Doppelt-Cadence" of the "Explication", a turn followed by a trill. The upward hook
       * at the beginning is ambiguous, however -- it could be a hastily drawn vertical line, in which case this mark would be an "Accent und Trillo", an
       * eighth-note appogiatura followed by a trill. The occurrence of sixteenth-note turns at analogous passages -- e.g. m. 4 and 32, r.h. -- seems to support
       * the choice of a turn-trill, but the performer should experiment with both interpretations. m. 33: The distinct hook here shows this unambiguously to be
       * a turn-trill. */
      case 'T': // not sure since only one sample (see bwv0780, bwv0791)
        list.add(Ornament.DOWN_PRALL);
        break;
      /** K = COMPOUND SLIDE-TRILL-MORDENT, m. 10: The first "Doppelt-Cadence" of the "Explication", a slide-trill (see Inv. 9, BWV 780, r.h., m. 16) completed
       * by a mordent. A downward hook is at the start of a long-trill symbol, and at the end is a mordent slash. */
      case 'K': // (see bwv0782, bwv0783)
        list.add(Ornament.UP_MORDENT);
        break;
      /** U = COMPOUND TURN-TRILL-MORDENT, m. 32: The second "Doppelt-Cadence und Mordant" in the "Explication", a long trill with an upper hook at the start
       * and a mordent slash at the end. */
      case 'U': // single example (see bwv0776)
        list.add(Ornament.DOWN_MORDENT);
        break;
      /***************************************************************************************/
      case 't': // symbol tr (see bwv1051_02)
        list.add(Ornament.DOWN_TRILL);
        // panic = true;
        break;
      /** t~ = LONG TRILL, m. 22: As in Inv. 2, BWV 773, l.h. (which see), the trill begins with a definite upward stroke, leading BG to misinterpret it as a
       * compound turn-trill. Here, the downward leap to the A#4 does suggest the possibility of an compound appogiatura-trill, the "Accent-und-Trillo" of the
       * "Explication". */
      case '~': // part of t~
        break;
      /***************************************************************************************/
      /** compromises */
      /** L = COMPOUND APPOGIATURA-TRILL, mm. 12, 23, track 1: The second "Accent und Trillo" of the "Exposition", an upward vertical stroke at the head of a
       * trill symbol, signifying a prolongation of the first upper auxiliary. */
      case 'L': // single example, not clear (see bwv0791)
        list.add(Ornament.PRALL);
        break;
      case 'k': // very rare, at least graphically identical to Prall, but not in aria (see bwv0171_04.st2\01, bwv0774)
        list.add(Ornament.PRALL);
        break;
      case 'd': // delayed turn, unfortunately we don't have a way to encode yet (see bwv0780)
        if (list.isEmpty())
          list.add(Ornament.TURN);
        break;
      case '>': // accent (not verified)
        list.add(Ornament.ACCENT);
        break;
      /***************************************************************************************/
      case 'E': // fermata upside down
      case 'F': // fermata
        list.add(Ornament.FERMATA);
        break;
      /***************************************************************************************/
      // --- the following characters don't represent ornaments
      case '.': // probably staccato (see bwv0047_04) [not in md]
      case 'i': // \portato (see bwv0851_2, bwv0879_2)
      case '^': // make clear pitch by adding sharp, flat or neutral
        // neutral sign (see bwv0852_1)
        // additional accidentals (see bwv0875_2)
      case '+': // accidental
      case '-':
      case '(': // slur (see bwv0006_05)
      case ')': //
      case '[': // related to grace notes
      case ']': // related to grace notes
      case '/': //
      case '\\': // squartet159_03.st2
      case '*': // tuplet opening (see bwv0879_2)
      case '!': // tuplet closing
      case 'S': // glissando [ceg] -> c e g quick (see bwv0791)
      case '%': // flag to ignore next character, but we discard (see bwv0006_05)
      case 'm': // part of mp, or mf for mezzo-pf (see bwv0145_03) [not in md]
      case 'p': // "piano" (see bwv0047_04) [not in md]
      case 'f': // "forte" (see bwv0047_04) [not in md]
        break;
      /** BEGIN: not in md */
      case '0': // ??? Haydn/StringQuartets/hob03_65_1.st2/01 )0
      case ',': // ??? mozart.duo423_03
      case '\'': // ?? Haydn/StringQuartets/hob03_60_1.st2/03
      case '{': // ??? Haydn/Symphonies/hob01_102_4.st2/04
      case '}': // ??? Haydn/Symphonies/hob01_102_4.st2/04
      case 'Z': // ??? mozart.duo423_02
      case 'b': // ??? mozart.duo423_03
      case 'h': // ??? mozart.duo424_02
      case 's': // ??? squartet155_02
      case 'u': // ??? Haydn/StringQuartets/hob03_57_1.st2/01 "(rus"
        // panic = true;
        break;
      // Stage2: squartet155_01 (tsp (tb
      // Stage2: duo424_02: hr
      /** END: not in md */
      case '&': // e.g. &Xt (see bwv0861_2), or footnote &1 (see alv065_07_2)
        leave = true;
        break;
      default:
        panic = true;
        break;
      }
      if (leave)
        break;
    }
    if (panic)
      throw new RuntimeException("PANIC: " + token);
    if (1 < list.size())
      throw new RuntimeException("AMBIGUOUS");
    return list.isEmpty() ? null : list.get(0);
  }
}
