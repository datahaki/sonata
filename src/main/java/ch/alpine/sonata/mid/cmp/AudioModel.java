// code by jph
package ch.alpine.sonata.mid.cmp;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.function.BiFunction;

import ch.alpine.midkit.Midi;
import ch.alpine.sonata.Diatone;
import ch.alpine.sonata.DiatoneAlter;
import ch.alpine.sonata.Dynamic;
import ch.alpine.sonata.HeptatonicScale;
import ch.alpine.sonata.Note;
import ch.alpine.sonata.Ornament;
import ch.alpine.sonata.Torrent;
import ch.alpine.sonata.Voice;
import ch.alpine.sonata.scr.Score;
import ch.alpine.sonata.seq.MidiSequence;
import ch.alpine.sonata.tri.Triad;
import ch.alpine.sonata.tri.cho.BalancedChordScore;
import ch.alpine.sonata.tri.cho.ChordScore;
import ch.alpine.sonata.tri.cho.ChordScoreEmit;
import ch.alpine.sonata.utl.Timescale;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.tmp.ResamplingMethod;
import ch.alpine.tensor.tmp.TimeSeries;
import sys.mat.IntRange;
import sys.mat.IntRanges;

public class AudioModel {
  /** private scaling factor to insert ornaments */
  public static final int FACTOR = 12;
  // ---
  public static final List<Scalar> fermataFractions = List.of( //
      RealScalar.ONE, //
      RationalScalar.of(3, 4), //
      RationalScalar.of(2, 3), //
      RationalScalar.of(1, 2), //
      RationalScalar.of(1, 3), //
      RationalScalar.of(1, 4));
  /** begin of selection */
  public int audio_ticks = 0;
  /** end of selection */
  public int audio_stops = 0;
  // ---
  /** play ornaments */
  public boolean playshake = true;
  /** AudioConfig chords */
  public boolean accompany = true; // TODO TPF rename accompany_triads?
  /** AudioConfig */
  public boolean figuredbass = true;
  /**
   * 
   */
  public int metronome = 0;
  // ---
  /** use getNormalVelocity() for normal velocity */
  public boolean dynamics = true;
  public Dynamic master = Dynamic.MF;
  /** use getChordVelocity() for chord velocity */
  public int chordVelocity; // assigned in constructor; also used for figuredBass
  public int chordMean = 60;
  /** play all chords by default */
  public boolean chordFilter = false;
  /** play notes pitch neutralized */
  public boolean pitchNeutralized = false;
  // ---
  public int staccatoDepth = 0;
  public int staccatoLevel = 0;
  public int swingDepth = 0;
  public int swingLevel = 0;
  // ---
  /** misnomer now */
  public Scalar fermataFraction = RealScalar.ONE;
  public int ritardandoMeasures = 0;
  public int ritardandoReduction = 0;

  public ChordScore createChordScore() {
    return new BalancedChordScore(1, 0); // TODO TPF magic const
  }

