// code by jph
package ch.alpine.sonata.enc.mid;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import ch.alpine.bridge.lang.EnumValue;
import ch.alpine.midkit.Midi;
import ch.alpine.midkit.MidiInstrument;
import ch.alpine.sonata.Divisions;
import ch.alpine.sonata.KeySignature;
import ch.alpine.sonata.Metric;
import ch.alpine.sonata.Voice;
import ch.alpine.sonata.scr.Score;
import ch.alpine.tensor.Rational;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.num.GCD;
import sys.dat.UniqueValue;
import sys.mat.IntRange;
import sys.mat.Ratio;

public class MidiStage {
  private static final Charset CHARSET = StandardCharsets.UTF_8;
  // ---
  private Integer quarterStretched = null;
  final SortedSet<Long> sortedSet = new TreeSet<>();
  private UniqueValue<Ratio> meter = UniqueValue.empty();
  public List<String> list = new LinkedList<>();
  private long gcd = 0;
  public int voices = 0;
  private Scalar bpm = null;
  private final UniqueValue<Integer> type = UniqueValue.empty();
  public final StringBuilder console = new StringBuilder();
  public NavigableMap<Long, String> text = new TreeMap<>();
  private final Map<Integer, MidiInstrument> channelToInst = new HashMap<>();

  public MidiStage(Sequence sequence) throws Exception {
    if (sequence.getDivisionType() == Sequence.PPQ) { // encoded in header
      quarterStretched = sequence.getResolution();
      gcd = quarterStretched;
    }
    // ---
    NavigableMap<Long, Scalar> tempo = new TreeMap<>();
    tempo.put(0L, Score.bpm_fallback);
    tempo.put(sequence.getTickLength(), Score.bpm_fallback);
    for (Track track : sequence.getTracks())
      for (int c0 : IntRange.positive(track.size())) {
        MidiEvent midiEvent = track.get(c0);
        long myLong = midiEvent.getTick();
        MidiMessage midiMessage = midiEvent.getMessage();
        byte[] data = midiMessage.getMessage();
        int head = data[0] & 0xff;
        switch (head) {
        case 0xff: // ff id length v0 v1 v2 v3 ...
          switch (data[1] & 0xff) {
          case Midi.TIME_SIGNATURE: // byte[2] has length
            meter.set(new Ratio(data[3], 1 << data[4]));
            break;
          case Midi.KEY_SIGNATURE:
            try {
              type.set((int) data[3]);
            } catch (Exception exception) {
              throw new RuntimeException("at ticks " + myLong + " KEY_SIGNATURE " + exception.getMessage());
            }
            break;
          case Midi.TEXT:
          case Midi.COPYRIGHT_NOTICE:
          case Midi.TRACK_NAME:
            list.add(new String(data, 3, data.length - 3, CHARSET));
            break;
          case Midi.LYRICS:
            String string = new String(data, 3, data.length - 3, CHARSET).trim();
            if (!string.isEmpty())
              text.put(myLong, text.containsKey(myLong) ? text.get(myLong) + "|" + string : string);
            break;
          case Midi.TEMPO: {
            bpm = Midi.parseTempo(data);
            tempo.put(myLong, bpm);
            break;
          }
          }
          break;
        default:
          break;
        }
        switch (data[0] & 0xf0) {
        case ShortMessage.PROGRAM_CHANGE: {
          channelToInst.put(data[0] & 0xf, EnumValue.fromOrdinal(MidiInstrument.class, data[1] & 0xff));
          System.out.println(channelToInst);
          break;
        }
        case ShortMessage.NOTE_ON:
        case ShortMessage.NOTE_OFF: {
          int voice = data[0] & 0x0f;
          voices = Math.max(voice + 1, voices);
          gcd = GCD.of(gcd, myLong).number().longValue();
          sortedSet.add(myLong);
          break;
        }
        default:
          break;
        }
      }
    if (gcd == 0)
      throw new Exception("gcd == 0");
    // ---
    long delta_max = -1;
    Entry<Long, Scalar> ante = null;
    for (Entry<Long, Scalar> post : tempo.entrySet()) {
      if (Objects.nonNull(ante)) {
        long delta = post.getKey() - ante.getKey();
        if (delta_max < delta) {
          bpm = ante.getValue();
          delta_max = delta;
        }
      }
      ante = post;
    }
  }

  public int getTicks(long myLong) {
    return (int) (myLong / gcd);
  }

  public void equip(Score score) {
    if (type.isPresent())
      score.keySignature = KeySignature.fromType(type.orElseThrow());
    // ---
    if (quarterStretched == null) {
      console.append("/quarter unavailable");
    } else {
      final int myInt = getTicks(quarterStretched);
      if (Metric.quartersList.contains(myInt)) {
        score.quarter = myInt;
        if (meter.isPresent()) {
          Ratio ratio = meter.orElseThrow();
          int measure = Scalars.intValueExact(Rational.of(4 * ratio.num() * score.quarter, ratio.den()));
          // IntegerMath.divideExact(4 * meter.num * score.quarter, meter.den);
          if (Metric.measuresList.contains(measure))
            score.division = Divisions.best(score.quarter, measure, meter.orElseThrow());
          else
            console.append("/measure=" + measure + " invalid");
        }
      } else {
        console.append("/quarter=" + myInt + " invalid");
      }
    }
    // ---
    score.bpm = bpm;
    if (Objects.nonNull(bpm))
      score.process.append(String.format("/tempo%s", bpm));
    // ---
    text.entrySet().forEach(myEntry -> score.text.put(getTicks(myEntry.getKey()), myEntry.getValue()));
    // for (Entry<Long, String> myEntry : text.entrySet())
    // myScore.text.put(getTicks(myEntry.getKey()), myEntry.getValue());
    int index = 0;
    // System.out.println("USE = "+channelToInst);
    for (Voice voice : score.voices) {
      if (channelToInst.containsKey(index))
        voice.midiInstrument = channelToInst.get(index);
      ++index;
    }
  }
}
