// code by jph
package ch.alpine.sonata.hrm;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import ch.alpine.sonata.KeySignature;
import ch.alpine.sonata.Natur;
import ch.alpine.sonata.tri.Chord;
import ch.alpine.sonata.tri.ChromaticAscii;
import ch.alpine.sonata.tri.ChromaticFormat;
import ch.alpine.sonata.tri.Triad;
import ch.alpine.sonata.tri.TriadFormat;

// this has to be data driven! call AdaptedTheory!?
@Deprecated
class ChromaticTheory implements AbstractTheory {
  static final int[][] naturC = { //
      // ---
      { 99, 0, 16, 0, 86, 17, 3, 78, 0, 8, 2, 6, }, // C (8191)
      { 99, 1, 45, 87, 7, 14, 19, 92, 2, 11, 3, 15, }, // Cm (234)
      { 65, 3, 6, 89, 28, 0, 99, 18, 3, 16, 1, 27, }, // Co (263)
      { 84, 0, 24, 2, 18, 17, 15, 76, 0, 99, 4, 32, }, // C6 (1522)
      { 22, 1, 77, 0, 11, 8, 2, 99, 0, 13, 0, 81, }, // G (6012)
      { 17, 7, 80, 1, 12, 10, 1, 99, 0, 33, 82, 2, }, // Gm (1113)
      { 7, 99, 19, 2, 20, 15, 3, 98, 2, 38, 79, 7, }, // Go (209)
      { 19, 9, 81, 0, 99, 29, 7, 83, 0, 19, 3, 13, }, // G6 (1312) // modified a22 c25
      { 15, 0, 99, 0, 11, 2, 77, 33, 3, 82, 1, 17, }, // D (1865)
      { 8, 2, 99, 0, 32, 86, 0, 19, 4, 75, 2, 9, }, // Dm (5911)
      { 17, 1, 99, 1, 37, 74, 3, 4, 76, 19, 1, 14, }, // Do (441)
      { 38, 1, 95, 0, 25, 11, 5, 14, 11, 65, 0, 99, }, // D6 (1559)
      { 3, 78, 35, 1, 87, 16, 2, 15, 0, 99, 2, 8, }, // A (1709)
      { 89, 0, 21, 1, 80, 6, 3, 6, 2, 99, 1, 24, }, // Am (8413)
      { 89, 0, 22, 78, 24, 6, 11, 16, 3, 99, 10, 32, }, // Ao (334)
      { 16, 1, 12, 11, 65, 1, 98, 31, 7, 99, 0, 26, }, // A6 (615)
      { 17, 1, 14, 0, 99, 5, 5, 3, 72, 39, 0, 80, }, // E (2798)
      { 14, 2, 9, 2, 99, 13, 15, 93, 0, 23, 1, 79, }, // Em (3673)
      { 17, 4, 12, 0, 93, 41, 2, 99, 0, 46, 70, 5, }, // Eo (940)
      { 1, 92, 18, 11, 98, 4, 21, 14, 5, 16, 10, 66, }, // E6 (88)
      { 5, 7, 3, 74, 51, 0, 86, 23, 1, 21, 0, 99, }, // B (823)
      { 34, 4, 97, 0, 28, 3, 88, 19, 7, 20, 0, 99, }, // Bm (873)
      { 39, 1, 99, 0, 43, 69, 1, 16, 4, 11, 0, 89, }, // Bo (2967)
      { 27, 13, 8, 2, 43, 5, 91, 27, 72, 35, 2, 97, }, // B6 (37)
      { 1, 75, 3, 3, 8, 0, 98, 10, 1, 5, 51, 19, }, // F# (40)
      { 14, 87, 21, 19, 29, 1, 99, 33, 9, 91, 0, 36, }, // F#m (149)
      { 80, 1, 22, 3, 10, 3, 91, 45, 4, 99, 3, 44, }, // F#o (1546)
      { 0, 87, 25, 50, 25, 12, 37, 50, 0, 12, 0, 87, }, // F#6 (6)
      { 0, 93, 13, 0, 13, 93, 6, 0, 53, 0, 0, 6, }, // C# (12)
      { 2, 92, 37, 15, 97, 2, 15, 20, 92, 22, 0, 7, }, // C#m (53)
      { 4, 81, 64, 1, 99, 44, 11, 75, 1, 25, 3, 6, }, // C#o (993)
      { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, }, // C#6 (0)
      { 96, 0, 21, 84, 6, 18, 3, 9, 93, 6, 0, 12, }, // G# (35)
      { 16, 2, 8, 56, 18, 2, 8, 2, 97, 29, 2, 91, }, // G#m (49)
      { 51, 2, 79, 0, 28, 4, 3, 3, 76, 61, 0, 99, }, // G#o (1533)
      { 0, 0, 0, 85, 0, 57, 0, 0, 42, 0, 14, 0, }, // G#6 (8)
      { 11, 9, 12, 75, 7, 4, 2, 98, 2, 26, 75, 3, }, // D# (87)
      { 15, 0, 23, 61, 23, 0, 92, 23, 7, 23, 61, 15, }, // D#m (15)
      { 7, 3, 6, 81, 64, 3, 99, 48, 5, 95, 0, 32, }, // D#o (550)
      { 92, 0, 7, 78, 0, 14, 7, 0, 14, 0, 85, 0, }, // D#6 (16)
      { 14, 2, 99, 2, 21, 80, 0, 11, 1, 14, 85, 2, }, // B& (841)
      { 6, 61, 25, 0, 14, 97, 0, 23, 0, 25, 40, 2, }, // B&m (45)
      { 3, 82, 31, 2, 99, 11, 8, 17, 1, 26, 76, 9, }, // B&o (205)
      { 19, 2, 15, 0, 26, 82, 0, 99, 0, 43, 70, 2, }, // B&6 (334)
      { 82, 0, 12, 1, 12, 99, 0, 21, 0, 96, 6, 17, }, // F (4656)
      { 99, 1, 13, 0, 14, 69, 2, 21, 74, 11, 4, 21, }, // Fm (165)
      { 13, 1, 16, 1, 27, 59, 4, 3, 79, 26, 0, 99, }, // Fo (411)
      { 71, 0, 99, 0, 38, 81, 1, 19, 1, 12, 2, 19, }, // F6 (1593) // modified b23 g23
      { 3, 1, 86, 0, 10, 70, 0, 1, 90, 7, 0, 99, }, // Fo7 (568)
      { 67, 0, 3, 81, 10, 0, 99, 7, 1, 92, 0, 13, }, // Co7 (300)
      { 1, 96, 8, 0, 99, 3, 3, 83, 0, 9, 80, 3, }, // Go7 (251)
      { 3, 75, 34, 0, 29, 96, 1, 28, 1, 99, 4, 10, }, // F+ (422)
      { 99, 0, 19, 0, 97, 8, 7, 3, 64, 34, 0, 19, }, // C+ (797) // modified b22 d27
      { 14, 3, 5, 63, 45, 3, 23, 99, 1, 24, 1, 96, }, // G+ (301)
      { 20, 18, 98, 1, 30, 5, 89, 24, 7, 30, 91, 30, } // D+ (90)
  };
  static final List<String> listC = List.of( //
      "C", "Cm", "Co", "C6", "G", "Gm", "Go", "G6", //
      "D", "Dm", "Do", "D6", "A", "Am", "Ao", "A6", //
      "E", "Em", "Eo", "E6", "B", "Bm", "Bo", "B6", //
      "F#", "F#m", "F#o", "F#6", "C#", "C#m", "C#o", "C#6", //
      "G#", "G#m", "G#o", "G#6", //
      "D#", "D#m", "D#o", "D#6", //
      "B&", "B&m", "B&o", "B&6", //
      "F", "Fm", "Fo", "F6", //
      "Fo7", "Co7", "Go7", //
      "F+", "C+", "G+", "D+");
  static final TriadFormat formatC = new ChromaticFormat(KeySignature.C, new ChromaticAscii());
  // ---
  final int tonic;

