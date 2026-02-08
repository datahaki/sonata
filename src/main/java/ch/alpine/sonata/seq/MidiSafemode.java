// code by jph
package ch.alpine.sonata.seq;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import ch.alpine.midkit.Midi;
import ch.alpine.tensor.ext.PathName;
import sys.mat.IntRange;

public enum MidiSafemode {
  ;
  /** removes events that are: SYSTEM_EXCLUSIVE, SEQUENCER_EXCLUSIVE, ALT_SUSTAIN_PEDAL
   * 
   * @param sequence
   * @return given sequence without the events listed above
   * @throws Exception */
  private static Sequence filter(Sequence sequence) throws Exception {
    for (Track track : sequence.getTracks()) {
      for (int index : IntRange.positive(track.size())) {
        MidiEvent midiEvent = track.get(index);
        MidiMessage midiMessage = midiEvent.getMessage();
        byte[] message = midiMessage.getMessage();
        boolean myBoolean = true;
        switch (message[0] & 0xf0) { // as "command value"
        case ShortMessage.NOTE_OFF: // 0x80
          break;
        case ShortMessage.NOTE_ON: // 0x90
          break;
        case ShortMessage.POLY_PRESSURE: // 0xa0
          break;
        case ShortMessage.CONTROL_CHANGE: // 0xb0
          switch (message[1] & 0xff) {
          case Midi.DATA_ENTRY_MSB:
            break;
          case Midi.VOLUME:
            break;
          case Midi.SUSTAIN_PEDAL:
            break;
          case Midi.SOSTENUTO_PEDAL:
            break;
          case Midi.SOFT_PEDAL:
            break;
          case Midi.HARMONIC_PEDAL:
            myBoolean = false;
            break;
          case Midi.EFFECTS_1_DEPTH:
            break;
          case Midi.EFFECTS_3_DEPTH:
            break;
          case Midi.NRPN_LSB:
            break;
          case Midi.NRPN_MSB:
            break;
          case Midi.ALL_NOTES_OFF:
            break;
          default:
            break;
          }
          break;
        case ShortMessage.PROGRAM_CHANGE: // 0xc0
          break;
        case ShortMessage.CHANNEL_PRESSURE: // 0xd0
          break;
        case ShortMessage.PITCH_BEND: // 0xe0 pitch wheel change
          break;
        case 0xf0: // e.g. 0xff
          switch (message[0] & 0xff) { // as "status byte"
          case Midi.SYSTEM_EXCLUSIVE: // System_Exclusive
            myBoolean = false;
            break;
          case ShortMessage.MIDI_TIME_CODE: // 0xf1
            break;
          case ShortMessage.SONG_POSITION_POINTER: // 0xf2
            break;
          case ShortMessage.SONG_SELECT: // 0xf3
            break;
          case ShortMessage.TUNE_REQUEST: // 0xf6
            break;
          case ShortMessage.END_OF_EXCLUSIVE: // 0xf7
            break;
          case ShortMessage.TIMING_CLOCK: // 0xf8
            break;
          case ShortMessage.START: // 0xfa
            break;
          case ShortMessage.CONTINUE: // 0xfb
            break;
          case ShortMessage.STOP: // 0xfc
            break;
          // 0xfd undefined (reserved)
          case ShortMessage.ACTIVE_SENSING: // 0xfe
            break;
          case ShortMessage.SYSTEM_RESET: // 0xff
            switch (message[1] & 0xff) { //
            case Midi.CHANNEL_PREFIX:
              break;
            case Midi.PORT:
              break;
            case Midi.TRACK_END:
              break;
            case Midi.TEMPO:
              break;
            case Midi.SMPTE_OFFSET:
              break;
            case Midi.TIME_SIGNATURE:
              break;
            case Midi.KEY_SIGNATURE:
              break;
            case Midi.SEQUENCER_SPECIFIC:
              myBoolean = false;
              break;
            case Midi.TEXT:
            case Midi.COPYRIGHT_NOTICE:
            case Midi.TRACK_NAME:
            case Midi.INSTRUMENT_NAME:
            case Midi.MARKER:
            case Midi.CUE_POINT:
              break;
            default:
              break;
            }
            break;
          }
          break;
        }
        if (!myBoolean) {
          boolean flag = track.remove(midiEvent);
          if (flag)
            --index;
          else
            throw new RuntimeException("remove failed");
        }
      }
    }
    return sequence;
  }

  /** @param file midi file
   * @throws Exception if file is corrupt */
  private static Sequence getSequence(Path file) throws Exception {
    return filter(MidiSystem.getSequence(Files.newInputStream(file)));
  }

  public static void defaultExport(Path file) throws Exception {
    Midi.write(MidiSafemode.getSequence(file), PathName.of(file).withExtension("safemode.mid"));
  }
}
