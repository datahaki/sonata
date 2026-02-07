// code by jph
package ch.alpine.sonata.lyr;

import sys.mat.IntRange;

public class LyricString {
  public final int pass;
  public IntRange ticksRange;
  public String string = "";

  public LyricString(int pass) {
    this.pass = pass;
  }

  @Override
  public boolean equals(Object myObject) {
    LyricString myLyricString = (LyricString) myObject;
    boolean equals = pass == myLyricString.pass;
    equals &= ticksRange.equals(myLyricString.ticksRange);
    equals &= string.equals(myLyricString.string);
    return equals;
  }

  @Override
  public int hashCode() {
    return pass + ticksRange.min() + string.hashCode();
  }
}
