// code by jph
package ch.alpine.sonata.utl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import ch.alpine.sonata.Note;
import ch.alpine.sonata.ScoreArray;
import ch.alpine.sonata.ScoreEntry;
import ch.alpine.sonata.Tone;
import ch.alpine.sonata.Torrent;
import ch.alpine.sonata.Voice;
import ch.alpine.sonata.jnt.ScoreArrays;
import ch.alpine.sonata.scr.Score;
import sys.dat.MapOperations;

public enum ScoreOps {
  ;
  public static Score clonePart(Score scoreModel_myScore, int beg, int end) {
    Score score = scoreModel_myScore.cloneScore();
    // myScore.comment += " " + myMeasureCounter.textInterval(beg, end);
    List<Voice> myVoices = new ArrayList<>();
    final int delay = beg % score.measure();
    for (Voice myVoice : score.voices) {
      Voice myExtract = myVoice.extract(beg, end);
      if (!myExtract.navigableMap.isEmpty()) {
        Timeshift.by(myExtract, delay);
        myVoices.add(myExtract);
      }
    }
    score.voices = myVoices;
    // ---
    score.triad.clear();
    MapOperations.translate(scoreModel_myScore.triad.subMap(beg, end), -beg + delay, score.triad);
    // ---
    // score.hepta.clear();
    // MapOperations.translate(scoreModel_myScore.hepta.subMap(beg, end), -beg + delay, score.hepta);
    // ---
    score.text.clear();
    MapOperations.translate(scoreModel_myScore.text.subMap(beg, end), -beg + delay, score.text);
    // ---
    new TimescalePacker(score, true).pack();
    return score;
  }

  public static NavigableMap<ScoreEntry, Note> getNoteEntries(Score score) {
    NavigableMap<ScoreEntry, Note> navigableMap = new TreeMap<>();
    int _voice = 0;
    for (Voice voice : score.voices) {
      for (Entry<Integer, Torrent> entry : voice.navigableMap.entrySet()) {
        int ticks = entry.getKey();
        for (Note note : entry.getValue().list) {
          navigableMap.put(new ScoreEntry(ticks, _voice), note);
          ticks += note.ticks();
        }
      }
      ++_voice;
    }
    return navigableMap;
  }

  public static Collection<Tone> getTones(Score score) {
    return score.allNotes().map(myNote -> myNote.tone()).collect(Collectors.toList());
  }

  public static void immutableScore(Score score) {
    for (Voice voice : score.voices)
      voice.navigableMap = Collections.unmodifiableNavigableMap(voice.navigableMap);
    score.voices = Collections.unmodifiableList(score.voices);
  }

  /** @param score
   * @return */
  public static ScoreArray create(Score score) {
    return ScoreArrays.create(score.voices, score.ticks());
  }
}
