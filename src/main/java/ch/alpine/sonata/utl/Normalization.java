// code by jph
package ch.alpine.sonata.utl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import ch.alpine.sonata.Diatone;
import ch.alpine.sonata.Note;
import ch.alpine.sonata.ScoreArray;
import ch.alpine.sonata.Torrent;
import ch.alpine.sonata.jnt.ScoreJoint0;
import ch.alpine.sonata.scr.Score;
import sys.mat.IntRange;
import sys.mat.Ratio;

public enum Normalization {
  ;
  /** @param score to be transposed to C/a
   * @return can be used in scoreModel.transpose to playback at original pitch */
  public static void keySignature0(Score score) {
    ScoreTranspose.keySignature0(score.keySignature).scoreInstance(score);
    Stylist.defaultStyle(score);
  }

  public static void octave(Score score) {
    List<Note> myList = score.allNotes().collect(Collectors.toList());
    if (!myList.isEmpty()) {
      Ratio ratio = new Ratio( //
          myList.stream().mapToInt(note -> note.tone().pitch() * note.ticks()).sum(), //
          myList.stream().mapToInt(note -> note.ticks()).sum());
      int octave = (int) Math.round((score.keySignature.tonic() - ratio.toDouble()) / 12);
      for (Note note : myList)
        note.setTone(note.tone().transposeByOctaves(octave));
      Stylist.defaultStyle(score);
    }
  }

  public static int estimateTonic(Score score, List<Long> myList) {
    myList.clear();
    ScoreArray myScoreArray = ScoreOps.create(score);
    ScoreJoint0 myScoreJoint0 = new ScoreJoint0(myScoreArray, 0, myScoreArray.ticks());
    myScoreJoint0.reduce();
    Map<Integer, Integer> myMap = myScoreJoint0.getPitchMap();
    int[] myInt = new int[12];
    long[] myLong = new long[12]; // result
    for (Entry<Integer, Integer> myEntry : myMap.entrySet())
      myInt[TpfStatics.mod12(myEntry.getKey())] += myEntry.getValue();
    long cmp = 0;
    int tonic = 0;
    for (int c0 : IntRange.positive(12)) { // assume tonica is c0
      for (Diatone diatone : Diatone.values())
        myLong[c0] += myInt[(diatone.white_delta() + c0) % 12];
      myList.add(myLong[c0]);
      if (cmp < myLong[c0]) {
        cmp = myLong[c0];
        tonic = (c0 + 6) % 12 - 6;
      }
      // myList.add(c0 + " " + myLong[c0]);
    }
    return tonic;
  }

  /** computes transpose delta to match myTorrent with myMaterial
   * 
   * @param torrent
   * @param material
   * @return */
  public static int estimateIvoryDelta(Torrent torrent, Torrent material) {
    int min = Math.min(torrent.list.size(), material.list.size());
    int num = 0;
    int den = 0;
    for (int c0 : IntRange.positive(min)) {
      Note myNote = torrent.list.get(c0);
      num += (myNote.tone().ivory().ivory() - material.list.get(c0).tone().ivory().ivory()) * myNote.ticks();
      den += myNote.ticks();
    }
    return (int) Math.round(new Ratio(num, den).toDouble());
  }
}
