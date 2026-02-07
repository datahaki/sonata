// code by jph
package ch.alpine.sonata;

import java.util.Arrays;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import ch.alpine.tensor.ext.Integers;
import sys.mat.IntRange;

/** ScoreArray is an alternative representation of the voices in the score
 * each pitch of a {@link Note} is stored in an array of type {@link Integer},
 * each hit of a Note is indicated as a true in an array of type {@link Boolean}. */
public class ScoreArray implements Comparable<ScoreArray> {
  /** for scores with no voices */
  private final int ticks_total;
  private final Pitch[][] myPitch;

  public ScoreArray(int voice, int ticks_total) {
    if (ticks_total < 0)
      throw new RuntimeException("ticks negative " + ticks_total);
    this.ticks_total = ticks_total;
    myPitch = new Pitch[voice][ticks_total];
  }

  public final int voices() {
    return myPitch.length;
  }

  public final int ticks() {
    return ticks_total;
  }

  public ScoreArray completeClone() {
    return extract(0, ticks_total);
  }

  public ScoreArray extract(final int beg, int end) {
    Integers.requirePositiveOrZero(beg);
    IntRange intRange = new IntRange(beg, end);
    // GlobalSwitch.assert_(beg <= end);
    int width = intRange.getWidth();
    int numel = Math.min(myPitch[0].length - beg, width);
    ScoreArray scoreArray = new ScoreArray(voices(), width);
    try {
      for (int voice : IntRange.positive(voices()))
        System.arraycopy(myPitch[voice], beg, scoreArray.myPitch[voice], 0, numel);
    } catch (Exception exception) {
      System.out.println("-----------------------");
      System.out.println("beg,end: " + beg + " " + end);
      System.out.println("avail v=" + myPitch.length + " t=" + myPitch[0].length);
      System.out.println("width " + width);
      System.out.println("numel " + numel);
      exception.printStackTrace();
    }
    return scoreArray;
  }

  /** @param list list of durations
   * @return */
  public ScoreArray expand(List<Integer> list) {
    Integers.requireEquals(list.size(), ticks_total);
    ScoreArray scoreArray = new ScoreArray(voices(), list.stream() //
        .mapToInt(i -> i).sum());
    int ofs = 0;
    for (int ticks : IntRange.positive(ticks_total)) {
      int durat = list.get(ticks);
      for (int voice : IntRange.positive(voices())) {
        Pitch myValue = myPitch[voice][ticks];
        if (Objects.nonNull(myValue)) {
          scoreArray.set(voice, ofs, myValue.pitch(), myValue.isHits());
          for (int count = 1; count < durat; ++count)
            scoreArray.set(voice, ofs + count, myValue.pitch(), false);
        }
      }
      ofs += durat;
    }
    return scoreArray;
  }

  // ---
  public ScoreArray overwrite(int offset, ScoreArray scoreArray) {
    if (voices() != scoreArray.voices())
      throw new RuntimeException("not same number of voices: " + voices() + " vs. " + scoreArray.voices());
    if (ticks_total < offset + scoreArray.ticks_total)
      throw new RuntimeException("not enough space to insert: " + ticks_total + " < " + (offset + scoreArray.ticks_total));
    for (int voice : IntRange.positive(voices()))
      System.arraycopy(scoreArray.myPitch[voice], 0, myPitch[voice], offset, scoreArray.ticks_total);
    return this;
  }

  public ScoreArray concat(ScoreArray scoreArray) {
    return extract(0, ticks_total + scoreArray.ticks_total).overwrite(ticks_total, scoreArray);
  }

  public ScoreArray concatSafe(ScoreArray scoreArray) {
    return scoreArray.ticks_total == 0 //
        ? completeClone()
        : concat(scoreArray).burned(ticks_total);
  }

  public int set(final int voice, int ticks, Iterable<Note> torrent) {
    for (Note note : torrent) {
      set(voice, ticks, note);
      ticks += note.ticks();
    }
    return ticks;
  }

  public void set(final int voice, int ticks, Note note) {
    set(voice, ticks, note.tone().pitch(), true);
    for (int count = 1; count < note.ticks(); ++count)
      set(voice, ticks + count, note.tone().pitch(), false);
  }

  public void set(final int voice, int ticks, Integer myInteger, Boolean myBoolean) {
    if (Objects.isNull(myInteger)) {
      myPitch[voice][ticks] = null;
      if (Objects.nonNull(myBoolean))
        throw new RuntimeException("nope");
    } else
      myPitch[voice][ticks] = Pitch.from(myInteger, myBoolean);
  }

