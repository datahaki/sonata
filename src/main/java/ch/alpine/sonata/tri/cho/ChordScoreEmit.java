// code by jph
package ch.alpine.sonata.tri.cho;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import ch.alpine.sonata.FiguredBass;
import ch.alpine.sonata.Joint;
import ch.alpine.sonata.Natur;
import ch.alpine.sonata.Note;
import ch.alpine.sonata.Tone;
import ch.alpine.sonata.Torrent;
import ch.alpine.sonata.Voice;
import ch.alpine.sonata.enc.utl.CoverageBundle;
import ch.alpine.sonata.hrm.Theory;
import ch.alpine.sonata.jnt.ScoreJoint;
import ch.alpine.sonata.jnt.ScoreJoint1;
import ch.alpine.sonata.scr.Score;
import ch.alpine.sonata.tri.Chord;
import ch.alpine.sonata.tri.Triad;
import ch.alpine.sonata.utl.FiguredTones;

public class ChordScoreEmit {
  /** duration of chord is quarter
   * 
   * @param score template
   * @param set
   * @param velocity between 0 and 127
   * @return */
  public static Score singleChord(Score score, Set<Natur> set, ChordScore chordScore, int velocity) {
    chordScore.initFromScore(score);
    chordScore.put(0, score.quarter, set, velocity);
    return chordScore.getScore();
  }

  /** @param score read-only
   * @param chordScore used for output generation
   * @param filter out chords that are dissonant with the respective notes
   * @param velocity between 0 and 127
   * @return score with notes of chords of myScore */
  public static Score accompaniment(Score score, ChordScore chordScore, boolean filter, int velocity) {
    final boolean excludeCommon = true; // excludes notes(=natur) of chords that already exist in score at the same time
    CoverageBundle coverageBundle = CoverageBundle.fromChords(score.triad); // joins adjacent identical map entries to interval entry
    ScoreJoint scoreJoint = ScoreJoint1.create(score.voices, 0, score.ticks());
    NavigableSet<Integer> navigableSet = new TreeSet<>();
    {
      navigableSet.addAll(scoreJoint.navigableMap.keySet());
      // only entries play a role, but not durations; use of format is a waste:
      navigableSet.addAll(coverageBundle.projectOnsets().keySet());
      Integer myEnd = coverageBundle.getEnd();
      if (myEnd != null && score.ticks() < myEnd)
        navigableSet.add(myEnd);
    }
    chordScore.initFromScore(score); // TODO TPF potentially should be called somewhere else
    for (Entry<Integer, Triad> entry : coverageBundle.projectOnsets().entrySet()) {
      int ticks = entry.getKey();
      Chord chord = Theory.uniform.getChord(entry.getValue());
      final int higher = navigableSet.higher(ticks);
      boolean myBoolean = true;
      if (filter) { // prevents chords from playing that are dissonant with the score
        Entry<Integer, Joint> soundEntry = scoreJoint.navigableMap.lowerEntry(ticks + 1);
        if (soundEntry != null) {
          Set<Natur> set = soundEntry.getValue().pitchPostUnsorted().map(Natur::fromPitch).collect(Collectors.toSet());
          myBoolean = chord.covering().containsAll(set); // TODO TPF extract functionality to function of Chord
        }
      }
      if (myBoolean) {
        Set<Natur> set = new HashSet<>(chord.triad().set());
        if (excludeCommon) {
          Entry<Integer, Joint> jointEntry = scoreJoint.navigableMap.lowerEntry(ticks + 1);
          if (jointEntry != null)
            jointEntry.getValue().pitchPostUnsorted().map(Natur::fromPitch).forEach(set::remove);
        }
        chordScore.put(ticks, higher - ticks, set, velocity);
      }
    }
    return chordScore.getScore();
  }

  /** @param score read-only
   * @param chordScore used for output generation
   * @param filter out chords that are dissonant with the respective notes
   * @param velocity between 0 and 127
   * @return score with notes of chords of myScore */
  public static Score accompanimentFigured(Score score, ChordScore chordScore, int velocity) {
    final boolean excludeCommon = true; // excludes notes(=natur) of chords that already exist in score at the same time
    // CoverageBundle myCoverageBundle = CoverageBundle.fromChords(myScore.triad); // joins adjacent identical map entries to interval entry
    ScoreJoint scoreJoint = ScoreJoint1.create(score.voices, 0, score.ticks());
    NavigableSet<Integer> navigableSet = new TreeSet<>();
    {
      navigableSet.addAll(scoreJoint.navigableMap.keySet());
      // only entries play a role, but not durations; use of format is a waste:
      // myNavigableSet.addAll(myCoverageBundle.projectOnsets().keySet());
      // Integer myEnd = myCoverageBundle.getEnd();
      // if (myEnd != null && myScore.ticks() < myEnd)
      // myNavigableSet.add(myEnd);
    }
    chordScore.initFromScore(score); // TODO TPF potentially should be called somewhere else
    final int index = score.voices.size() - 1;
    Voice voice = score.voices.get(index);
    navigableSet.addAll(voice.fbass.keySet()); // FIXME TPF still needs to make sure that highest != null
    for (Entry<Integer, FiguredBass> entry : voice.fbass.entrySet()) {
      final int ticks = entry.getKey();
      FiguredBass figuredBass = entry.getValue();
      final int higher = navigableSet.higher(ticks);
      Note note = voice.getNote(ticks, false);
      if (note != null) {
        Set<Natur> set = new HashSet<>();
        FiguredTones figuredTones = new FiguredTones(score.keySignature, note.tone());
        for (Tone tone : figuredTones.getTones( //
            // FiguredBase.getCompleteBass(myFiguredBass) //
            figuredBass //
        ))
          set.add(Natur.fromPitch(tone.pitch()));
        if (excludeCommon) {
          Entry<Integer, Joint> jointEntry = scoreJoint.navigableMap.lowerEntry(ticks + 1);
          if (jointEntry != null)
            jointEntry.getValue().pitchPostUnsorted().map(Natur::fromPitch).forEach(set::remove);
        }
        chordScore.put(ticks, higher - ticks, set, velocity);
      }
    }
    return chordScore.getScore();
  }

