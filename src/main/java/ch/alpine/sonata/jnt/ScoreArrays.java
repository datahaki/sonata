// code by jph
package ch.alpine.sonata.jnt;

import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;

import ch.alpine.sonata.KeySignature;
import ch.alpine.sonata.Note;
import ch.alpine.sonata.Pitch;
import ch.alpine.sonata.ScoreArray;
import ch.alpine.sonata.Torrent;
import ch.alpine.sonata.Voice;
import sys.mat.IntRange;

public enum ScoreArrays {
  ;
  public static ScoreArray create(List<Voice> voices, int ticks) {
    int ticks_test = 0;
    for (Voice voice : voices)
      ticks_test = Math.max(ticks_test, voice.ticks());
    if (ticks < ticks_test)
      System.out.println("warning in score array");
    ScoreArray scoreArray = new ScoreArray(voices.size(), ticks);
    int _voice = 0;
    for (Voice voice : voices) {
      for (Entry<Integer, Torrent> entry : voice)
        scoreArray.set(_voice, entry.getKey(), entry.getValue());
      ++_voice;
    }
    return scoreArray;
  }

  public static Torrent get(ScoreArray scoreArray, KeySignature keySignature, final int voice, int ticks, int max_length) {
    Torrent torrent = new Torrent();
    Note note = null;
    for (int ofs : IntRange.positive(max_length)) {
      Pitch pitch = scoreArray.getPitch(voice, ticks + ofs);
      if (Objects.isNull(pitch))
        break;
      if (pitch.isHits()) {
        note = new Note(keySignature.dodecatonicScale().getTone(pitch.pitch()), 1);
        torrent.list.add(note);
      } else //
      if (note != null)
        note.setTicks(note.ticks() + 1);
      else
        throw new RuntimeException("torrent extraction must start on begin of note");
    }
    return torrent;
  }

  // ---
  public static NavigableSet<Integer> getAlts(List<Voice> voices) {
    NavigableSet<Integer> navigableSet = new TreeSet<>();
    for (Voice voice : voices)
      for (Entry<Integer, Torrent> entry : voice) {
        int ticks = entry.getKey();
        for (Note note : entry.getValue().list) {
          navigableSet.add(ticks);
          ticks += note.ticks();
        }
        navigableSet.add(ticks);
      }
    return navigableSet;
  }
}
