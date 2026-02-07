// code by jph
package ch.alpine.sonata;

import java.util.List;

/** {@link Torrent} takes stem of voice.
 * The native file format also does not support stems for torrents. */
public class Torrent extends Melody {
  public Attributes attributes;

  /** @param attributes if null, torrent is equivalent to {@link Melody} */
  public Torrent(Attributes attributes) {
    this.attributes = attributes;
  }

  public Torrent() {
    this(new Attributes());
  }

  public static Torrent from(List<Note> list) {
    Torrent torrent = new Torrent();
    torrent.list.addAll(list);
    return torrent;
  }

  public final Torrent standardForm() {
    Torrent torrent = new Torrent(attributes);
    int delta = -first().tone().ivory().octave();
    for (Note note : list)
      torrent.list.add(note.cloneTransposeByOctave(delta));
    return torrent;
  }

  public Torrent cloneTorrent() {
    return cloneUntil(ticks());
  }

  public Torrent cloneUntil(int end) {
    Torrent torrent = new Torrent(attributes);
    int ticks = 0;
    for (Note note : list) {
      if (ticks < end)
        torrent.list.add(new Note(note.tone(), Math.min(note.ticks(), end - ticks)));
      ticks += note.ticks();
    }
    // if (myTorrent.myList.isEmpty()) // TODO NOTATION this is used for fugal plugin
    // new RuntimeException("torrent clone empty").printStackTrace();
    return torrent;
  }

  public Torrent subTorrent(int beg) {
    Torrent torrent = new Torrent(attributes);
    int ticks = 0;
    int index = 0;
    // if (0<=beg)
    for (Note note : list) {
      if (beg == ticks) {
        torrent.list.addAll(list.subList(index, list.size()));
        return torrent;
      } else //
      if (ticks < beg && beg < ticks + note.ticks()) {
        torrent.list.add(new Note(note.tone(), ticks + note.ticks() - beg));
        torrent.list.addAll(list.subList(index + 1, list.size()));
        return torrent;
      }
      ticks += note.ticks();
      ++index;
    }
    System.out.println("ticks " + ticks() + " (requested from " + beg + ")");
    throw new RuntimeException("subtorrent empty");
  }
}
