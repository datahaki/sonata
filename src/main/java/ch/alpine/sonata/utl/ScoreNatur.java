// code by jph
package ch.alpine.sonata.utl;

import java.util.EnumSet;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import ch.alpine.sonata.Figure;
import ch.alpine.sonata.FiguredBass;
import ch.alpine.sonata.Joint;
import ch.alpine.sonata.KeySignature;
import ch.alpine.sonata.Natur;
import ch.alpine.sonata.Note;
import ch.alpine.sonata.Voice;
import ch.alpine.sonata.jnt.ScoreJoint;
import ch.alpine.sonata.scr.Score;

/** purpose of class is to help compute chord coverage
 * from {@link Note}s as well as {@link FiguredBass} */
public class ScoreNatur {
  public final NavigableMap<Integer, Set<Natur>> navigableMap = new TreeMap<>();

  public ScoreNatur(ScoreJoint scoreJoint) {
    for (Entry<Integer, Joint> entry : scoreJoint.navigableMap.entrySet()) {
      int ticks = entry.getKey();
      Joint joint = entry.getValue();
      navigableMap.put(ticks, joint.pitchPostUnsortedList().stream() //
          .map(Natur::fromPitch) //
          .collect(Collectors.toCollection(() -> EnumSet.noneOf(Natur.class))));
    }
  }

  public void addFiguredBass(Score score) {
    if (!navigableMap.isEmpty()) {
      KeySignature keySignature = score.keySignature;
      {
        NavigableSet<Integer> navigableSet = new TreeSet<>();
        for (Voice voice : score.voices)
          navigableSet.addAll(voice.fbass.keySet());
        {
          Integer first = navigableMap.firstKey();
          navigableSet.removeAll(navigableMap.keySet());
          for (int ticks : navigableSet.tailSet(first)) {
            Entry<Integer, Set<Natur>> entry = navigableMap.lowerEntry(ticks);
            navigableMap.put(ticks, entry.getValue());
          }
        }
      }
      // ---
      for (Voice voice : score.voices)
        for (Entry<Integer, FiguredBass> entry : voice.fbass.entrySet()) {
          int ticks = entry.getKey();
          Note note = voice.getNote(ticks, false);
          if (note != null) {
            FiguredBass figuredBass = entry.getValue();
            FiguredTones figuredTones = new FiguredTones(keySignature, note.tone());
            Set<Natur> set = navigableMap.get(ticks);
            for (Figure figure : figuredBass.figures())
              set.add(Natur.fromPitch(figuredTones.getTone(figure).pitch()));
          }
        }
    }
  }
}