  ChromaticTheory(KeySignature keySignature) {
    tonic = keySignature.tonic();
  }

  private Chord createChord(Triad myRef, Triad triad) {
    final String myString = formatC.format(myRef);
    final int index = listC.indexOf(myString);
    Collection<Natur> myCollection = EnumSet.copyOf(triad.set());
    Collection<Natur> myTolerance = EnumSet.copyOf(triad.set()); // TODO TPF not used
    // TODO TPF if string expression of Alter changes -> this breaks
    for (Natur myNatur : Natur.values()) {
      int prc = naturC[index][myNatur.ordinal()];
      if (20 < prc)
        myCollection.add(myNatur.ascend(tonic));
      if (2 < prc)
        myTolerance.add(myNatur.ascend(tonic));
    }
    return new Chord(triad, myCollection);
  }

  @Override
  public Chord major(Natur pitch) {
    return createChord(Triad.major(pitch.ascend(-tonic)), Triad.major(pitch));
  }

  @Override
  public Chord minor(Natur pitch) {
    return createChord(Triad.minor(pitch.ascend(-tonic)), Triad.minor(pitch));
  }

  @Override
  public Chord augmented(Natur pitch) {
    return createChord(Triad.augmented(pitch.ascend(-tonic)), Triad.augmented(pitch));
  }

  @Override
  public Chord diminished(Natur pitch) {
    return createChord(Triad.diminished(pitch.ascend(-tonic)), Triad.diminished(pitch));
  }

  @Override
  public Chord majorSixth(Natur pitch) {
    return createChord(Triad.majorSixth(pitch.ascend(-tonic)), Triad.majorSixth(pitch));
  }

  @Override
  public Chord diminishedSeventh(Natur pitch) {
    return createChord(Triad.diminishedSeventh(pitch.ascend(-tonic)), Triad.diminishedSeventh(pitch));
  }
}
