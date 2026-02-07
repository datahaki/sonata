// code by jph
package ch.alpine.sonata;

/** link is used in Relation to encode an instantaneous change in a voice;
 * order of links affects the sorting of Relations */
public enum Link {
  NADA(false), // ---- ----
  HOLD(true), // int0 ---- note is held
  LIVE(false), // int0 ---- note is created after a pause?
  KILL(false), // int0 ---- note ends and followed by pause?
  JUMP(true); // int0 int1 note changes

  // ---
  public final boolean isComplete;

  private Link(boolean isComplete) {
    this.isComplete = isComplete;
  }
}