  public void clear(int voice, int ticks, int length) {
    for (int count : IntRange.positive(length))
      myPitch[voice][ticks + count] = null;
  }

  public Pitch getPitch(int voice, int ticks) {
    return 0 <= voice //
        && voice < voices() //
        && 0 <= ticks //
        && ticks < ticks_total //
            ? myPitch[voice][ticks]
            : null;
  }

  public Pitch getPitchUnsafe(int voice, int ticks) {
    return myPitch[voice][ticks];
  }

  public Integer getInteger(int voice, int ticks) {
    Pitch myPitch = getPitch(voice, ticks);
    return myPitch == null ? null : myPitch.pitch();
  }

  protected Boolean getBoolean(int voice, int ticks) {
    Pitch myPitch = getPitch(voice, ticks);
    return myPitch == null ? null : myPitch.isHits();
  }

  public Relation getRelation0(int voice, int ticks) {
    Pitch myPitch = getPitch(voice, ticks);
    return myPitch == null ? Relation.nada : (myPitch.isHits() ? Relation.live(myPitch.pitch()) : Relation.hold(myPitch.pitch()));
  }

  /** counts the consecutive rests in voice to the right starting from ticks_offset.
   * 
   * @param voice
   * @param ticks_offset
   * @return */
  public int freeFrom(final int voice, final int ticks_offset) {
    int ticks = ticks_offset;
    for (; ticks < ticks_total; ++ticks)
      if (myPitch[voice][ticks] != null)
        break;
    return ticks - ticks_offset;
  }

  // ---
  /** @return map from ticks to number of hits at ticks */
  public NavigableSet<Integer> getHits(final int beg, int end) {
    return getHits(beg, end, IntRange.positive(voices()));
  }

  public NavigableSet<Integer> getHits(final int beg, int end, IntRange intRange) {
    Integers.requirePositiveOrZero(beg);
    NavigableSet<Integer> navigableSet = new TreeSet<>();
    for (int ticks : new IntRange(beg, end).intersect(0, ticks_total))
      for (int voice : intRange)
        if (myPitch[voice][ticks] != null && myPitch[voice][ticks].isHits()) {
          navigableSet.add(ticks);
          break;
        }
    return navigableSet;
  }

  public NavigableSet<Integer> getHits() {
    return getHits(0, ticks_total);
  }

  public NavigableSet<ScoreEntry> getHitEntries() {
    NavigableSet<ScoreEntry> navigableSet = new TreeSet<>();
    for (int voice : IntRange.positive(voices()))
      for (int ticks : IntRange.positive(ticks_total)) {
        Pitch nyPitch = myPitch[voice][ticks];
        if (nyPitch != null && nyPitch.isHits())
          navigableSet.add(new ScoreEntry(ticks, voice));
      }
    return navigableSet;
  }

  /** @return map from ticks to number of alterations at ticks */
  public NavigableSet<Integer> getAlts(int beg, int end) {
    NavigableSet<Integer> navigableSet = new TreeSet<>();
    // = Math.min(Math.max(0, beg), ticks_total); ticks < Math.min(end, ticks_total); ++ticks
    for (int ticks : new IntRange(beg, end).intersect(0, ticks_total)) {
      int count = 0;
      for (int voice = 0; voice < voices(); ++voice)
        if (ticks == 0 || myPitch[voice][ticks - 1] == null)
          count += myPitch[voice][ticks] != null ? 1 : 0; // previous was pause and now there is a note
        else
          count += myPitch[voice][ticks] == null || myPitch[voice][ticks].isHits() ? 1 : 0; // previous was NOT pause and now there is a pause or another hit
      if (0 < count)
        navigableSet.add(ticks); // TODO NOTATION can make more efficient!
    }
    return navigableSet;
  }

  public ScoreArray burned() {
    return burned(0);
  }

  /** ensures that at ticks=0 all notes are hits
   * 
   * @return */
  ScoreArray burned(int ticks) {
    if (0 < ticks_total)
      for (int voice = 0; voice < voices(); ++voice)
        if (myPitch[voice][ticks] != null)
          myPitch[voice][ticks] = myPitch[voice][ticks].withHits( //
              myPitch[voice][ticks].isHits() || //
                  (0 == ticks || !myPitch[voice][ticks].equalsPitch(myPitch[voice][ticks - 1])));
    return this;
  }

