// code by jph
// http://www.recordingblogs.com/sa/tabid/88/Default.aspx?topic=MIDI+meta+messages
// http://www.deluge.co/?q=midi-tempo-bpm
package ch.alpine.sonata.seq;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import ch.alpine.midkit.DrumKit;
import ch.alpine.midkit.Midi;
import ch.alpine.midkit.MidiInstrument;
import ch.alpine.sonata.Meter;
import ch.alpine.sonata.Torrent;
import ch.alpine.sonata.Voice;
import ch.alpine.sonata.lyr.LyricsFormatter;
import ch.alpine.sonata.scr.Score;
import ch.alpine.tensor.Scalar;
import sys.mat.IntRange;

/** Sequence.PPQ */
public abstract class MidiSequence {
  private static final Charset CHARSET = StandardCharsets.UTF_8;

  private static MetaMessage fromString(int id, String string) throws InvalidMidiDataException {
    byte[] data = string.getBytes(CHARSET);
    return new MetaMessage(id, data, data.length);
  }

  private final Score myScore;
  final List<Voice> voices;
  // ---
  /** track0 is used for tempo, meter, tonic, text
   * also: some sequencing requires note_off events to be in track0,
   * since simultaneous note_off needs to advance note_on events of the same pitch */
  protected Track track0; // design is bad
  protected List<Track> tracks; // design is bad
  // ---
  private SortedMap<Integer, Scalar> extraTempo = Collections.emptySortedMap();
  private boolean withText = false;
  // ---
  private Map<Integer, NavigableMap<Integer, Set<Integer>>> nrpn = new HashMap<>();
  private int nrpn_channel = 1; // Hauptwerk default

  protected MidiSequence(Score score, List<Voice> voices) {
    this.myScore = score;
    this.voices = voices;
  }

