// code by jph
package ch.alpine.sonata.utl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import ch.alpine.sonata.Division;
import ch.alpine.sonata.Note;
import ch.alpine.sonata.ScoreEntry;
import ch.alpine.sonata.scr.Score;
import sys.mat.IntegerMath;

public class TimescalePacker {
  final Score myScore;
  final boolean withDivision;
  // private
  public int gcd = 0; // seed value 0
  public int ticks;
  public String myString = "";

  public TimescalePacker(Score myScore, boolean withDivision) {
    this.myScore = myScore;
    this.withDivision = withDivision;
    // ---
    for (Entry<ScoreEntry, Note> myEntry : ScoreOps.getNoteEntries(myScore).entrySet()) {
      ScoreEntry myScoreEntry = myEntry.getKey();
      Note note = myEntry.getValue();
      ticks = myScoreEntry.ticks();
      gcd = IntegerMath.gcd(gcd, ticks);
      if (!canPack()) {
        myString = "entry at " + myScoreEntry;
        return;
      }
      gcd = IntegerMath.gcd(gcd, note.ticks());
      if (!canPack()) {
        myString = "note at " + myScoreEntry + " with length " + note.ticks();
        return;
      }
    }
    if (withDivision) {
      gcd = IntegerMath.gcd(gcd, myScore.measure());
      if (canPack())
        if (myScore.division.indexOf(gcd) < 0)
          gcd = 1;
      if (!canPack()) {
        ticks = -1;
        myString = "measure, or division";
        return;
      }
    }
    gcd = IntegerMath.gcd(gcd, myScore.quarter);
    if (!canPack()) {
      ticks = -1;
      myString = "quarter";
      return;
    }
  }

  public boolean canPack() {
    return 1 != gcd;
  }

  public void pack() {
    if (canPack() && gcd != 0) {
      if (withDivision) {
        int index = myScore.division.indexOf(gcd);
        List<Integer> myList = new ArrayList<>();
        for (int myInt : myScore.division.getList().subList(0, index + 1))
          myList.add(myInt / gcd);
        myScore.division = new Division(myList);
      }
      Timescale.pack(myScore, gcd);
      gcd = 1;
    }
  }
}
