// code by jph
package ch.alpine.sonata.scr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import ch.alpine.sonata.Note;
import ch.alpine.sonata.Torrent;
import ch.alpine.sonata.Voice;
import sys.mat.IntRange;

public class VoiceScore implements Serializable {
  public List<Voice> voices = new ArrayList<>();

  public int ticks() {
    return voices.stream() //
        .mapToInt(Voice::ticks) //
        .max().orElse(0);
  }

  public int lastHit() {
    int max = 0;
    for (Voice voice : voices)
      if (!voice.navigableMap.isEmpty()) {
        Entry<Integer, Torrent> myEntry = voice.navigableMap.lastEntry();
        max = Math.max(max, myEntry.getValue().getNoteMapAbsolute(myEntry.getKey()).lastKey());
      }
    return max;
  }

  public final Optional<Integer> entry() {
    Integer myInteger = null;
    for (Voice voice : voices)
      if (!voice.navigableMap.isEmpty()) {
        int min = voice.navigableMap.firstKey();
        myInteger = myInteger == null ? min : Math.min(myInteger, min);
      }
    return Optional.ofNullable(myInteger);
  }

  public IntRange getAudibleRange() {
    Optional<Integer> optional = entry();
    if (!optional.isPresent())
      return new IntRange(0, 0);
    return new IntRange(optional.get(), ticks());
  }

  public Stream<Note> allNotes() {
    return voices.stream().flatMap(myVoice -> myVoice.allNotes());
  }

  public VoiceScore extract(int beg, int end) {
    VoiceScore voiceScore = new VoiceScore();
    for (Voice voice : voices)
      voiceScore.voices.add(voice.extract(beg, end));
    return voiceScore;
  }

  public void clearVoices() {
    voices.forEach(Voice::clear);
  }

  public void clearMotifs() {
    voices.forEach(voice -> voice.motif.clear());
  }

  public final int voices() {
    return voices.size();
  }

  void plagiarizeVoicesFrom(Iterator<Voice> iterator) {
    for (Voice voice : voices)
      voice.plagiarizeFrom(iterator.next());
  }
}