  /** implementation must not alter state of member variables, so that multiple calls return the same result
   * 
   * @return
   * @throws InvalidMidiDataException */
  public final Sequence getSequence() throws InvalidMidiDataException {
    // The tempo-based timing type, for which the resolution is expressed in pulses (ticks) per quarter note.
    Sequence sequence = new Sequence(Sequence.PPQ, myScore.quarter); // normalizes tempo
    track0 = sequence.createTrack();
    track0.add(new MidiEvent(fromString(Midi.TEXT, myScore.title), 0));
    track0.add(new MidiEvent(fromString(Midi.TEXT, myScore.comment), 0));
    track0.add(new MidiEvent(fromString(Midi.COPYRIGHT_NOTICE, "sequenced by ThePirateFugues"), 0));
    // BEGIN: time signature
    {
      Meter meter = myScore.getMeter();
      int num = meter.num();
      int den = meter.den();
      track0.add(new MidiEvent(Midi.timeSignature(num, den), 0)); // at ticks=0
    }
    // END: time signature
    track0.add(new MidiEvent(Midi.tempo(myScore.bpm), 0)); // at ticks=0
    // BEGIN: tonic
    final MidiEvent keySignatureEvent;
    {
      byte[] data = new byte[2]; // myByte[0] = type, myByte[1] = major/minor
      data[0] = (byte) myScore.keySignature.type();
      MetaMessage metaMessage = new MetaMessage(Midi.KEY_SIGNATURE, data, data.length);
      keySignatureEvent = new MidiEvent(metaMessage, 0);
      track0.add(keySignatureEvent); // at ticks=0
    }
    // END: tonic
    {
      int total = voices.stream().mapToInt(voice -> voice.ticks()).max().orElse(0);
      int count = 0;
      for (NavigableMap<Integer, Set<Integer>> navigableMap : nrpn.values()) {
        navigableMap.put(total, Collections.emptySet());
        count += implementNrpnStream(navigableMap);
      }
      if (0 < count)
        track0.add(new MidiEvent(fromString(Midi.MARKER, "eof"), total + 1)); // otherwise a single stop might still be standing
    }
    // BEGIN: all voices
    tracks = new ArrayList<>();
    for (int index : IntRange.positive(voices.size())) {
      Track track = sequence.createTrack();
      {
        MidiInstrument midiInstrument = voices.get(index).midiInstrument;
        int channel = channel(index, midiInstrument);
        ShortMessage shortMessage = new ShortMessage(ShortMessage.PROGRAM_CHANGE, channel, midiInstrument.ordinal(), 0);
        track.add(new MidiEvent(shortMessage, 0));
      }
      track.add(keySignatureEvent);
      tracks.add(track);
    }
    {
      int voice = 0;
      for (Voice myVoice : voices) {
        for (Entry<Integer, Torrent> entry : myVoice)
          play(voice, entry.getKey(), entry.getValue());
        ++voice;
      }
    }
    // END: all voices
    // BEGIN: vary tempo (after note-off events)
    for (Entry<Integer, Scalar> entry : extraTempo.entrySet())
      track0.add(new MidiEvent(Midi.tempo(entry.getValue()), entry.getKey()));
    // END: vary tempo
    // ---
    if (withText) {
      for (int voice : IntRange.positive(voices.size()))
        mapText(tracks.get(voice), new LyricsFormatter(myScore.voices.get(voice).lyric, false), Midi.LYRICS);
      mapText(track0, new LyricsFormatter(myScore.text, false), Midi.TEXT);
    }
    // ---
    try {
      for (Entry<Integer, DrumKit> entry : navigableMap.entrySet())
        track0.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, Midi.DRUMKIT_CHANNEL, entry.getValue().pitch(), metronome_vel), entry.getKey()));
    } catch (Exception exception) {
      exception.printStackTrace();
    }
    return sequence;
  }

  public void setNrpnChannel(int nrpn_channel) {
    this.nrpn_channel = nrpn_channel;
  }

  private NavigableMap<Integer, DrumKit> navigableMap = new TreeMap<>();
  public int metronome_vel = 32;

  public void setMetronome(NavigableMap<Integer, DrumKit> navigableMap) {
    this.navigableMap = navigableMap;
  }
  // private void insertMetronome(int depth) {
  // int delta = myScore.division.valueAtSafe(depth);
  // try {
  // for (int ticks = 0; ticks < myScore.ticks(); ticks += delta) {
  // DrumKit drumKit = ticks % myScore.measure() == 0 //
  // ? DrumKit.METRONOME_BELL
  // : DrumKit.METRONOME_CLICK;
  // track0.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, Midi.DRUMKIT_CHANNEL, drumKit.pitch(), 64), ticks));
  // }
  // } catch (Exception exception) {
  // exception.printStackTrace();
  // }
  // }

  /** @param hash is typically channel because the same nrpn affect all notes on channel
   * @param ticks
   * @param set */
  public void setNrpn(int hash, int ticks, Set<Integer> set) {
    if (!nrpn.containsKey(hash))
      nrpn.put(hash, new TreeMap<>());
    nrpn.get(hash).put(ticks, set);
  }

  private int implementNrpnStream(NavigableMap<Integer, Set<Integer>> navigableMap) throws InvalidMidiDataException {
    int count = 0;
    final int channel = nrpn_channel;
    Set<Integer> prev = Collections.emptySet();
    for (Entry<Integer, Set<Integer>> entry : navigableMap.entrySet()) {
      final int ticks = entry.getKey();
      Set<Integer> next = entry.getValue();
      for (int nrpn : prev)
        if (!next.contains(nrpn)) {
          int data = 0x0;
          track0.add(new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, Midi.NRPN_MSB, nrpn >> 7), ticks));
          track0.add(new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, Midi.NRPN_LSB, nrpn & 0x7f), ticks));
          track0.add(new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, Midi.DATA_ENTRY_MSB, data), ticks));
          ++count;
        }
      for (int nrpn : next)
        if (!prev.contains(nrpn)) {
          int data = 0x7f;
          track0.add(new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, Midi.NRPN_MSB, nrpn >> 7), ticks));
          track0.add(new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, Midi.NRPN_LSB, nrpn & 0x7f), ticks));
          track0.add(new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, Midi.DATA_ENTRY_MSB, data), ticks));
          ++count;
        }
      prev = next;
    }
    return count;
  }

  public void setExtraTempo(SortedMap<Integer, Scalar> sortedMap) {
    extraTempo = sortedMap;
  }

  public void setText(boolean myBoolean) {
    withText = myBoolean;
  }

  private void mapText(Track track, LyricsFormatter lyricsFormatter, final int id) throws InvalidMidiDataException {
    for (int pass : IntRange.positive(lyricsFormatter.max_pass))
      for (Entry<Integer, String> entry : lyricsFormatter.getMap(pass).entrySet())
        track.add(new MidiEvent(fromString(id, entry.getValue()), entry.getKey()));
  }

  protected abstract void play(int voice, int ticks, Torrent torrent) throws InvalidMidiDataException;

  protected abstract int channel(int voice, MidiInstrument midiInstrument);
  // protected final static MidiEvent createMidiEvent(MidiMessage midiMessage, long ticks) {
  // return new MidiEvent(midiMessage, ticks );
  // }

  protected final static MidiEvent noteOffEvent(int channel, int pitch, int vel, long ticks) throws InvalidMidiDataException {
    return new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF, channel, pitch, 0), ticks); // 0 instead of vel
  }
}
