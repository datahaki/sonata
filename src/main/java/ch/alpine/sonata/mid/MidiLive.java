// code by jph
package ch.alpine.sonata.mid;

import java.nio.ByteBuffer;
import java.util.Set;
import java.util.stream.Stream;

import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;

import ch.alpine.midkit.Midi;
import ch.alpine.midkit.put.MidiPut;
import ch.alpine.sonata.Joint;
import ch.alpine.sonata.Link;
import ch.alpine.sonata.Relation;

public class MidiLive {
  private static final long REALTIME = -1; // better now than never
  private final MidiPut midiPut;

  public MidiLive(MidiPut midiPut) {
    this.midiPut = midiPut;
  }

  public void play(Relation relation, int velocity, int channel) {
    play(new Joint(Stream.of(relation)), velocity, channel);
  }

  /** @param joint not untypical to contain only one {@link Relation}
   * @param myPlayback
   * @param transpose */
  public void play(Joint joint, int velocity, int channel) {
    try {
      int vel = velocity;
      // Receiver myReceiver = MidiOut.instance.getReceiver();
      // ---
      Set<Integer> hold = joint.getInteger0Set(Link.HOLD);
      for (int myInt : joint.getInteger0Set(Link.KILL))
        if (!hold.contains(myInt))
          midiPut.sendMessage(new ShortMessage(ShortMessage.NOTE_OFF, channel, myInt, vel), REALTIME);
      // ---
      for (Relation relation : joint)
        switch (relation.link) {
        case LIVE:
          midiPut.sendMessage(new ShortMessage(ShortMessage.NOTE_ON, channel, relation.integer0, vel), REALTIME);
          break;
        case JUMP:
          midiPut.sendMessage(new ShortMessage(ShortMessage.NOTE_OFF, channel, relation.integer0, vel), REALTIME);
          midiPut.sendMessage(new ShortMessage(ShortMessage.NOTE_ON, channel, relation.integer0 + relation.integer1, vel), REALTIME);
          break;
        default:
          break;
        }
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  /** used for piano pedals, but also control of Soundiron Choir
   * 
   * @param channel
   * @param command
   * @param value */
  public void sendControlChange(int channel, int command, int value) {
    try {
      // Receiver myReceiver = MidiOut.instance.getReceiver();
      midiPut.sendMessage(new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, command, value), REALTIME);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  /** used by Hauptwerk
   * 
   * @param channel
   * @param nrpn
   * @param data */
  public void sendNrpnAndData(int channel, int nrpn, int data) {
    try {
      // Receiver myReceiver = MidiOut.instance.getReceiver();
      midiPut.sendMessage(new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, Midi.NRPN_MSB, nrpn >> 7), REALTIME);
      midiPut.sendMessage(new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, Midi.NRPN_LSB, nrpn & 0x7f), REALTIME);
      midiPut.sendMessage(new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, Midi.DATA_ENTRY_MSB, data), REALTIME);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  /** for piano */
  public void sustainPedal(boolean myBoolean) {
    sendControlChange(0, Midi.SUSTAIN_PEDAL, myBoolean ? 127 : 0);
  }

  public void sostenutoPedal(boolean myBoolean) {
    sendControlChange(0, Midi.SOSTENUTO_PEDAL, myBoolean ? 127 : 0);
  }

  public void softPedal(boolean myBoolean) {
    sendControlChange(0, Midi.SOFT_PEDAL, myBoolean ? 127 : 0);
  }

  /** @param channel
   * @param value 0, ..., 16383, and 8192 is no bending */
  public void sendPitchBend(int channel, short value) {
    try {
      // Receiver myReceiver = MidiOut.instance.getReceiver();
      midiPut.sendMessage(new ShortMessage(ShortMessage.PITCH_BEND, channel, value & 0x7f, value >> 7), REALTIME);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  /** @param pitch -48 ... 48
   * @param value -16383 ... 16383 */
  public void sendTuneKey(final int pitch, short value) {
    byte[] data = new byte[8 + 4];
    ByteBuffer byteBuffer = ByteBuffer.wrap(data);
    byteBuffer.put((byte) Midi.SYSTEM_EXCLUSIVE);
    byteBuffer.put((byte) 0x7f);
    byteBuffer.put((byte) 0); // device id
    byteBuffer.put((byte) 8);
    byteBuffer.put((byte) 2);
    byteBuffer.put((byte) 0); // program
    byteBuffer.put((byte) 1); // number of changes
    int key = pitch;
    byteBuffer.put((byte) key);
    if (value < 0) {
      value = (short) (0x3fff - value);
      --key;
    }
    byteBuffer.put((byte) key);
    byteBuffer.put((byte) ((value >> 7) & 0x7f));
    byteBuffer.put((byte) (value & 0x7f));
    byteBuffer.put((byte) 0xf7); // EOX
    // System.out.println("sending " + FriendlyFormat.hexString(myByte, 0, myByte.length, " %02x"));
    try {
      // Receiver myReceiver = MidiOut.instance.getReceiver();
      midiPut.sendMessage(new SysexMessage(data, data.length), REALTIME);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }
}
