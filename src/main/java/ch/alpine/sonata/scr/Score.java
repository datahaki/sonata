// code by jph
package ch.alpine.sonata.scr;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import ch.alpine.bridge.lang.SI;
import ch.alpine.sonata.Division;
import ch.alpine.sonata.KeyMode;
import ch.alpine.sonata.KeySignature;
import ch.alpine.sonata.Meter;
import ch.alpine.sonata.Note;
import ch.alpine.sonata.ScoreEntry;
import ch.alpine.sonata.Torrent;
import ch.alpine.sonata.Voice;
import ch.alpine.sonata.tri.Triad;
import ch.alpine.tensor.Rational;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.alg.Flatten;
import ch.alpine.tensor.alg.VectorQ;
import ch.alpine.tensor.ext.Serialization;
import ch.alpine.tensor.io.StringScalarQ;
import ch.alpine.tensor.red.Total;
import ch.alpine.tensor.sca.Ceiling;
import ch.alpine.tensor.sca.Sign;
import sys.mat.IntRange;

/** basic information of musical score */
public class Score extends VoiceScore {
  public static final List<Scalar> ATOMS = List.of( //
      RealScalar.of(64).reciprocal(), //
      RealScalar.of(48).reciprocal(), //
      RealScalar.of(32).reciprocal(), //
      RealScalar.of(24).reciprocal(), //
      RealScalar.of(16).reciprocal(), //
      RealScalar.of(8).reciprocal());
  public static final Scalar bpm_fallback = SI.PER_MINUTE.quantity(100);
  // --- HEADER
  public String title = "untitled"; // Wohl-Temperirtes Clavier I - Fuga 01
  public String comment = ""; // bwv 847
  public String staffPartition = ""; // "{1, 1, 2, 1}"
  public KeySignature keySignature = KeySignature.C;
  private KeyMode keyMode = KeyMode.IONIAN;

  public KeyMode keyMode() {
    return keyMode;
  }

  public void setKeyMode(KeyMode keyMode) {
    this.keyMode = Objects.requireNonNull(keyMode);
  }

  public boolean isKeyModeValid = true;
  public boolean hasDiatonicPrecision = true;
  public int quarter = 2; // ticks per quarter
  public Scalar atom = Rational.of(1, 32);
  public Division division = Division.FALLBACK;
  public int period = 1;
  /** bpm = quarters per minute */
  public Scalar bpm = bpm_fallback;
  // ---
  /** The character | separates multiple voices (Musedata standard), see choral256_01 for instance. */
  /** values are labels from IntrinsicFormat.absolute */
  public NavigableMap<Integer, Triad> triad = new TreeMap<>();
  // public NavigableMap<Integer, Hepta> hepta = new TreeMap<>();
  public NavigableMap<Integer, String> text = new TreeMap<>();
  @Deprecated // TODO TPF should be provided through inputreader mechanism
  public StringBuilder process = new StringBuilder(); // technical information on how the score was post processed after import

  public void setMetric(Score score) {
    quarter = score.quarter;
    division = score.division;
    bpm = score.bpm;
  }

  public int measure() {
    return division.measure;
  }

  public int measures() {
    return Ceiling.intValueExact(Rational.of(ticks(), measure()));
  }

  public Integer anyEntry(Integer myInteger) {
    myInteger = entry().orElse(myInteger);
    if (!triad.isEmpty())
      myInteger = myInteger == null ? triad.firstKey() : Math.min(triad.firstKey(), myInteger);
    if (!text.isEmpty())
      myInteger = myInteger == null ? text.firstKey() : Math.min(text.firstKey(), myInteger);
    return myInteger;
  }

  public Scalar seconds() {
    return seconds(ticks() - entry().orElse(0));
  }

  /** @param ticks
   * @return duration of ticks in seconds (depends on bpm and quarter) */
  public Scalar seconds(int ticks) {
    return SI.SECONDS.convert(bpm.reciprocal().multiply(Rational.of(ticks, quarter)));
  }

  public Meter getMeter() {
    return Meter.of(quarter, division);
  }

