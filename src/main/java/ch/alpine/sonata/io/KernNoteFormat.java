// code by jph
package ch.alpine.sonata.io;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.alpine.sonata.Diatone;
import ch.alpine.sonata.Note;
import ch.alpine.sonata.Tone;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.Scalars;
import sys.mat.IntRange;

public class KernNoteFormat implements NoteFormat {
  private static final int CENTER_OCTAVE = 5;
  final int basis;

  public KernNoteFormat(int basis) {
    this.basis = basis;
  }

  @Override
  public Note parseNote(String string) {
    // System.out.println("PARSE: " + string);
    return new Note(parseTone(string), getTicks(string, basis));
  }

  @Override
  public String format(Note note) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Tone parseTone(String string) {
    int[] occur = new int[128];
    for (char chr : string.toCharArray())
      ++occur[chr];
    if (0 < occur['q'])
      throw new RuntimeException("grace note not permitted in final score");
    // ---
    Matcher matcher = PATTERN_MATCH.matcher(string);
    matcher.find();
    String matchString = matcher.group();
    String myFirst = matchString.substring(0, 1);
    // TODO TPF switch to upper case for efficiency
    String myLower = myFirst.toLowerCase();
    boolean status = myFirst.equals(myLower); // lower case means higher pitch
    int octave = matchString.length() * (status ? 1 : -1) + (status ? -1 : 0);
    // TODO TPF computation redundant to Tones
    return Tone.from(Diatone.valueOf(myLower.toUpperCase()).ordinal() + (CENTER_OCTAVE + octave) * 7, occur['#'] - occur['-']);
  }

  // ---
  static final Pattern PATTERN_NOTE = Pattern.compile("\\d+\\.*[A-Ga-g]+#*-*q*");
  static final Pattern PATTERN_REST = Pattern.compile("\\d+\\.*r");
  public static final Pattern PATTERN_DIGIT = Pattern.compile("\\d+");
  static final Pattern PATTERN_MATCH = Pattern.compile("[A-Ga-g]+");

  public static boolean containsNote(String string) {
    Matcher matcher = PATTERN_NOTE.matcher(string);
    return matcher.find() //
        && !matcher.group().endsWith("q");
  }

  public static boolean containsRest(String string) {
    Matcher matcher = PATTERN_REST.matcher(string);
    return matcher.find();
  }

  public static int getTicks(String string, int basis) {
    int[] occur = new int[128];
    for (char chr : string.toCharArray())
      ++occur[chr];
    Matcher matcher = PATTERN_DIGIT.matcher(string);
    matcher.find();
    int den = Integer.parseInt(matcher.group());
    int ticks = den == 0 //
        ? 2 * basis
        : Scalars.intValueExact(RationalScalar.of(basis, den));
    int incr = ticks;
    for (@SuppressWarnings("unused")
    int __ : IntRange.positive(occur['.'])) {
      if (incr % 2 != 0)
        throw new RuntimeException("lcm wrong when dotting duration");
      incr /= 2;
      ticks += incr;
    }
    return ticks;
  }
}
