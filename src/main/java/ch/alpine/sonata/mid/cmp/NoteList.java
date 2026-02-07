// code by jph
package ch.alpine.sonata.mid.cmp;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import ch.alpine.sonata.Note;
import ch.alpine.tensor.ext.Integers;

public class NoteList implements Iterable<NoteEntry> {
  private List<NoteEntry> list = new LinkedList<>();
  public NavigableSet<Integer> navigableSet = new TreeSet<>();

  public void addEntry(int prev_ticks, Note prev_note, int next_ticks, Note next_note) {
    list.add(new NoteEntry(prev_ticks, prev_note, next_ticks, next_note));
    navigableSet.add(prev_ticks);
    navigableSet.add(prev_ticks + prev_note.ticks());
  }

  void assertConsistent() {
    int ticks = 0;
    for (NoteEntry noteEntry : this) {
      Integers.requireLessEquals(ticks, noteEntry.next_ticks);
      ticks = noteEntry.next_ticks + noteEntry.next_note.ticks();
    }
  }

  @Override
  public Iterator<NoteEntry> iterator() {
    return list.iterator();
  }
}
