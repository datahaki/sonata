// code by jph
package ch.alpine.sonata.jnt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Optional;

import ch.alpine.sonata.Joint;
import ch.alpine.sonata.Note;
import ch.alpine.sonata.Relation;
import ch.alpine.sonata.ScoreArray;
import ch.alpine.sonata.Voice;
import sys.mat.IntRange;

public class ScoreJoint1 extends ScoreJoint {
  public static ScoreJoint1 create(List<Voice> myVoices) {
    return create(myVoices, 0, 0);
  }

  /** produces reduced ScoreJoint
   * 
   * @param myVoices
   * @param beg IGNORED
   * @param end IGNORED */
  public static ScoreJoint1 create(List<Voice> myVoices, int beg, int end) { // WARNING: beg end are ignored
    Map<Integer, List<Relation>> map = new HashMap<>();
    NavigableSet<Integer> navigableSet = ScoreArrays.getAlts(myVoices);
    for (int ticks : navigableSet)
      map.put(ticks, new ArrayList<>());
    // myNavigableMap.put(ticks, new Joint());
    for (Voice myVoice : myVoices) {
      Entry<Integer, Note> myPrev = null;
      for (Entry<Integer, Note> myEntry : myVoice.getNoteMapAbsolute().entrySet()) {
        int ticks = myEntry.getKey();
        Note note = myEntry.getValue();
        if (myPrev != null) {
          if (myPrev.getKey() + myPrev.getValue().ticks() < ticks) {
            map.get(myPrev.getKey() + myPrev.getValue().ticks()).add(Relation.kill(myPrev.getValue().tone().pitch()));
            map.get(ticks).add(Relation.live(note.tone().pitch()));
          } else {
            int pitch = myPrev.getValue().tone().pitch();
            map.get(ticks).add(Relation.jump(pitch, note.tone().pitch() - pitch));
          }
        } else
          map.get(ticks).add(Relation.live(note.tone().pitch()));
        for (int immed : navigableSet.subSet(ticks + 1, ticks + note.ticks()))
          map.get(immed).add(Relation.hold(note.tone().pitch()));
        myPrev = myEntry;
      }
      if (myPrev != null)
        map.get(myPrev.getKey() + myPrev.getValue().ticks()).add(Relation.kill(myPrev.getValue().tone().pitch()));
    }
    ScoreJoint1 scoreJoint1 = new ScoreJoint1();
    for (Entry<Integer, List<Relation>> myEntry : map.entrySet())
      scoreJoint1.navigableMap.put(myEntry.getKey(), new Joint(myEntry.getValue()));
    return scoreJoint1;
  }

  public ScoreJoint1() {
  }

  /** preserves rests in score, joints only at alts
   * 
   * @param scoreArray
   * @param beg
   * @param end */
  public ScoreJoint1(ScoreArray scoreArray, int beg, int end) {
    for (int ticks : scoreArray.getAlts(beg, end))
      navigableMap.put(ticks, getJoint1At(scoreArray, ticks));
  }

  /** joint at every ticks
   * 
   * @param scoreArray
   * @param intRange */
  public ScoreJoint1(ScoreArray scoreArray, IntRange intRange) {
    for (int ticks : intRange)
      navigableMap.put(ticks, getJoint1At(scoreArray, ticks));
  }

  public static Joint getJoint1At(ScoreArray scoreArray, final int ticks) {
    List<Relation> list = new ArrayList<>();
    for (int voice : IntRange.positive(scoreArray.voices()))
      if (ticks == 0 || scoreArray.getPitchUnsafe(voice, ticks - 1) == null) {
        if (scoreArray.getPitchUnsafe(voice, ticks) == null)
          // [ ] [ ] => nada
          list.add(Relation.nada);
        else
          // [ ] [X.] => live
          list.add(Relation.live(scoreArray.getPitchUnsafe(voice, ticks).pitch()));
      } else {
        if (scoreArray.getPitchUnsafe(voice, ticks) == null) // [X] [ ] => kill
          list.add(Relation.kill(scoreArray.getPitchUnsafe(voice, ticks - 1).pitch()));
        else {
          if (scoreArray.getPitchUnsafe(voice, ticks).isHits()) { // [X] [Y.] => jump
            final int pitch = scoreArray.getPitchUnsafe(voice, ticks - 1).pitch();
            list.add(Relation.jump(pitch, scoreArray.getPitchUnsafe(voice, ticks).pitch() - pitch));
          } else { // [X] [X] => hold
            if (scoreArray.getPitchUnsafe(voice, ticks - 1).pitch() != scoreArray.getPitchUnsafe(voice, ticks).pitch()) {
              // TODO TPF java.lang.RuntimeException: collision at (1,18-19)
              System.out.println(scoreArray + "\n");
              throw new RuntimeException("collision at (" + voice + "," + (ticks - 1) + "-" + ticks + ")");
            }
            list.add(Relation.hold(scoreArray.getPitchUnsafe(voice, ticks - 1).pitch()));
          }
        }
      }
    return new Joint(list);
  }

  public static ScoreArray toScoreArray(NavigableMap<Integer, Joint> navigableMap, int voices, int total_ticks) {
    ScoreArray scoreArray = new ScoreArray(voices, total_ticks);
    for (Entry<Integer, Joint> entry : navigableMap.entrySet()) {
      int beg = entry.getKey();
      int end = Optional.ofNullable(navigableMap.higherKey(beg)).orElse(total_ticks);
      int voice = 0;
      for (Relation relation : entry.getValue()) {
        switch (relation.link) {
        case NADA:
        case KILL:
          break;
        case HOLD:
          for (int ticks = beg; ticks < end; ++ticks)
            scoreArray.set(voice, ticks, relation.integer0, false);
          break;
        case LIVE:
          for (int ticks = beg; ticks < end; ++ticks)
            scoreArray.set(voice, ticks, relation.integer0, ticks == beg);
          break;
        case JUMP:
          for (int ticks = beg; ticks < end; ++ticks)
            scoreArray.set(voice, ticks, relation.integer0 + relation.integer1, ticks == beg);
          break;
        }
        ++voice;
      }
    }
    return scoreArray;
  }
}
