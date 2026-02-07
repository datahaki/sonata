// code by jph
package ch.alpine.sonata.enc.utl;

/** estimates measure uses bar lines to keep track of correct tick count */
public class BarLine {
  private int bar1;
  private int bar2;
  private int count = 0;

  public int markAt(int ticks, boolean strict) throws Exception {
    switch (count) {
    case 1:
      bar1 = ticks;
      break;
    case 2:
      bar2 = ticks;
      break;
    }
    if (2 < count) {
      int measure = getMeasure();
      int remain = Math.floorMod(measure - (ticks - bar1), measure);
      if (remain != 0) {
        if (strict)
          throw new RuntimeException(String.format("ticks=%d violate bar line mod1=%d measure=%d", ticks, bar1, getMeasure()));
        // System.out.println("!!! advance " + remain + " m=" + measure);
        ticks += remain;
      }
    }
    ++count;
    return ticks;
  }

  public boolean hasPartial() {
    return bar1 * 2 < bar2;
  }

  public int getDelay() {
    return hasPartial() ? getMeasure() - bar1 : 0;
  }

  public int getMeasure() {
    if (count < 2)
      throw new RuntimeException("measure cannot be established");
    return bar2 - bar1;
  }

  public int getBarCount() {
    return count;
  }

  /** introduced for import of scores with meter change
   * 
   * @param ticks
   * @param measure */
  public void changeTo(int ticks, int measure) {
    count = Math.max(count, 2);
    bar1 = ticks - 2 * measure;
    bar2 = ticks - measure;
  }
}
