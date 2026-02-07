// code by jph
package ch.alpine.sonata;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import ch.alpine.tensor.ext.Integers;

/** A heptatonic scale is a musical scale that has seven pitches per octave.
 * 
 * @see https://en.wikipedia.org/wiki/Heptatonic_scale */
public final class HeptatonicScale extends Scale {
  protected static final int SIZE = 7;

  /** A diatonic scale is a {@link HeptatonicScale} that includes
   * five whole steps and two half steps in each octave,
   * in which the two half steps are separated from each other by
   * either two or three whole steps,
   * depending on their position in the scale.
   * This pattern ensures that, in a diatonic scale spanning more than one octave,
   * all the half steps are maximally separated from each other
   * (i.e. separated by at least two whole steps).
   * Wide usage: Western, Indian, Arab music
   * 
   * @see https://en.wikipedia.org/wiki/Diatonic_scale */
  public static HeptatonicScale diatonic(int type) {
    return new HeptatonicScale(CircleFifths.subMap(type - 1, SIZE));
  }

  public static HeptatonicScale of(Map<Diatone, DiatoneAlter> diatoneMap) {
    return new HeptatonicScale(diatoneMap.values().stream() //
        .collect(Collectors.toMap(DiatoneAlter::natur, Function.identity())));
  }

  // ---
  private final Map<Diatone, DiatoneAlter> diatoneMap = new EnumMap<>(Diatone.class);
  // TODO TPF check if there are not crossings!

  private HeptatonicScale(Map<Natur, DiatoneAlter> map) {
    super(map);
    Integers.requireEquals(size(), SIZE);
    map.entrySet().stream() //
        .forEach(entry -> diatoneMap.put(entry.getValue().diatone(), entry.getValue()));
    Integers.requireEquals(diatoneMap.size(), SIZE);
  }

  /** @param ivory non-null
   * @return */
  public Tone getToneFromIvory(Ivory ivory) {
    return Tone.from(ivory, diatoneMap.get(ivory.diatone()).alter());
  }

  /** neutralizes accidentals with respect to scale
   * 
   * @param note
   * @return */
  public Note projectNote(Note note) {
    return new Note(getToneFromIvory(note.tone().ivory()), note.ticks());
  }

  public Torrent projectTorrent(Torrent torrent) {
    Torrent myReturn = new Torrent(torrent.attributes);
    torrent.list.stream() //
        .map(this::projectNote) //
        .forEach(myReturn.list::add); // since return.list is already instantiated
    return myReturn;
  }

  public Map<Diatone, DiatoneAlter> diatoneMap() {
    return Collections.unmodifiableMap(diatoneMap);
  }
  // public HeptatonicScale with(DiatoneAlter diatoneAlter) {
  // return null;
  // }
}