  public static Score neutralize(Score score, ChordScore chordScore, Integer velocity) {
    chordScore.initFromScore(score);
    for (Voice voice : score.voices)
      for (Entry<Integer, Torrent> entry : voice) {
        int ticks = entry.getKey();
        Torrent torrent = entry.getValue();
        final int vel = velocity == null ? torrent.attributes.velocity : velocity;
        for (Note note : torrent) {
          chordScore.put(ticks, note.ticks(), EnumSet.of(note.tone().diatoneAlter().natur()), vel);
          ticks += note.ticks();
        }
      }
    return chordScore.getScore();
  }
  // @Deprecated
  // private static Score reduction(Score myScore, ChordScore chordScore, int velocity) { // wrong place
  // ScoreJoint myScoreJoint = ScoreJoint1.create(myScore.voices, 0, myScore.ticks());
  // Theory myTheory = TheoryType.uniform.getTheory(myScore.keySignature, myScore.keyMode());
  // ScoreChords myScoreChordsAmbience = ScoreChords.instance(myTheory).updateCoverage(myScore);
  // CoverageBundle myCoverageBundle = new CoverageBundle(myScoreChordsAmbience);
  // final NavigableMap<Integer, Triad> myNavigableMap = myCoverageBundle.projectOnsets();
  // HeptaCoverage heptaCoverage = new HeptaCoverage(myScore);
  // // ---
  // NavigableSet<Integer> myNavigableSet = new TreeSet<>();
  // {
  // myNavigableSet.addAll(myScoreJoint.navigableMap.keySet());
  // myNavigableSet.addAll(myCoverageBundle.navigableMap.keySet()); // only entries play a role, but not durations; use of format is a waste
  // Integer myEnd = myCoverageBundle.getEnd();
  // if (myEnd != null && myScore.ticks() < myEnd)
  // myNavigableSet.add(myEnd);
  // }
  // NavigableMap<Integer, Set<Natur>> naturMap = new TreeMap<>();
  // myNavigableSet.forEach(ticks -> naturMap.put(ticks, EnumSet.noneOf(Natur.class)));
  // // ---
  // chordScore.initFromScore(myScore);
  // {
  // Hepta myHeptaPrev = null;
  // for (Entry<Integer, Hepta> myEntry : heptaCoverage.navigableMap.entrySet()) {
  // int ticks = myEntry.getKey();
  // Hepta myHepta = myEntry.getValue();
  // if (myHeptaPrev != null) {
  // Set<Natur> mySet = new HashSet<>(myHepta.getNaturMap().keySet());
  // mySet.removeAll(myHeptaPrev.getNaturMap().keySet());
  // System.out.println(mySet);
  // naturMap.get(ticks).addAll(mySet);
  // }
  // System.out.println(myHepta);
  // myHeptaPrev = myHepta;
  // }
  // }
  // {
  // for (Entry<Integer, Triad> entry : myNavigableMap.entrySet()) {
  // int ticks = entry.getKey();
  // // Integer higher = myNavigableSet.higher(ticks);
  // Chord chord = Theory.uniform.getChord(entry.getValue());
  // naturMap.get(ticks).addAll(chord.triad.set());
  // }
  // }
  // {
  // for (Entry<Integer, Hepta> myEntry : heptaCoverage.navigableMap.entrySet()) {
  // int ticks = myEntry.getKey();
  // Hepta hepta = myEntry.getValue();
  // Integer higher = heptaCoverage.navigableMap.higherKey(ticks);
  // if (higher != null) {
  // Set<Natur> available = new HashSet<>(hepta.getNaturMap().keySet());
  // Set<Natur> set = naturMap.subMap(ticks, higher).entrySet().stream().flatMap(e -> e.getValue().stream()).collect(Collectors.toSet());
  // available.removeAll(set);
  // for (Entry<Integer, Set<Natur>> vacant : naturMap.subMap(ticks, higher).entrySet()) {
  // if (vacant.getValue().isEmpty() && !available.isEmpty()) {
  // List<Natur> myList = new ArrayList<>(available);
  // Collections.shuffle(myList);
  // Natur myNatur = myList.iterator().next();
  // available.remove(myNatur);
  // naturMap.get(vacant.getKey()).add(myNatur);
  // }
  // }
  // }
  // }
  // }
  // naturMap.entrySet().forEach(System.out::println);
  // {
  // for (Entry<Integer, Set<Natur>> entry : naturMap.entrySet()) {
  // int ticks = entry.getKey();
  // Integer higher = naturMap.higherKey(ticks);
  // chordScore.put(ticks, higher == null ? 1 : higher - ticks, entry.getValue(), velocity);
  // }
  // }
  // return chordScore.getScore();
  // }
}
