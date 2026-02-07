// code by jph
package ch.alpine.sonata.hrm;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import ch.alpine.sonata.Natur;
import ch.alpine.sonata.Naturs;
import ch.alpine.sonata.tri.Chord;
import ch.alpine.sonata.tri.Triad;

/** universal compromise among chord theories */
class UniformTheory implements AbstractTheory {
  // input: 0 -> C major
  // musthave=[c, e, g], optional=[c, d, e, f, g], prohibit=[cd, de, fg, ga, a, ab, b]
  @Override
  public Chord major(Natur natur) {
    Set<Natur> mySet = new HashSet<>(Naturs.keySignature(natur).diatonicScale().map().keySet());
    mySet.remove(natur.ascend(9)); // C major vs. a minor
    mySet.remove(natur.ascend(11)); // C major vs. G7
    return new Chord(Triad.major(natur), mySet);
  }

  // input: 9 -> a minor
  // musthave=[c, e, a], optional=[c, d, e, a, b], prohibit=[cd, de, f, fg, g, ga, ab]
  @Override
  public Chord minor(Natur natur) {
    Triad triad = Triad.minor(natur);
    natur = natur.ascend(3);
    Set<Natur> mySet = new HashSet<>(Naturs.keySignature(natur).diatonicScale().map().keySet());
    mySet.remove(natur.ascend(5)); // ???
    mySet.remove(natur.ascend(7)); // C maj vs. a min
    return new Chord(triad, mySet);
  }

  @Override
  public Chord augmented(Natur pitch) {
    return new Chord(Triad.augmented(pitch));
  }

  // input: 11 -> Bo = "G7"
  @Override
  public Chord diminished(Natur natur) {
    Triad triad = Triad.diminished(natur);
    natur = natur.ascend(1);
    Set<Natur> mySet = new HashSet<>(Naturs.keySignature(natur).diatonicScale().map().keySet());
    // removing c does more harm than good
    mySet.remove(natur.ascend(9)); // very good results
    return new Chord(triad, mySet);
  }

  // input: 0 -> C major
  // musthave=[c, g, a], optional=[c, e, g, a]
  // F6 = d+sept
  @Override
  public Chord majorSixth(Natur pitch) {
    return new Chord(Triad.majorSixth(pitch), //
        Stream.of(pitch, pitch.ascend(4), pitch.ascend(7), pitch.ascend(9)).toList()); // C major vs. a minor, try allow pitch + 3
  }

  // minor sixth would have too much in common with major sixth
  // public static Chord minorSixth(int pitch) {
  // return Chord.createFrom(new Integer[] { pitch + 0, pitch + 7, pitch + 9 }, //
  // Natur.fromPitch(new Integer[] { pitch + 0, pitch + 3, pitch + 7, pitch + 9 })); // C major vs. a minor, try allow pitch + 3
  // }
  // input: 2 -> [d, f, ga, b]
  // musthave=[d, f, ga, b], optional=[d, f, ga, b], prohibit=[c, cd, de, e, fg, g, a, ab]
  @Override
  public Chord diminishedSeventh(Natur pitch) {
    return new Chord(Triad.diminishedSeventh(pitch));
  }
}
