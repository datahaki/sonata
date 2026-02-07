// code by jph
package ch.alpine.sonata;

import java.util.Comparator;

public enum NoteComparator {
  ;
  /** comparison between notes is only based on pitch and ticks, but not on ivory */
  public static final Comparator<Note> pitchBased = new Comparator<>() {
    @Override
    public int compare(Note note1, Note note2) {
      int cmp = note1.tone().pitch() - note2.tone().pitch();
      return cmp == 0 //
          ? note1.ticks() - note2.ticks()
          : cmp;
    }
  };
}
