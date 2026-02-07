// code by jph
package ch.alpine.sonata.scr;

import java.util.Map.Entry;
import java.util.function.Consumer;

import ch.alpine.sonata.Note;
import ch.alpine.sonata.Torrent;
import ch.alpine.sonata.Voice;

public class UniformFlatten implements Consumer<Voice> {
  @Override
  public void accept(Voice voice) {
    applyTo(voice);
  }

  public static Voice of(Voice voice) {
    VoiceBuffer voiceBuffer = new VoiceBuffer();
    for (Entry<Integer, Torrent> entry : voice.navigableMap.entrySet()) {
      int ticks = entry.getKey();
      for (Note note : entry.getValue()) {
        voiceBuffer.put(note, false, ticks);
        ticks += note.ticks();
      }
    }
    return voiceBuffer.getVoice(); // clef ? etc.
  }

  public static void applyTo(Voice voice) {
    Voice myTemp = of(voice);
    voice.navigableMap.clear();
    voice.navigableMap.putAll(myTemp.navigableMap);
  }
}