  private Score getPlayScore(Score score, AudioConfiguration audioConfiguration, IntRanges<IntRange> fermatas) {
    Score playScore = new Score();
    playScore.takesHeaderFrom(score);
    // ---
    for (Voice voice : audioConfiguration.voices)
      playScore.voices.add(voice);
    playScore.triad.putAll(score.triad);
    playScore = playScore.cloneScore();
    // shake is omitted because ornaments will be implemented as notes
    playScore.text.putAll(score.text);
    // ---
    final int voices = playScore.voices();
    // ---
    // BEGIN: build notelist
    List<NoteList> noteLists = new LinkedList<>();
    for (int voice : IntRange.positive(voices)) {
      NoteList noteList = new NoteList();
      for (Entry<Integer, Note> entry : playScore.voices.get(voice).getNoteMapAbsolute().entrySet()) {
        final int ticks = entry.getKey();
        final Note note = entry.getValue().copy();
        noteList.addEntry(ticks, note, //
            ticks * FACTOR, new Note(note.tone(), note.ticks() * FACTOR));
      }
      noteLists.add(noteList);
    }
    // END: build notelist
    // ---
    // BEGIN: modify velocity
    {
      // final ScoreJoint scoreJoint1 = ScoreJoint1.create(playScore.voices, 0, 0); // here voice content is required
      // final int maxDepth = playScore.division.maxDepth;
      /** can be null */
      for (int voice : IntRange.positive(voices))
        for (NoteEntry noteEntry : noteLists.get(voice)) {
          double vel = master.volume();
          if (dynamics) {
            Entry<Integer, Dynamic> entry = score.voices.get(voice).press.lowerEntry(noteEntry.prev_ticks + 1);
            if (Objects.nonNull(entry))
              vel = entry.getValue().volume();
          }
          // ---
          {
            Ornament ornament = score.voices.get(voice).shake.get(noteEntry.prev_ticks);
            if (Objects.nonNull(ornament))
              vel += ornament.velocity_delta;
          }
          // ---
          noteEntry.velocity = vel;
        }
    }
    // END: modify velocity
    // ---
    // BEGIN: modify rhythm
    {
      final int mark = Math.max(1, playScore.division.maxDepth - swingDepth);
      final int mod = playScore.division.valueAtSafe(mark); // min length!
      final int range = playScore.division.valueAtSafe(mark - 1); // min length!
      // System.out.println("div : " + myScore.myDivision);
      // System.out.println("mark : " + mark);
      // System.out.println("mod : " + mod);
      // System.out.println("range: " + range);
      SwingHelper swingHelper = new SwingHelper(mod, range);
      for (int voice : IntRange.positive(voices))
        for (NoteEntry noteEntry : noteLists.get(voice)) {
          { // check beginning
            int pos = noteEntry.prev_ticks;
            int beg = Math.floorDiv(pos, range) * range;
            if (swingHelper.allClear(noteLists.get(voice).navigableSet.subSet(beg, beg + range))) {
              int delta = swingHelper.getRyhtmDelta(pos);
              noteEntry.next_ticks += swingLevel * delta;
              noteEntry.next_note.setTicks(noteEntry.next_note.ticks() - swingLevel * delta);
            }
          }
          { // check end
            int pos = noteEntry.prev_ticks + noteEntry.prev_note.ticks();
            int beg = Math.floorDiv(pos, range) * range;
            if (swingHelper.allClear(noteLists.get(voice).navigableSet.subSet(beg, beg + range))) {
              int delta = swingHelper.getRyhtmDelta(pos);
              noteEntry.next_note.setTicks(noteEntry.next_note.ticks() + swingLevel * delta);
            }
          }
        }
    }
    // END: modify rhythm
    // ---
    // BEGIN: stretch temporal resolution
    List<Voice> chordVoice = new LinkedList<>();
    Timescale.stretch(playScore, 2, 3, 2); // assert that as factor
    if (audioConfiguration.accompany || audioConfiguration.figuredbass) {
      Entry<Integer, Triad> myPrev = null;
      for (Entry<Integer, Triad> entry : score.triad.entrySet()) {
        if (Objects.nonNull(myPrev) && //
            myPrev.getKey() + 1 == entry.getKey() && //
            myPrev.getValue().equals(entry.getValue())) {
          int offset = myPrev.getKey() * FACTOR;
          for (int ticks : new IntRange(1, FACTOR)) // = 1; ticks < factor; ++ticks)
            playScore.triad.put(offset + ticks, myPrev.getValue());
        }
        myPrev = entry;
      }
      ChordScore chordScore = createChordScore();
      chordScore.setPitchMean(chordMean);
      if (audioConfiguration.accompany) // TODO TPF not final design
        chordVoice.addAll(ChordScoreEmit.accompaniment( //
            playScore, chordScore, chordFilter, getChordVelocity()).voices);
      if (audioConfiguration.figuredbass)
        chordVoice.addAll(ChordScoreEmit.accompanimentFigured( //
            playScore, chordScore, master.velocity).voices); // deliberately as loud as the average
    }
    for (int voice : IntRange.positive(voices))
      playScore.voices.get(voice).navigableMap.clear();
    // END: stretch temporal resolution
    // ---
    // final Map<Integer, Integer> fermatas = new HashMap<>();
    // IntRanges<IntRange> fermatas = new IntRanges<>();
    {
      // BEGIN: install ornaments
      // System.out.println(" ---------------- install ornaments");
      {
        for (int voice : IntRange.positive(voices)) {
          Map<Integer, Torrent> map = playScore.voices.get(voice).navigableMap;
          for (NoteEntry noteEntry : noteLists.get(voice)) {
            final Torrent torrent = new Torrent();
            torrent.attributes.velocity = noteEntry.getMidiVelocity();
            Ornament ornament = score.voices.get(voice).shake.get(noteEntry.prev_ticks);
            if (playshake && ornament != null) {
              if (ornament.equals(Ornament.FERMATA))
                fermatas.union(new IntRange(noteEntry.next_ticks, noteEntry.next_ticks + noteEntry.next_note.ticks()));
              final int unit = ornament.getUnit(FACTOR, score.quarter, score.atom);
              if (canConvert(score, noteEntry.next_note.ticks(), ornament))
                try {
                  HeptatonicScale heptatonicScale = score.keySignature.diatonicScale();
                  Map<Diatone, DiatoneAlter> diatoneMap = new HashMap<>(heptatonicScale.diatoneMap());
                  Note note = noteEntry.next_note;
                  DiatoneAlter diatoneAlter = note.tone().diatoneAlter();
                  diatoneMap.put(diatoneAlter.diatone(), diatoneAlter);
                  torrent.list.addAll(ornament.convert(noteEntry.next_note, unit, HeptatonicScale.of(diatoneMap)).list);
                } catch (Exception exception) {
                  throw new RuntimeException(exception);
                }
            }
            if (torrent.list.isEmpty())
              torrent.list.add(noteEntry.next_note);
            map.put(noteEntry.next_ticks, torrent);
          }
        }
      }
      // END: install ornaments
      // ---
      // BEGIN: modify staccato
      for (int voice : IntRange.positive(voices)) {
        Map<Integer, Torrent> map = playScore.voices.get(voice).navigableMap;
        for (NoteEntry noteEntry : noteLists.get(voice)) {
          int log = score.division.log(noteEntry.prev_note.ticks());
          if (log <= staccatoDepth) {
            Torrent torrent = map.get(noteEntry.next_ticks);
            if (torrent.list.size() == 1) { // equivalent: no ornament installed. staccato leaves ornaments alone
              Note note = torrent.first();
              note.setTicks(note.ticks() - staccatoLevel * (log + 1));
              note.setTicks(Math.max(1, note.ticks()));
            }
          }
        }
      }
      // END: modify staccato
    }
    if (pitchNeutralized) {
      ChordScore chordScore = createChordScore();
      chordScore.setPitchMean(chordMean);
      playScore = ChordScoreEmit.neutralize(playScore, chordScore, null);
    }
    // ---
    playScore.voices.addAll(chordVoice);
    // ---
    return playScore;
  }

