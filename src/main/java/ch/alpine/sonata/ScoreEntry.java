// code by jph
package ch.alpine.sonata;

import java.util.Properties;

import sys.mat.OrderedTuple;

/** immutable */
public class ScoreEntry extends OrderedTuple {
  private static final int voiceMark = -1;

  /** @param ticks non negative
   * @param voice non negative, typically between 0, 1, ..., 4 */
  public ScoreEntry(int ticks, int voice) {
    super(ticks, voice);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected ScoreEntry create(int ticks, int voice) {
    return new ScoreEntry(ticks, voice);
  }

  public int ticks() {
    return x;
  }

  public int voice() {
    return y;
  }

  /** use for instance as <code>myNavigableMap.subMap(ScoreEntry.ticksMark(10), ScoreEntry.ticksMark(15))</code>
   * 
   * @param ticks
   * @return */
  public static ScoreEntry ticksMark(int ticks) {
    return new ScoreEntry(ticks, voiceMark);
  }

  public ScoreEntry shift(int delta) {
    return new ScoreEntry(ticks() + delta, voice());
  }

  /** no spaces are allowed,
   * keys of {@link Properties} don't contain whitespace
   * 
   * @param string
   * @return */
  public static ScoreEntry fromString(String string) {
    String[] split = string.split(",");
    return new ScoreEntry( //
        Integer.parseInt(split[0].substring(1)), //
        Integer.parseInt(split[1].substring(0, split[1].length() - 1)));
  }
}
