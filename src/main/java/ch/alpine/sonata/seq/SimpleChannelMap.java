// code by jph
package ch.alpine.sonata.seq;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.alpine.midkit.Midi;
import ch.alpine.midkit.MidiInstrument;
import ch.alpine.sonata.Voice;
import ch.alpine.tensor.ext.Integers;
import ch.alpine.tensor.ext.PackageTestAccess;

public class SimpleChannelMap {
  private final Map<MidiInstrument, Integer> map = new HashMap<>();

  public SimpleChannelMap(List<Voice> voices) {
    for (int index = 0; index < voices.size(); ++index)
      map.putIfAbsent(voices.get(index).midiInstrument, skipDrums(map.size()));
  }

  public int channel(MidiInstrument midiInstrument) {
    return map.get(midiInstrument);
  }

  @PackageTestAccess
  static int skipDrums(int size) {
    int channel = size < Midi.DRUMKIT_CHANNEL //
        ? size
        : size + 1;
    Integers.requireLessThan(channel, 16);
    return channel;
  }
}
