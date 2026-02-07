// code by jph
package ch.alpine.sonata;

import java.util.List;

public enum Metric {
  ;
  public static final List<Integer> quartersList = List.of( //
      1, 2, 3, 4, 6, 8, 12, 16, 24, 28, 30, 32, 48);
  public static final int measureMax = 192;
  public static final List<Integer> measuresList = List.of( //
      2, 3, 4, 5, 6, 8, 9, 10, 12, 16, 18, 20, 24, 30, 32, 36, 40, 48, 64, 72, 96, 112, 128, 144, measureMax);
}
