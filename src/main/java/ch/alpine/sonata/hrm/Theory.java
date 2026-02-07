// code by jph
package ch.alpine.sonata.hrm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import ch.alpine.sonata.Harmony;
import ch.alpine.sonata.KeySignature;
import ch.alpine.sonata.Natur;
import ch.alpine.sonata.tri.Chord;
import ch.alpine.sonata.tri.Triad;
import ch.alpine.tensor.ext.Integers;

/** chords other than covered by Theory are:
 * 
 * 1) efa, ceb 2) cefa, cef / cgb cegb 3) cde fga gab 4) cdb efg 5) def cab 6) fab */
public class Theory implements Iterable<Chord> {
  /** when in doubt */
  public static final Theory uniform = new Theory(new UniformTheory());
  private static final Map<KeySignature, Theory> chromatic = new HashMap<>();
  static {
    for (KeySignature key : KeySignature.values())
      chromatic.put(key, new Theory(new ChromaticTheory(key)));
  }

  public static Theory getChromaticTheory(KeySignature keySignature) {
    return chromatic.get(keySignature);
  }

  // ---
  private Collection<Chord> collection = new LinkedHashSet<>();
  private Map<Triad, Chord> map = new HashMap<>();

  public Theory(AbstractTheory abstractTheory) {
    for (Harmony harmony : Harmony.values()) {
      Natur natur = Natur.fromPitch(harmony.tonic());
      add(abstractTheory.major(natur));
      add(abstractTheory.minor(natur));
      add(abstractTheory.diminished(natur));
      add(abstractTheory.majorSixth(natur));
    }
    for (int type : new int[] { -1, 0, 1 }) // TODO TPF these iterators should be provided by
      add(abstractTheory.diminishedSeventh(Natur.fromPitch(type * 7)));
    for (int type : new int[] { -1, 0, 1, 2 })
      add(abstractTheory.augmented(Natur.fromPitch(type * 7)));
    Integers.requireEquals(collection.size(), Triad.values().length);
  }

  /** @param triad
   * @return non-null */
  public Chord getChord(Triad triad) {
    Chord chord = map.get(triad);
    Objects.requireNonNull(chord);
    return chord;
  }

  private void add(Chord chord) {
    Objects.requireNonNull(chord);
    collection.add(chord);
    map.put(chord.triad(), chord);
    if (collection.size() != map.size())
      throw new RuntimeException("chords are not unique");
  }

  public Collection<Chord> getMusthave(Collection<Natur> set) { // function only used once
    return collection.stream().filter(chord -> chord.triad().set().containsAll(set)).collect(Collectors.toList());
  }

  public Collection<Chord> getCovering(Collection<Natur> set) { // function only used once
    return collection.stream().filter(chord -> chord.covering().containsAll(set)).collect(Collectors.toList());
  }

  @Override
  public Iterator<Chord> iterator() {
    return collection.iterator();
  }

  public int size() {
    return collection.size();
  }
}
