// code by jph
package ch.alpine.sonata.seq;

import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import ch.alpine.midkit.Midi;
import ch.alpine.midkit.MidiInstrument;
import ch.alpine.sonata.Note;
import ch.alpine.sonata.Torrent;
import ch.alpine.sonata.Voice;
import ch.alpine.sonata.scr.Score;

public class RealtimeSequence extends MidiSequence {
  private final SimpleChannelMap simpleChannelMap;

  public RealtimeSequence(Score score, List<Voice> voices) { // possibly replace by VoiceScore as soon as approved
    super(score, voices);
    simpleChannelMap = new SimpleChannelMap(voices);
  }

  @Override
  protected void play(int voice, int ticks, Torrent torrent) throws InvalidMidiDataException {
    Track track = tracks.get(voice);
    int channel = simpleChannelMap.channel(voices.get(voice).midiInstrument); // between 0 ... 15
    int vel = Midi.clip7bit(torrent.attributes.velocity);
    for (Note note : torrent) {
      int pitch = note.tone().pitch(); // valid range taken from MidiChannel.noteOn
      track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, channel, pitch, vel), ticks)); // into track of voice
      ticks += note.ticks();
      track0.add(noteOffEvent(channel, pitch, vel, ticks)); // into track 0
    }
  }

  @Override
  protected int channel(int voice, MidiInstrument midiInstrument) {
    return simpleChannelMap.channel(voices.get(voice).midiInstrument);
  }
}
