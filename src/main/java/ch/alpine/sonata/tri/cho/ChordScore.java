// code by jph
package ch.alpine.sonata.tri.cho;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ch.alpine.bridge.lang.SI;
import ch.alpine.midkit.MidiInstrument;
import ch.alpine.sonata.Attributes;
import ch.alpine.sonata.Clef;
import ch.alpine.sonata.Division;
import ch.alpine.sonata.KeySignature;
import ch.alpine.sonata.Natur;
import ch.alpine.sonata.Note;
import ch.alpine.sonata.Torrent;
import ch.alpine.sonata.Voice;
import ch.alpine.sonata.scr.Score;
import ch.alpine.sonata.scr.Scores;
import ch.alpine.sonata.tri.Triad;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.ext.Integers;
import sys.mat.IntRange;

public abstract class ChordScore {
  // TODO TPF should be final!
  protected int pitch_mean = 60;
  public final int channel;
  final Score myScore = Scores.create(0);
  List<Voice> voicesHi = new ArrayList<>(); // treble
  List<Voice> voicesLo = new ArrayList<>(); // bass clef

  protected ChordScore(int channel) {
    this.channel = channel;
  }

  public void setPitchMean(int mean) {
    pitch_mean = Integers.clip(60 - 12, 60 + 12).applyAsInt(mean);
    if (pitch_mean != mean)
      System.out.println("truncating pitch_mean from " + mean);
  }

  public void initFromScore(Score score) {
    setMetric(score.quarter, score.division, score.bpm);
  }

  private void setMetric(int quarter, Division myDivision, Scalar bpm) {
    myScore.quarter = quarter;
    myScore.division = myDivision;
    myScore.bpm = bpm;
  }

  private void setBpm(Scalar bpm) {
    myScore.bpm = bpm;
  }

  /** @param ticks
   * @param mySet
   * @param duration
   * @param velocity is between 0 and 127 */
  public abstract void put(int ticks, int duration, Set<Natur> mySet, int velocity);

  protected void append(List<Torrent> list, int ticks, int duration) {
    int _voice = 0;
    for (Torrent torrent : list) {
      final boolean treble = 0 <= torrent.first().tone().pitch();
      final List<Voice> voices = treble ? voicesHi : voicesLo;
      boolean myBoolean = false;
      for (; _voice < voices.size(); ++_voice) { // find voice that can insert
        Voice voice = voices.get(_voice);
        int free = voice.freeAt(ticks);
        // System.out.println(voice + " free@" + ticks + " " + free);
        if (duration <= free) {
          voice.navigableMap.put(ticks, torrent);
          myBoolean = true;
          ++_voice;
          break;
        }
      }
      if (!myBoolean) {
        Voice voice = new Voice();
        voice.navigableMap.put(ticks, torrent);
        voice.clef = treble ? Clef.TREBLE : Clef.BASS;
        voice.midiInstrument = MidiInstrument.GRAND_PIANO;
        voices.add(voice);
        ++_voice;
      }
    }
  }

  /** @param list
   * @param pitch of note to add
   * @param ticks
   * @param velocity no larger than 127 */
  protected void add(List<Torrent> list, int pitch, int ticks, double velocity) {
    final int vel = (int) Math.round(velocity);
    if (0 < vel) {
      final int rgb = 255 - 2 * vel;
      // System.out.println("pitch=" + pitch);
      Note note = new Note(myScore.keySignature.dodecatonicScale().getTone(pitch), ticks);
      Attributes attributes = new Attributes(rgb);
      attributes.velocity = vel;
      Torrent torrent = new Torrent(attributes);
      torrent.list.add(note);
      list.add(torrent);
    }
  }

  public final Score getScore() {
    myScore.voices.clear();
    myScore.voices.addAll(voicesHi);
    myScore.voices.addAll(voicesLo);
    return myScore;
  }

  // ---
  // TODO TPF move function?
  public static Score testScale(ChordScore chordScore, int velocity) {
    chordScore.setBpm(SI.PER_MINUTE.quantity(120));
    List<Natur> list = new LinkedList<>();
    list.addAll(KeySignature.fromType(0).diatonicScale().map().keySet());
    list.addAll(KeySignature.fromType(3).diatonicScale().map().keySet());
    list.addAll(KeySignature.fromType(-3).diatonicScale().map().keySet());
    list.add(list.get(0));
    int ticks = 0;
    for (Natur natur : list) {
      Set<Natur> set = new HashSet<>();
      set.add(natur);
      chordScore.put(ticks, 1, set, velocity);
      ++ticks;
    }
    return chordScore.getScore();
  }

  // TODO TPF move function?
  public static Score testChord(ChordScore chordScore, int velocity) {
    for (int c0 : IntRange.positive(60 + 12 + 1))
      chordScore.put(c0, 1, Triad.major(Natur.fromPitch(c0)).set(), velocity);
    chordScore.setBpm(SI.PER_MINUTE.quantity(80));
    return chordScore.getScore();
  }
}
