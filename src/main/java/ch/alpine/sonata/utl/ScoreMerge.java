// code by jph
package ch.alpine.sonata.utl;

import java.util.List;

import ch.alpine.midkit.MidiInstrument;
import ch.alpine.sonata.Clef;
import ch.alpine.sonata.DodecatonicScale;
import ch.alpine.sonata.Note;
import ch.alpine.sonata.Pitch;
import ch.alpine.sonata.ScoreArray;
import ch.alpine.sonata.Tone;
import ch.alpine.sonata.Torrent;
import ch.alpine.sonata.Voice;
import ch.alpine.sonata.scr.Score;
import sys.mat.IntRange;

public class ScoreMerge { // TODO TPF please document
  final ScoreArray scoreArray;

  public ScoreMerge(ScoreArray scoreArray) {
    this.scoreArray = scoreArray;
  }

  /** @param score 1) can be empty, 2) */
  public void updateVoices(Score score) {
    updateVoices(score.keySignature.dodecatonicScale(), score.voices);
  }

  private void updateVoices(DodecatonicScale navigableMap, List<Voice> list) {
    int index = 0;
    for (Voice voice : list) {
      Voice temp = getVoice(navigableMap, voice.clef, voice.midiInstrument, index++);
      voice.navigableMap.clear();
      voice.navigableMap.putAll(temp.navigableMap);
    }
  }

  private Voice getVoice(DodecatonicScale navigableMap, Clef clef, MidiInstrument midiInstrument, int index) {
    Voice voice = new Voice();
    voice.clef = clef;
    voice.midiInstrument = midiInstrument;
    boolean sticky = false;
    Note note = new Note(Tone.from(35, 0), 1); // dummy init to prevent compiler warning/error
    Torrent torrent = new Torrent();
    for (int ticks : IntRange.positive(scoreArray.ticks())) {
      Pitch pitch = scoreArray.getPitch(index, ticks);
      if (pitch != null) {
        boolean myBoolean = pitch.isHits(); // getBoolean(voice, c1);
        if (sticky) {
          if (myBoolean) {
            note = new Note(navigableMap.getTone(pitch.pitch()), 1);
            torrent.list.add(note);
          } else {
            if (pitch.pitch() != note.tone().pitch()) {
              // this is an important check, add fixme if commented out
              System.out.println(toString());
              throw new RuntimeException("pitch has changed: " + pitch + " != " + note.tone().pitch());
            }
            note.setTicks(note.ticks() + 1);
          }
        } else {
          if (!myBoolean) {
            // this is an important check, add fixme if commented out
            System.out.println(toString());
            throw new RuntimeException("boolean should be true at (" + index + ", " + ticks + ")");
          }
          note = new Note(navigableMap.getTone(pitch.pitch()), 1);
          torrent = new Torrent();
          torrent.list.add(note);
          voice.navigableMap.put(ticks, torrent);
        }
      }
      sticky = pitch != null;
    }
    return voice;
  }
}
