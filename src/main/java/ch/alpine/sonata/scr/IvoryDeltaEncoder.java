// code by jph
package ch.alpine.sonata.scr;

import ch.alpine.sonata.IvoryFlow;
import ch.alpine.sonata.Note;

public enum IvoryDeltaEncoder {
  ;
  public static int encodeJump(Note prev, Note note) {
    int delta = encodeJumpExact(prev, note); // myPrev.myIvory.ivory - myNote.myIvory.ivory;
    IvoryFlow myIvoryFlow = IvoryFlow.delta2Flow.get(delta);
    if (myIvoryFlow == null) {
      new RuntimeException("delta is not encoded").printStackTrace();
      return 0;
    }
    return myIvoryFlow.ordinal();
  }

  public static int encodeJumpExact(Note prev, Note note) {
    return prev.tone().ivory().ivory() - note.tone().ivory().ivory();
  }
}
