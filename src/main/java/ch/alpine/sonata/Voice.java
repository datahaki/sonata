// code by jph
package ch.alpine.sonata;

import java.io.Serializable;
import java.util.IntSummaryStatistics;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Stream;

import ch.alpine.midkit.MidiInstrument;
import sys.dat.MapOperations;
import sys.mat.IntRange;
import sys.mat.Ratio;

public class Voice implements Iterable<Entry<Integer, Torrent>>, Serializable {
  public static final int MOTIF_LENGTH_MIN = 4;
  // ---
  public MidiInstrument midiInstrument = MidiInstrument.GRAND_PIANO;
  public NavigableMap<Integer, Torrent> navigableMap = new TreeMap<>();
  public Clef clef = Clef.TREBLE;
  /** for MIDI playback; instrument identifier of voice */
  // public int channel = 0;
  /** location of {@link Ornament}s */
  public NavigableMap<Integer, Ornament> shake = new TreeMap<>();
  /** location of {@link Dynamic} */
  public NavigableMap<Integer, Dynamic> press = new TreeMap<>();
  /** location of figured bass */
  public NavigableMap<Integer, FiguredBass> fbass = new TreeMap<>();
  /** location of lyrics */
  public NavigableMap<Integer, String> lyric = new TreeMap<>();
  /** location of motifs */
  public NavigableMap<Integer, Integer> motif = new TreeMap<>();

  /** @return ticks of final sound of voice */
  public int ticks() {
    Entry<Integer, Torrent> entry = navigableMap.lastEntry();
    return Objects.isNull(entry) //
        ? 0
        : entry.getKey() + entry.getValue().ticks();
  }

  public void takesHeaderFrom(Voice voice) {
    clef = voice.clef;
    midiInstrument = voice.midiInstrument;
    // channel = voice.channel;
  }

  public Voice extract(final int beg, final int end) {
    Voice voice = new Voice();
    voice.takesHeaderFrom(this);
    {
      Entry<Integer, Torrent> myEntry = getEntryOver(beg);
      if (myEntry != null && myEntry.getKey() < beg)
        voice.navigableMap.put(0, myEntry.getValue().subTorrent(beg - myEntry.getKey()).cloneUntil(end - beg));
    }
    MapOperations.translate(navigableMap.subMap(beg, end), -beg, voice.navigableMap);
    MapOperations.translate(shake.subMap(beg, end), -beg, voice.shake);
    MapOperations.translate(press.subMap(beg, end), -beg, voice.press);
    MapOperations.translate(fbass.subMap(beg, end), -beg, voice.fbass);
    MapOperations.translate(lyric.subMap(beg, end), -beg, voice.lyric);
    MapOperations.translate(motif.subMap(beg, end), -beg, voice.motif);
    return voice;
  }

  public Entry<Integer, Torrent> getEntryOver(int ticks) {
    Entry<Integer, Torrent> entry = navigableMap.lowerEntry(ticks + 1);
    return entry != null && entry.getKey() + entry.getValue().ticks() <= ticks ? null : entry;
  }

  public Entry<Integer, Note> getHigherEntry(int ticks) {
    Entry<Integer, Torrent> entry = getEntryOver(ticks);
    if (entry != null) {
      Entry<Integer, Note> noteEntry = entry.getValue().getNoteMapAbsolute(entry.getKey()).higherEntry(ticks);
      if (noteEntry != null)
        return noteEntry;
    }
    entry = navigableMap.higherEntry(ticks);
    if (entry != null) {
      Entry<Integer, Note> noteEntry = entry.getValue().getNoteMapAbsolute(entry.getKey()).higherEntry(ticks);
      if (noteEntry != null)
        return noteEntry;
    }
    return null;
  }

  /** use this function to modify pitch or ticks of note
   * 
   * @param ticks
   * @param exact
   * @return */
  public Note getNote(int ticks, boolean exact) {
    Integer myInteger = navigableMap.lowerKey(ticks + 1);
    if (myInteger != null)
      return navigableMap.get(myInteger).getNote(ticks - myInteger, exact);
    return null;
  }

  /** @param ticks
   * @return 0 if ticks is occupied otherwise positive distance to next note ahead */
  public int freeAt(int ticks) {
    Entry<Integer, Torrent> entry = getEntryOver(ticks);
    if (entry != null)
      return 0;
    Integer myInteger = navigableMap.higherKey(ticks);
    return Objects.isNull(myInteger) //
        ? Integer.MAX_VALUE
        : myInteger - ticks;
  }

