// code by jph
package ch.alpine.sonata.scr;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import ch.alpine.sonata.Dynamic;
import ch.alpine.sonata.FiguredBass;
import ch.alpine.sonata.Note;
import ch.alpine.sonata.Ornament;
import ch.alpine.sonata.ScoreEntry;
import ch.alpine.sonata.Voice;

public class ScoreBuffer {
  private SortedMap<Integer, VoiceBuffer> sortedMap = new TreeMap<>();

  public void reserve(int voice) {
    if (!sortedMap.containsKey(voice))
      sortedMap.put(voice, new VoiceBuffer());
  }

  /** @param note
   * @param tie is true, if a note should bind to this myNote in the future
   * @param ticks absolute in score
   * @param voice
   * @throws Exception */
  public void put(Note note, boolean tie, int ticks, int voice) {
    reserve(voice);
    sortedMap.get(voice).put(note, tie, ticks);
  }

  public void shake(ScoreEntry scoreEntry, Ornament ornament) {
    if (ornament != null) {
      reserve(scoreEntry.voice());
      sortedMap.get(scoreEntry.voice()).shake(scoreEntry.ticks(), ornament);
    }
  }

  public void press(ScoreEntry scoreEntry, Dynamic dynamics) {
    if (dynamics != null) {
      reserve(scoreEntry.voice());
      sortedMap.get(scoreEntry.voice()).press(scoreEntry.ticks(), dynamics);
    }
  }

  public void lyric(String string, int ticks, int voice) {
    if (!string.isEmpty()) {
      reserve(voice);
      sortedMap.get(voice).lyric(string, ticks);
    }
  }

  public void fbass(ScoreEntry scoreEntry, FiguredBass figuredBass) {
    if (!figuredBass.isDefault()) {
      reserve(scoreEntry.voice());
      sortedMap.get(scoreEntry.voice()).fbass(scoreEntry.ticks(), figuredBass);
    }
  }

  public boolean isFreeAt(int ticks, int voice) {
    return !sortedMap.containsKey(voice) //
        || sortedMap.get(voice).canAppendAt(ticks);
  }

  public void untie(int voice) {
    sortedMap.get(voice).untie();
  }

  // public int indexOf(int voice) {
  // return new LinkedList<Integer>(myVoiceBuffers.keySet()).indexOf(voice);
  // }
  public Voice getVoice(int voice) {
    return sortedMap.containsKey(voice) ? sortedMap.get(voice).getVoice() : null;
  }

  public List<Voice> getVoices() {
    return sortedMap.values().stream().map(VoiceBuffer::getVoice).collect(Collectors.toList());
  }

  /** @return number of {@link Voice}s getVoices() returns */
  public int voices() {
    return sortedMap.size();
  }
}
