// code by jph
package ch.alpine.sonata.enc.krn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import ch.alpine.sonata.Note;
import ch.alpine.sonata.io.KernNoteFormat;
import ch.alpine.sonata.scr.ScoreBuffer;
import sys.dat.UniqueValue;

class KernColumn {
  int ticks = 0;
  SortedSet<Integer> sortedSet = new TreeSet<>(); // holds ticks at measure bars

  KernColumn(List<String> list, int basis, final int measure, int voice_offset, ScoreBuffer scoreBuffer) throws Exception {
    String myMeasure = "";
    UniqueValue<Integer> modulus = UniqueValue.empty();
    Map<Integer, Integer> tieMap = new HashMap<>();
    for (String column : list) {
      String[] split = column.split(" ");
      int voice = 0;
      UniqueValue<Integer> uniqueValue = UniqueValue.empty();
      for (String string : split) {
        if (string.startsWith("*")) {
          // void
        } else //
        if (string.startsWith("!")) {
          // void
        } else //
        if (string.matches("=\\d+")) {
          int abs = ticks;
          sortedSet.add(abs);
          myMeasure = string + " -> " + abs;
          myMeasure.toLowerCase(); // dummy to prevent warning
          try {
            modulus.set(abs % measure);
          } catch (Exception exception) {
            throw new RuntimeException("ticks " + modulus + " vs. " + (abs % measure) + " at measure " + string + " absolute " + abs);
          }
        } else {
          if (KernNoteFormat.containsNote(string)) {
            KernNoteFormat kernNoteFormat = new KernNoteFormat(basis);
            Note note = kernNoteFormat.parseNote(string);
            if (0 <= string.indexOf('['))
              tieMap.put(note.tone().pitch(), voice);
            if (0 <= string.indexOf(']')) {
              if (!tieMap.containsKey(note.tone().pitch()))
                throw new RuntimeException("untie has no match for " + note + " in column " + column);
              int myInt = tieMap.get(note.tone().pitch());
              if (myInt != voice)
                throw new RuntimeException("untie has match in different voice in column " + column);
              tieMap.remove(note.tone().pitch());
            }
            boolean tie = tieMap.containsKey(note.tone().pitch()) && tieMap.get(note.tone().pitch()) == voice;
            try {
              scoreBuffer.put(note, tie, ticks, voice_offset + voice);
            } catch (Exception exception) {
              scoreBuffer.untie(voice_offset + voice);
              scoreBuffer.put(note, tie, ticks, voice_offset + voice);
            }
            uniqueValue.set(note.ticks());
          }
          if (KernNoteFormat.containsRest(string)) {
            uniqueValue.set(KernNoteFormat.getTicks(string, basis));
          }
        }
        ++voice;
      }
      if (uniqueValue.isPresent())
        ticks += uniqueValue.orElseThrow();
    }
  }
}
