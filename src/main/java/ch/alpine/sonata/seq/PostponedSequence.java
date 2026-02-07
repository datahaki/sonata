// code by jph
// http://computermusicresource.com/MIDI.Commands.html
package ch.alpine.sonata.seq;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import ch.alpine.midkit.Midi;
import ch.alpine.midkit.MidiInstrument;
import ch.alpine.sonata.Joint;
import ch.alpine.sonata.Link;
import ch.alpine.sonata.Note;
import ch.alpine.sonata.Torrent;
import ch.alpine.sonata.Voice;
import ch.alpine.sonata.jnt.ScoreJoint;
import ch.alpine.sonata.jnt.ScoreJoint1;
import ch.alpine.sonata.scr.Score;

/** sequencing optimized for playback with
 * 1) Propellerhead Reason 5;
 * 2) Hauptwerk.
 * note off events are stored in track0.
 * channels are shared based on instrumentation.
 * held notes are respected.
 * for correct playback with Synthogy Ivory 2, each NOTE_ON is paired with a NOTE_OFF event (not required for Reason, Hauptwerk).
 * 
 * Previously: class was abstract, and EnsembleSequence and MaterialEnsembleSequence were implementations. */
public class PostponedSequence extends MidiSequence {
  // channel -> ( pitch -> Set<ticks> )
  private final Map<Integer, Map<Integer, NavigableSet<Integer>>> noteOff = new HashMap<>();
  private final SimpleChannelMap simpleChannelMap;

  public PostponedSequence(Score score, List<Voice> voices) { // possibly replace by VoiceScore as soon as approved
    super(score, voices);
    simpleChannelMap = new SimpleChannelMap(voices);
    // BEGIN: to prevent kill of held note
    final int ticks_total = voices.stream().mapToInt(Voice::ticks).max().orElse(0);
    // channel -> ( ticks -> Set<pitch> )
    Map<Integer, Map<Integer, Set<Integer>>> holdKill = new HashMap<>();
    voices.stream().mapToInt(voice -> simpleChannelMap.channel(voice.midiInstrument)).distinct().sorted().forEach(channel -> {
      Map<Integer, Set<Integer>> map = new HashMap<>();
      ScoreJoint scoreJoint = ScoreJoint1.create(onlyChannel(voices, channel), 0, ticks_total);
      for (Entry<Integer, Joint> entry : scoreJoint.navigableMap.entrySet()) {
        int ticks = entry.getKey();
        Joint joint = entry.getValue();
        // System.out.println(ticks + " " + myJoint.myList);
        Set<Integer> hold = joint.getInteger0Set(Link.HOLD);
        Set<Integer> lift = new HashSet<>();
        lift.addAll(joint.getInteger0Set(Link.KILL));
        lift.addAll(joint.getInteger0Set(Link.JUMP));
        for (int pitch : lift)
          if (hold.contains(pitch)) {
            if (!map.containsKey(ticks))
              map.put(ticks, new HashSet<>());
            map.get(ticks).add(pitch);
          }
      }
      holdKill.put(channel, map);
      noteOff.put(channel, new HashMap<>());
    });
    // END: to prevent kill of held note
    // ---
    for (Voice voice : voices) {
      final int channel = simpleChannelMap.channel(voice.midiInstrument);
      for (Entry<Integer, Torrent> entry : voice) {
        int ticks = entry.getKey();
        Torrent torrent = entry.getValue();
        Map<Integer, Set<Integer>> map = holdKill.get(channel);
        Map<Integer, NavigableSet<Integer>> myOff = noteOff.get(channel);
        for (Note note : torrent) {
          ticks += note.ticks();
          if (!(map.containsKey(ticks) && map.get(ticks).contains(note.tone().pitch()))) { // NOTE_OFF is allowed here
            if (!myOff.containsKey(note.tone().pitch()))
              myOff.put(note.tone().pitch(), new TreeSet<>());
            myOff.get(note.tone().pitch()).add(ticks);
          }
        }
      }
    }
  }

  private List<Voice> onlyChannel(List<Voice> voices, int channel) {
    return voices.stream() //
        .filter(voice -> simpleChannelMap.channel(voice.midiInstrument) == channel) //
        .collect(Collectors.toList());
  }

  @Override
  protected void play(int voice, int ticks, Torrent torrent) throws InvalidMidiDataException {
    // System.out.println("voice"+voice+" ticks"+ticks);
    Track track = tracks.get(voice);
    int channel = simpleChannelMap.channel(voices.get(voice).midiInstrument); // between 0 ... 15
    int vel = torrent.attributes.velocity;
    // System.out.println("vel "+vel);
    // Midi.clip7bit(torrent.attributes.velocity); // TODO TPF
    for (Note note : torrent) {
      int pitch = Midi.clip7bit(note.tone().pitch()); // + myScore.transpose valid range taken from MidiChannel.noteOn
      track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, channel, pitch, vel), //
          ticks)); // into track of voice
      track0.add(noteOffEvent(channel, pitch, vel, noteOff.get(channel).get(note.tone().pitch()).higher(ticks))); // into track 0
      ticks += note.ticks();
    }
  }

  @Override
  protected int channel(int voice, MidiInstrument midiInstrument) {
    return simpleChannelMap.channel(midiInstrument);
  }
}
