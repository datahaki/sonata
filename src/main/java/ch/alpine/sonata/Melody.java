// code by jph
package ch.alpine.sonata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.IntSummaryStatistics;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Stream;

import sys.mat.IntRange;

public class Melody implements Comparable<Melody>, Iterable<Note>, Serializable {
  // FIXME NOTATION should be final
  public List<Note> list = new ArrayList<>();

  public NavigableMap<Integer, Note> getNoteMapAbsolute(int ticks) {
    NavigableMap<Integer, Note> navigableMap = new TreeMap<>();
    for (Note note : list) {
      navigableMap.put(ticks, note);
      ticks += note.ticks();
    }
    return navigableMap;
  }

  public final void transposeByOctave(int octaves) {
    for (Note note : list)
      note.setTone(note.tone().transposeByOctaves(octaves));
  }

  /** @param ticks
   * @param exact
   * @return note at position of ticks */
  public Note getNote(int ticks, boolean exact) {
    int myInt = 0;
    for (Note note : list) {
      if (myInt <= ticks && ticks < myInt + note.ticks())
        if (myInt == ticks || !exact) // this has not been tested much ?
          return note;
      myInt += note.ticks();
    }
    return null;
  }

  public void spliceInsert(int ante, Melody melody, int post) {
    List<Note> copy = new ArrayList<>();
    copy.addAll(list.subList(0, ante));
    copy.addAll(melody.list);
    copy.addAll(list.subList(post, list.size()));
    list = copy;
  }

  public Relation getRelation0(int ticks) {
    if (ticks < 0 || ticks() <= ticks)
      return Relation.nada;
    Note note = getNote(ticks, false);
    return Objects.isNull(getNote(ticks, true)) //
        ? Relation.hold(note.tone().pitch())
        : Relation.live(note.tone().pitch());
  }

  public int getIndexOfNoteAt(int ticks, boolean exact) {
    int index = 0;
    int myInt = 0;
    for (Note note : list) {
      if (myInt <= ticks && ticks < myInt + note.ticks())
        if (myInt == ticks || !exact) // this has not been tested much ?
          return index;
      ++index;
      myInt += note.ticks();
    }
    return -1;
  }

  /** @return accumulation of ticks of all notes */
  public int ticks() {
    return list.stream() //
        .mapToInt(note -> note.ticks()).sum();
  }

  /** @param index
   * @return ticks in torrent before note myList.get(index). For instance, for index=0 returns 0. */
  public int ticksBeforeNote(int index) {
    int ticks = 0;
    for (Note note : list)
      if (0 <= --index)
        ticks += note.ticks();
      else
        break;
    return ticks;
  }

  /** @return reference to first note in torrent */
  public final Note first() {
    return list.get(0);
  }

  /** @return reference to last note in torrent */
  public final Note last() {
    return list.get(list.size() - 1);
  }

  @Deprecated
  public double meanPitch() {
    int num = 0;
    int den = list.isEmpty() ? 1 : list.size();
    // Ratio ratio = new Ratio(0, );
    for (Note note : list)
      num += note.tone().pitch();
    return num / (double) den;
  }

  /** @return closed interval, i.e. membership is determined by IntRange::contains */
  public PitchRange getPitchRange() {
    IntSummaryStatistics intSummaryStatistics = //
        list.stream().mapToInt(note -> note.tone().pitch()).summaryStatistics();
    if (intSummaryStatistics.getCount() == 0)
      new RuntimeException("melody empty").printStackTrace();
    return new PitchRange(intSummaryStatistics.getMin(), intSummaryStatistics.getMax() + 1);
  }

  public IntRange getIvoryRange() {
    IntSummaryStatistics intSummaryStatistics = //
        list.stream().mapToInt(note -> note.tone().ivory().ivory()).summaryStatistics();
    if (intSummaryStatistics.getCount() == 0)
      new RuntimeException("melody empty").printStackTrace();
    return new IntRange(intSummaryStatistics.getMin(), intSummaryStatistics.getMax() + 1);
  }

  /** the durations of the last matching note has to match.
   * 
   * @param melody
   * @return */
  public boolean isStartOf(Melody melody) {
    int last = list.size() - 1;
    if (list.isEmpty())
      return true;
    boolean myBoolean = last < melody.list.size();
    if (myBoolean) {
      for (int c0 : IntRange.positive(last))
        myBoolean &= list.get(c0).equals(melody.list.get(c0));
      if (myBoolean) {
        Note note0 = list.get(last);
        Note note1 = melody.list.get(last);
        myBoolean &= note0.tone().pitch() == note1.tone().pitch() && note0.tone().ivory().equals(note1.tone().ivory());
        myBoolean &= note0.ticks() <= note1.ticks();
      }
    }
    return myBoolean;
  }

  public void setNote(int ticks, Note note) {
    int index = 0;
    int past = 0;
    for (Note myN : list) {
      if (past == ticks)
        break;
      past += myN.ticks();
      ++index;
    }
    list.set(index, note);
  }

  public Stream<Note> noteStream() {
    return list.stream();
  }

  @Override
  public int hashCode() {
    return list.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    Melody melody = (Melody) object;
    return list.equals(melody.list);
  }

  @Override
  public Iterator<Note> iterator() {
    return list.iterator();
  }

  @Override
  public int compareTo(Melody melody) {
    int cmp;
    int min = Math.min(list.size(), melody.list.size());
    for (int c0 : IntRange.positive(min)) {
      cmp = NoteComparator.pitchBased.compare(list.get(c0), melody.list.get(c0));
      if (cmp != 0)
        return cmp;
    }
    return Integer.compare(list.size(), melody.list.size());
  }
}