  public Ratio pitchNotesRatio() {
    IntSummaryStatistics intSummaryStatistics = navigableMap.values().stream() //
        .flatMap(torrent -> torrent.list.stream()) //
        .mapToInt(note -> note.tone().pitch()) //
        .summaryStatistics();
    return Ratio.of(intSummaryStatistics);
  }

  public Ratio ivoryNotesRatio() {
    IntSummaryStatistics intSummaryStatistics = navigableMap.values().stream() //
        .flatMap(torrent -> torrent.list.stream()) //
        .mapToInt(note -> note.tone().ivory().ivory()) //
        .summaryStatistics();
    return Ratio.of(intSummaryStatistics);
  }

  /** @return list of references to all notes in voice */
  public Stream<Note> allNotes() {
    return navigableMap.values().stream() //
        .flatMap(torrent -> torrent.list.stream()); //
  }

  public boolean split(int ticks) {
    Entry<Integer, Torrent> entry = navigableMap.lowerEntry(ticks);
    if (entry != null) {
      Torrent torrent = entry.getValue();
      int into = entry.getKey();
      int index = 0;
      for (Note note : torrent.list) {
        into += note.ticks();
        ++index;
        if (ticks == into) {
          navigableMap.put(entry.getKey(), Torrent.from(torrent.list.subList(0, index)));
          navigableMap.put(ticks, Torrent.from(torrent.list.subList(index, torrent.list.size())));
          return true;
        }
      }
    }
    return false;
  }

  public void spliceInsert(int ticks, Melody melody) {
    Entry<Integer, Torrent> entry = getEntryOver(ticks);
    int local = ticks - entry.getKey();
    Torrent torrent = entry.getValue();
    int index = torrent.getIndexOfNoteAt(local, true);
    torrent.spliceInsert(index, melody, index + 1);
  }

  public Optional<PitchRange> getPitchRange() {
    IntSummaryStatistics intSummaryStatistics = allNotes().mapToInt(note -> note.tone().pitch()).summaryStatistics();
    if (0 < intSummaryStatistics.getCount())
      return Optional.of(new PitchRange(intSummaryStatistics.getMin(), intSummaryStatistics.getMax() + 1));
    return Optional.empty();
  }

  public NavigableMap<Integer, Note> getNoteMapAbsolute() {
    NavigableMap<Integer, Note> navigableMap = new TreeMap<>();
    for (Entry<Integer, Torrent> entry : this)
      navigableMap.putAll(entry.getValue().getNoteMapAbsolute(entry.getKey()));
    return navigableMap;
  }

  public boolean isEmpty(IntRange intRange) {
    return intRange.capMap(getNoteMapAbsolute()).isEmpty();
  }

  @Override
  public Iterator<Entry<Integer, Torrent>> iterator() {
    return navigableMap.entrySet().iterator();
  }

  public void clear() {
    navigableMap.clear();
    shake.clear();
    press.clear();
    fbass.clear();
    lyric.clear();
    motif.clear();
  }

  public void setNote(int ticks, Note note) {
    Entry<Integer, Torrent> entry = getEntryOver(ticks);
    entry.getValue().setNote(ticks - entry.getKey(), note);
  }

  public void plagiarizeFrom(Voice voice) {
    clear();
    for (Entry<Integer, Torrent> entry : voice.navigableMap.entrySet())
      navigableMap.put(entry.getKey(), entry.getValue().cloneTorrent());
    clef = voice.clef;
    midiInstrument = voice.midiInstrument;
    shake.putAll(voice.shake);
    press.putAll(voice.press);
    fbass.putAll(voice.fbass);
    lyric.putAll(voice.lyric);
    motif.putAll(voice.motif);
  }

  private static <Type> void translateContent(int beg, int end, int beg_new, NavigableMap<Integer, Type> navigableMap) {
    ClipboardMap<Type> clipboardMap = new ClipboardMap<>();
    clipboardMap.setContent(beg, navigableMap.subMap(beg, end));
    navigableMap.subMap(beg, end).clear();
    clipboardMap.getContent(beg_new, navigableMap);
  }

  public void translate(int beg, int end, int beg_new) {
    translateContent(beg, end, beg_new, shake);
    translateContent(beg, end, beg_new, press);
    translateContent(beg, end, beg_new, fbass);
    translateContent(beg, end, beg_new, lyric);
    translateContent(beg, end, beg_new, motif);
  }
}
