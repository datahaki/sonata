// code by jph
package ch.alpine.sonata.scr;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.OptionalInt;
import java.util.TreeMap;
import java.util.stream.Collectors;

import ch.alpine.sonata.Attributes;
import ch.alpine.sonata.ScoreEntry;
import ch.alpine.sonata.Torrent;
import ch.alpine.sonata.Voice;

public class Repertoire {
  /** torrents in map are guaranteed to have at least 4 notes */
  public final NavigableMap<ScoreEntry, Torrent> navigableMap = new TreeMap<>();
  public final List<Torrent> list = new ArrayList<>();
  public final int modus_ticks;

  public Repertoire(int modus_ticks) {
    this.modus_ticks = modus_ticks;
  }

  public Entry<ScoreEntry, Torrent> getEntryOver(int mouse_ticks, int voice) {
    for (Entry<ScoreEntry, Torrent> entry : navigableMap.entrySet()) {
      ScoreEntry myScoreEntry = entry.getKey();
      if (myScoreEntry.voice() == voice && //
          myScoreEntry.ticks() <= mouse_ticks && //
          mouse_ticks < myScoreEntry.ticks() + entry.getValue().ticks())
        return entry;
    }
    return null;
  }

  public boolean isSynced(int ticks, int entry_ticks) {
    return (ticks - entry_ticks) % modus_ticks == 0;
  }

  public boolean isSyncedAny(int ticks) {
    return navigableMap.keySet().stream().anyMatch(myScoreEntry -> isSynced(ticks, myScoreEntry.ticks()));
  }

  public List<Torrent> getSynced(int ticks) {
    return navigableMap.entrySet().stream() //
        .filter(myEntry -> isSynced(ticks, myEntry.getKey().ticks())) //
        .map(myEntry -> myEntry.getValue()) //
        .collect(Collectors.toList());
  }

  public int size() {
    return navigableMap.size();
  }

  void append(ScoreEntry scoreEntry, Torrent torrent) {
    torrent.attributes = new Attributes(size());
    navigableMap.put(scoreEntry, torrent);
    list.add(torrent);
  }

  public Torrent getByIndex(int index) {
    Torrent torrent = list.get(index);
    if (torrent.attributes.getIndex() != index)
      throw new RuntimeException();
    return torrent;
  }

  public void print() {
    for (Entry<ScoreEntry, Torrent> entry : navigableMap.entrySet()) {
      Torrent torrent = entry.getValue();
      System.out.println(entry.getKey() + " #" + torrent.attributes.getIndex() + " " + torrent.list);
    }
  }

  // ---------------------------------------------------------------------------------------------------------------
  /** allows to apply repertoire of myScore to myVoices
   * 
   * @param myDst with repertoire
   * @param voices */
  @Deprecated
  public void factor(List<Voice> voices) {
    for (Voice voice : voices)
      factor(voice);
  }

  // splits torrents into smaller torrents if constitute note material
  @Deprecated
  public void factor(Voice voice) {
    UniformFlatten.applyTo(voice);
    for (Entry<ScoreEntry, Torrent> entry : navigableMap.entrySet())
      for (int ticks : new LinkedList<>(voice.navigableMap.keySet())) { // preserve old key set because map is altered
        Torrent torrent = voice.navigableMap.get(ticks);
        if (!torrent.attributes.isAppended())
          factor(ticks, torrent, entry.getKey().ticks(), entry.getValue(), voice.navigableMap);
      }
  }

  @Deprecated
  private void factor(int ticks, Torrent torrent, int modus, Torrent pattern, NavigableMap<Integer, Torrent> navigableMap) {
    String myStringT = TorrentEncoder.encodeIvoryDelta(torrent, true, true);
    String myStringI = TorrentEncoder.encodeIvoryDelta(pattern, true, true);
    int offset = -1;
    do {
      offset = myStringT.indexOf(myStringI.substring(0, 3), offset + 1);
    } while (0 <= offset && !isSynced(ticks + torrent.ticksBeforeNote(offset), modus)); // not efficient
    // condition: material needs to be offset compatible
    if (0 <= offset) { // split existing torrent
      int cutoff = 3; // extend found instance as far as possible
      while (cutoff < pattern.list.size() && myStringT.indexOf(myStringI.substring(0, cutoff), offset) == offset)
        ++cutoff;
      Torrent iterate;
      if (0 < offset) {
        iterate = Torrent.from(torrent.list.subList(0, offset));
        navigableMap.put(ticks, iterate); // overwrite
      }
      iterate = Torrent.from(torrent.list.subList(offset, offset + cutoff));
      iterate.attributes = pattern.attributes;
      navigableMap.put(ticks + torrent.ticksBeforeNote(offset), iterate);
      if (offset + cutoff < torrent.list.size()) {
        int split = ticks + torrent.ticksBeforeNote(offset + cutoff);
        iterate = Torrent.from(torrent.list.subList(offset + cutoff, torrent.list.size()));
        navigableMap.put(split, iterate); // overwrite
        factor(split, iterate, modus, pattern, navigableMap);
      }
    }
  }

  public static int coverage(Score score) {
    return coverage(score.voices, 0, score.ticks());
  }

  /** counts ticks between beg and end for all voices that are covered with notes from the repertoire
   * 
   * @param voices
   * @param beg
   * @param end
   * @return */
  public static int coverage(List<Voice> voices, int beg, int end) {
    int myInt = 0;
    for (Voice voice : voices)
      for (int c1 = beg; c1 < end; ++c1) {
        Entry<Integer, Torrent> entry = voice.getEntryOver(c1);
        if (entry != null) {
          Torrent torrent = entry.getValue();
          int overlap = Math.min(entry.getKey() + torrent.ticks() - c1, end - c1);
          if (torrent.attributes.isAppended())
            myInt += overlap;
          c1 += overlap - 1; // the last -1 compensates the ++c1 in the next iteration
        }
      }
    return myInt;
  }

  public OptionalInt getMaximumTicks() {
    return navigableMap.values().stream() //
        .mapToInt(Torrent::ticks) //
        .max();
  }
}