  /** @param voice
   * @param ticks
   * @param delta
   * @return careful when delta is negative */
  public int getDuration(final int voice, final int ticks, int delta) {
    int count = 0;
    while (0 <= ticks + count //
        && ticks + count < ticks_total //
        && myPitch[voice][ticks + count] != null //
        && myPitch[voice][ticks].pitch() == myPitch[voice][ticks + count].pitch() && (count == 0 || !myPitch[voice][ticks + count].isHits()))
      count += delta;
    return count;
  }

  @Deprecated
  boolean[] getFree() {
    boolean[] free = new boolean[ticks_total];
    for (int ticks = 0; ticks < ticks_total; ++ticks)
      for (int voice = 0; voice < voices(); ++voice)
        if (myPitch[voice][ticks] == null) {
          free[ticks] = true;
          break;
        }
    return free;
  }

  public boolean hasFreeAt(int ticks) {
    for (int voice = 0; voice < voices(); ++voice)
      if (myPitch[voice][ticks] == null)
        return true;
    return false;
  }

  /** @param voice
   * @param ticks
   * @param count number of consecutive (i.e. without intermediate rest) notes backward
   * @return onset of note, or first rest if no notes are available, or 0 when reaching the start */
  public int ticksOfNoteBack(final int voice, int ticks, int count) {
    while (0 < count && 0 < ticks) {
      --ticks;
      if (myPitch[voice][ticks] == null)
        break;
      if (myPitch[voice][ticks].isHits())
        --count;
    }
    return ticks;
  }

  public String booleanString(int voice, int ticks, int width) {
    char[] myChar = new char[width];
    for (int ofs = 0; ofs < width; ++ofs) {
      Pitch myTemp = myPitch[voice][ticks + ofs];
      if (myTemp == null)
        myChar[ofs] = '_';
      else
        myChar[ofs] = myTemp.isHits() ? 'h' : '-';
    }
    return new String(myChar);
  }

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder("ScoreArray, ticks=" + ticks_total + "\n     ");
    Set<Integer> set = getHits();
    for (int ticks = 0; ticks < ticks_total; ++ticks)
      stringBuilder.append(set.contains(ticks) ? " \\/  " : "     ");
    stringBuilder.append("\n");
    for (int voice = 0; voice < voices(); ++voice) {
      stringBuilder.append(String.format("v=%d  ", voice));
      // myStringBuffer.append(String.format("v=%d%8s ", c0, clef[c0]));
      for (int ticks = 0; ticks < ticks_total; ++ticks) {
        // System.out.println(myInteger[c0][c1] + " " + myBoolean[c0][c1]);
        stringBuilder.append(myPitch[voice][ticks] == null ? "[  " : String.format("%3d", myPitch[voice][ticks].pitch()));
        stringBuilder.append(myPitch[voice][ticks] == null ? "] " : (myPitch[voice][ticks].isHits() ? ". " : "  "));
      }
      if (voice + 1 < voices())
        stringBuilder.append("\n");
    }
    return stringBuilder.toString();
  }

  @Override
  public int hashCode() {
    int value = ticks_total + 1;
    value *= voices() + 1;
    for (int voice = 0; voice < voices(); ++voice)
      for (int ticks = 0; ticks < ticks_total; ++ticks)
        value += myPitch[voice][ticks] == null ? 47 : myPitch[voice][ticks].hashCode();
    return value;
  }

  private boolean arraysEquals(ScoreArray scoreArray) {
    for (int voice = 0; voice < voices(); ++voice)
      if (!Arrays.equals(myPitch[voice], scoreArray.myPitch[voice]))
        return false;
    return true;
  }

  @Override
  public boolean equals(Object object) {
    ScoreArray scoreArray = (ScoreArray) object;
    return voices() == scoreArray.voices() //
        && arraysEquals(scoreArray);
  }

  @Override
  public int compareTo(ScoreArray scoreArray) { // TODO NOTATION extract function to sort tower
    int ticks_max = Math.max(ticks_total, scoreArray.ticks_total);
    int voice_max = Math.max(voices(), scoreArray.voices());
    for (int ticks = 0; ticks < ticks_max; ++ticks)
      for (int voice = 0; voice < voice_max; ++voice) {
        Pitch pitch0 = getPitch(voice, ticks);
        Pitch pitch1 = scoreArray.getPitch(voice, ticks);
        int int0 = pitch0 == null ? -1000 : pitch0.toCompareInt();
        int int1 = pitch1 == null ? -1000 : pitch1.toCompareInt();
        int cmp = Integer.compare(int0, int1);
        if (cmp != 0)
          return cmp;
      }
    int cmp = Integer.compare(ticks_total, scoreArray.ticks_total);
    if (cmp != 0)
      return cmp;
    return Integer.compare(voices(), scoreArray.voices());
  }
}
