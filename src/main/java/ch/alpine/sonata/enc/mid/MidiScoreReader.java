// code by jph
package ch.alpine.sonata.enc.mid;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import ch.alpine.sonata.Joint;
import ch.alpine.sonata.Link;
import ch.alpine.sonata.Relation;
import ch.alpine.sonata.ScoreArray;
import ch.alpine.sonata.ScoreEntry;
import ch.alpine.sonata.jnt.ScoreJoint1;
import ch.alpine.sonata.scr.Score;
import ch.alpine.sonata.scr.Scores;
import ch.alpine.sonata.utl.Duplicates;
import ch.alpine.sonata.utl.ScoreMerge;
import ch.alpine.sonata.utl.Shuffler;
import ch.alpine.sonata.utl.Stylist;
import ch.alpine.tensor.ext.PathName;
import sys.mat.IntRange;

/** does not read lyrics */
class MidiScoreReader {
  protected Score get(Path file) throws Exception {
    MidiProperties midiProperties = null;
    Path managerFile = PathName.of(file).withExtension("properties");
    if (Files.exists(managerFile))
      midiProperties = new MidiProperties(managerFile); // .equip(myScore)
    return get(MidiSystem.getSequence(Files.newInputStream(file)), midiProperties);
  }

  private Score get(Sequence sequence, MidiProperties midiProperties) throws Exception {
    NavigableMap<ScoreEntry, Relation> navigableMap = new TreeMap<>();
    MidiStage midiStage = new MidiStage(sequence);
    Score score = Scores.create(midiStage.voices);
    for (Track track : sequence.getTracks()) {
      for (int _track : IntRange.positive(track.size())) {
        MidiEvent midiEvent = track.get(_track);
        long myLong = midiEvent.getTick();
        MidiMessage midiMessage = midiEvent.getMessage();
        byte[] myByte = midiMessage.getMessage();
        Relation relation = null;
        switch (myByte[0] & 0xf0) {
        case ShortMessage.NOTE_ON:
          relation = 0 == myByte[2] ? Relation.kill(myByte[1]) : Relation.live(myByte[1]);
          break;
        case ShortMessage.NOTE_OFF:
          relation = Relation.kill(myByte[1]);
          break;
        }
        if (Objects.nonNull(relation)) {
          ScoreEntry scoreEntry = new ScoreEntry(midiStage.getTicks(myLong), myByte[0] & 0x0f);
          if (navigableMap.containsKey(scoreEntry)) {
            Relation terminator = navigableMap.get(scoreEntry);
            if (terminator.link.equals(Link.KILL)) {
              if (relation.link.equals(Link.LIVE)) {
                int pitch = terminator.integer0;
                navigableMap.put(scoreEntry, Relation.jump(pitch, relation.integer0 - pitch));
              } else
                reportProblem("collision at " + midiStage.getTicks(myLong));
            } else
              reportProblem("collision at " + midiStage.getTicks(myLong));
          } else
            navigableMap.put(scoreEntry, relation);
        }
      }
    }
    Integer[] hold = new Integer[midiStage.voices];
    NavigableMap<Integer, Joint> jointMap = new TreeMap<>();
    for (long myLong : midiStage.sortedSet) {
      int ticks = midiStage.getTicks(myLong);
      if (!navigableMap.subMap(ScoreEntry.ticksMark(ticks), ScoreEntry.ticksMark(ticks + 1)).isEmpty()) {
        List<Relation> list = new LinkedList<>();
        for (int voice : IntRange.positive(midiStage.voices)) {
          ScoreEntry scoreEntry = new ScoreEntry(ticks, voice);
          if (navigableMap.containsKey(scoreEntry)) {
            Relation relation = navigableMap.get(scoreEntry);
            switch (relation.link) {
            case KILL:
              if (hold[voice] != relation.pitchAnte())
                reportProblem("kill invalid at " + myLong + " = " + scoreEntry);
              break;
            case JUMP:
              if (hold[voice] != relation.pitchAnte())
                reportProblem("jump invalid at " + myLong + " = " + scoreEntry);
              break;
            case LIVE:
              break;
            default:
              // excluded by construction
            }
            list.add(relation);
            hold[voice] = relation.pitchPost();
          } else {
            list.add(hold[voice] == null ? Relation.nada : Relation.hold(hold[voice]));
          }
        }
        jointMap.put(ticks, new Joint(list.stream()));
      }
    }
    midiStage.equip(score);
    // ---
    if (Objects.nonNull(midiProperties))
      midiProperties.equip(score);
    // ---
    ScoreArray scoreArray = ScoreJoint1.toScoreArray(jointMap, midiStage.voices, jointMap.lastKey());
    new ScoreMerge(scoreArray).updateVoices(score); // uses keySignature
    // ---
    Duplicates.removeEmptyVoices(score);
    Shuffler.arrange(score);
    Stylist.defaultStyle(score);
    return score;
  }

  boolean fatal = true;

  void reportProblem(String string) {
    if (fatal)
      throw new RuntimeException(string);
    System.out.println(string);
  }
}
