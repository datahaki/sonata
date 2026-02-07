// code by jph
package ch.alpine.sonata.seq;

import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import ch.alpine.midkit.MidiInstrument;
import ch.alpine.sonata.Note;
import ch.alpine.sonata.Torrent;
import ch.alpine.sonata.Voice;
import ch.alpine.sonata.scr.Score;

/** Each voice has own track and channel for convenient sheet reconstruction.
 * Note off events are stored in same track as note on events.
 * 
 * Also works for playback in Ivory II. */
public class VoicedSequence extends MidiSequence {
  public VoicedSequence(Score score, List<Voice> voices) {
    super(score, voices);
  }

  @Override
  protected final void play(int voice, int ticks, Torrent torrent) throws InvalidMidiDataException {
    Track track = tracks.get(voice); // voice==0 retrieves track1 etc. (because track0 is reserved)
    final int channel = voice;
    for (Note note : torrent) {
      int pitch = note.tone().pitch(); // + myScore.transpose valid range taken from MidiChannel.noteOn
      int vel = 64; // ??? myTorrent.myAttributes.velocity;
      track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, channel, pitch, vel), ticks));
      ticks += note.ticks();
      track.add(noteOffEvent(channel, pitch, vel, ticks));
    }
  }

  @Override
  protected int channel(int voice, MidiInstrument midiInstrument) {
    return voice;
  }
}