  public Repertoire getRepertoire() {
    Repertoire repertoire = new Repertoire(division.valueAtSafe(period));
    SortedSet<ScoreEntry> set = new TreeSet<>();
    List<NavigableMap<Integer, Note>> noteMaps = new ArrayList<>();
    { // implementation prioritizes by scoreEntry
      int _voice = 0;
      for (Voice voice : voices) {
        noteMaps.add(voice.getNoteMapAbsolute());
        for (int ticks : voice.motif.keySet())
          set.add(new ScoreEntry(ticks, _voice));
        ++_voice;
      }
    }
    for (ScoreEntry scoreEntry : set) {
      int ticks = scoreEntry.ticks();
      int voice = scoreEntry.voice();
      Voice myVoice = voices.get(voice);
      Torrent torrent = new Torrent();
      // System.out.println(""+myVoice.motif);
      int numel = myVoice.motif.get(ticks);
      for (@SuppressWarnings("unused")
      int __ : IntRange.positive(numel)) {
        Note note = noteMaps.get(voice).get(ticks);
        if (note == null)
          break;
        torrent.list.add(note.copy());
        ticks += note.ticks();
      }
      if (Voice.MOTIF_LENGTH_MIN <= torrent.list.size())
        repertoire.append(scoreEntry, torrent);
    }
    return repertoire;
  }

  /** @param beg
   * @param end */
  public void shiftMaps(int beg, int end) {
    for (Voice voice : voices) {
      Scores.shiftMap(voice.shake, beg, end);
      Scores.shiftMap(voice.press, beg, end);
      Scores.shiftMap(voice.lyric, beg, end);
      Scores.shiftMap(voice.fbass, beg, end);
      Scores.shiftMap(voice.motif, beg, end);
    }
    Scores.shiftMap(triad, beg, end);
    // shiftMap(hepta, beg, end);
    Scores.shiftMap(text, beg, end);
  }

  /** @param score is trash afterwards */
  public void appendScore(Score score) {
    final int ticks = ticks();
    score.shiftMaps(0, ticks);
    for (int voice : IntRange.positive(voices())) {
      Voice voice0 = voices.get(voice);
      Voice voice1 = score.voices.get(voice);
      Scores.shiftMap(voice1.navigableMap, 0, ticks);
      voice0.navigableMap.putAll(voice1.navigableMap);
      voice0.shake.putAll(voice1.shake);
      voice0.press.putAll(voice1.press);
      voice0.lyric.putAll(voice1.lyric);
      voice0.fbass.putAll(voice1.fbass);
      voice0.motif.putAll(voice1.motif);
    }
    triad.putAll(score.triad);
    // hepta.putAll(score.hepta);
    text.putAll(score.text);
  }

  public void appendText(int ticks, String string, boolean newLine) {
    text.put(ticks, (text.containsKey(ticks) ? text.get(ticks) + (newLine ? "|" : " ") : "") + string);
  }

  public boolean hasKeyMode(KeyMode keyMode) {
    return isKeyModeValid //
        && this.keyMode.equals(keyMode);
  }

  public void takesHeaderFrom(Score score) { // use voice=0 for adaptive scores
    title = score.title;
    comment = score.comment;
    staffPartition = score.staffPartition;
    keySignature = score.keySignature;
    keyMode = score.keyMode;
    isKeyModeValid = score.isKeyModeValid;
    quarter = score.quarter;
    atom = score.atom;
    division = score.division;
    period = score.period;
    bpm = score.bpm;
  }

  public void plagiarizeFrom(Score score) {
    plagiarizeVoicesFrom(score.voices.iterator());
    // ---
    takesHeaderFrom(score);
    // ---
    triad.clear();
    triad.putAll(score.triad);
    // ---
    text.clear();
    text.putAll(score.text);
  }

  public Tensor getStaffPartition() {
    try {
      Tensor sp = Tensors.fromString(staffPartition);
      if (VectorQ.of(sp) && //
          !StringScalarQ.any(sp) && //
          Flatten.scalars(sp).allMatch(Sign::isPositive)) {
        OptionalInt optionalInt = Scalars.optionalInt(Total.ofVector(sp));
        if (optionalInt.isPresent() && //
            optionalInt.getAsInt() == voices())
          return sp.copy();
      }
    } catch (Exception exception) {
      exception.printStackTrace();
    }
    // System.err.println("staff partition fallback");
    return Array.same(RealScalar.ONE, voices());
  }

  public Score cloneScore() {
    try {
      return Serialization.copy(this);
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }
}