  public int nrpn_channel() {
    return 0;
  }

  // make design so that only real scores are passed into here
  public MidiSequence getSequence(Score score, AudioConfiguration audioConfiguration, BiFunction<Score, List<Voice>, MidiSequence> biFunction) {
    IntRanges<IntRange> fermatas = new IntRanges<>();
    Score playScore = getPlayScore(score, audioConfiguration, fermatas);
    final int audio_first = audio_ticks; // identical with audio_ticks when playback starts, but interval is allowed to change during play
    final int newAudioFirst = audio_first * FACTOR;
    final int newAudioStops = audio_stops * FACTOR;
    // ---
    // for (Voice voice : playScore.voices)
    // voice.channel = HcSvntDracones.getVirtualStudio().channel(voice.channel);
    // ---
    MidiSequence midiSequence = biFunction.apply(playScore, playScore.extract(newAudioFirst, newAudioStops).voices);
    // BEGIN: nrpn
    if (dynamics) {
      midiSequence.setNrpnChannel(nrpn_channel());
      for (Voice voice : playScore.voices) {
        NavigableMap<Integer, Note> navigableMap = voice.getNoteMapAbsolute();
        Integer higher = navigableMap.higherKey(newAudioFirst - 1);
        if (higher != null && higher < newAudioStops) {
          if (!voice.press.containsKey((int) higher)) {
            Entry<Integer, Dynamic> myLo = voice.press.lowerEntry(higher + 1);
            voice.press.put(higher, myLo == null ? master : myLo.getValue());
          }
          // for (Entry<Integer, Dynamic> entry : voice.press.subMap(newAudioFirst, newAudioStops).entrySet())
          // midiSequence.setNrpn( //
          // voice.channel, //
          // entry.getKey() - newAudioFirst, //
          // HcSvntDracones.getVirtualStudio().getNrpnFor(voice.channel, entry.getValue()));
        }
      }
    }
    // END: nrpn
    // BEGIN: ritardando
    {
      Scalar bpm = playScore.bpm;
      TimeSeries timeSeries = TimeSeries.empty(ResamplingMethod.LINEAR_INTERPOLATION);
      // LinearInterpolation linearInterpolation = new LinearInterpolation();
      {
        int lasth = playScore.lastHit(); // ... instead of last release
        // ---
        timeSeries.insert(RealScalar.ZERO, bpm);
        // linearInterpolation.loAlt = SI.PER_MINUTE.doubleValue(bpm);
        timeSeries.insert(RealScalar.of(lasth - ritardandoMeasures * playScore.division.valueAtSafe(1)), bpm);
        timeSeries.insert(RealScalar.of(lasth), bpm.multiply(RealScalar.of(1 - ritardandoReduction * 0.09)));
        // linearInterpolation.hiAlt = linearInterpolation.get(lasth);
      }
      // FIXME TPF disabled because problems at end of score
      // final NavigableMap<Integer, Scalar> tempoMap = new TreeMap<>();
      // // System.out.println(myPlayScore.myDivision);
      // {
      // int ticks = newAudioFirst;
      // Scalar prev = playScore.bpm;
      // while (ticks < newAudioStops) {
      // Scalar value = (Scalar) timeSeries.evaluate(RealScalar.of(ticks));
      // if (prev != value) {
      // tempoMap.put(ticks - newAudioFirst, value);
      // prev = value;
      // }
      // int delta = modus - (ticks % modus);
      // ticks += delta;
      // }
      // }
      // // if (ritardandoMeasures != 0 || ritardandoReduction != 0 || limp != 0)
      // if (!fermataFraction.equals(RationalScalar.ONE)) {
      // // System.out.println("---");
      // // IntRanges<IntRange> fermatas = new IntRanges<>(); // TODO TPF take from getPlayScore function
      // fermatas.intersect(newAudioFirst, newAudioStops);
      // // fermatas.print();
      // for (IntRange intRange : fermatas) {
      // intRange = intRange.translate(-newAudioFirst);
      // for (int ticks : intRange)
      // tempoMap.remove(ticks);
      // tempoMap.put(intRange.min(), bpm.multiply(fermataFraction));
      // tempoMap.put(intRange.max(), bpm);
      // }
      // // myTempoMap.entrySet().stream().forEach(System.out::println);
      // }
      // midiSequence.setExtraTempo(tempoMap);
      // System.out.println("---");
      // myNavigableMap.entrySet().forEach(System.out::println);
      // System.out.println("MAPSIZE " + myNavigableMap.size());
      // if (myNavigableMap.isEmpty())
      // System.out.println("map is empty");
    }
    // END: ritardando
    return midiSequence;
  }

  public void updateSelection(int beg, int end) {
    audio_ticks = beg;
    audio_stops = end;
  }

  public int getNormalVelocity() {
    return master.velocity;
  }

  public int getChordVelocity() {
    return Midi.clip7bit(master.velocity + chordVelocity);
  }

  public static long getTicks(long tickPosition) {
    return tickPosition / FACTOR;
  }

  public static boolean canConvertOriginal(Score score, int ticks, Ornament ornament) {
    return canConvert(score, ticks * FACTOR, ornament);
  }

  private static boolean canConvert(Score score, int ticks, Ornament ornament) {
    final int unit = ornament.getUnit(FACTOR, score.quarter, score.atom);
    return ornament.canConvert(ticks, unit);
  }

  public IntRange getSelectedRange() {
    return new IntRange(audio_ticks, audio_stops);
  }

  public AudioConfiguration getAudioConfiguration(List<Voice> voices) {
    return new AudioConfiguration(voices, accompany, figuredbass);
  }
}
