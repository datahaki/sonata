// code by jph
package ch.alpine.sonata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** flow defines 28 equivalent classes of pitch differences
 * previous partitioning was [0] [1..4] [5..9] [10..] and the respective negative intervals */
public enum PitchFlow {
  hold(new Integer[] { null }), // null helps the function isIdentical (because jump by delta null == hold)
  unison(new Integer[] { 0 }),
  second_up(new Integer[] { +1, +2 }),
  second_dn(new Integer[] { -1, -2 }),
  third_up(new Integer[] { +3, +4 }),
  third_dn(new Integer[] { -3, -4 }),
  fourth_fifth_up(new Integer[] { +5, +6, +7 }),
  fourth_fifth_dn(new Integer[] { -5, -6, -7 }),
  sixth_up(new Integer[] { +8, +9 }),
  sixth_dn(new Integer[] { -8, -9 }),
  seventh_up(new Integer[] { +10, +11 }),
  seventh_dn(new Integer[] { -10, -11 }),
  octave_up(new Integer[] { +12 }),
  octave_dn(new Integer[] { -12 }),
  second8_up(new Integer[] { +13, +14 }),
  second8_dn(new Integer[] { -13, -14 }),
  third8_up(new Integer[] { +15, +16 }),
  third8_dn(new Integer[] { -15, -16 }),
  fourth8_fifth_up(new Integer[] { +17, +18, +19 }),
  fourth8_fifth_dn(new Integer[] { -17, -18, -19 }),
  sixth8_up(new Integer[] { +20, +21 }),
  sixth8_dn(new Integer[] { -20, -21 }),
  seventh8_up(new Integer[] { +22, +23 }),
  seventh8_dn(new Integer[] { -22, -23 }),
  octave8_up(new Integer[] { +24 }),
  octave8_dn(new Integer[] { -24 }),
  sink_up(new Integer[] { +25, +26, +27, +28, +29, +30, +31, +32, +33, +34, +35, +36, //
      +37, +38, +39, +40, +41, +42, +43, +44, +45, +46, +47, +48, //
      +49, +50, +51, +52, +53, +54, +55, +56, +57, +58, +59, +60, //
      +61, +62, +63, +64, +65, +66, +67, +68, +69, +70, +71, +72 }), //
  sink_dn(new Integer[] { -25, -26, -27, -28, -29, -30, -31, -32, -33, -34, -35, -36, //
      -37, -38, -39, -40, -41, -42, -43, -44, -45, -46, -47, -48, //
      -49, -50, -51, -52, -53, -54, -55, -56, -57, -58, -59, -60, //
      -61, -62, -63, -64, -65, -66, -67, -68, -69, -70, -71, -72 });

  private final List<Integer> list;

  private PitchFlow(Integer[] myInteger) {
    list = List.of(myInteger);
  }

  public List<Integer> list() {
    return list;
  }

  /** cannot be a TreeMap because key can be null */
  public static final Map<Integer, PitchFlow> delta2Flow = new HashMap<>();
  static {
    Set<Integer> set = new HashSet<>();
    for (PitchFlow myFlow : PitchFlow.values())
      for (Integer myInteger : myFlow.list) {
        if (!set.add(myInteger))
          throw new RuntimeException("duplicate " + myInteger);
        delta2Flow.put(myInteger, myFlow);
      }
    for (int pitch = -72; pitch <= 72; ++pitch)
      if (!set.contains(pitch))
        throw new RuntimeException("pitch unassigned " + pitch);
  }

  /** @param myDelta0
   * @param myDelta1
   * @return true if the two pitch differences myDelta0 and myDelta1 are of equivalent class */
  public static boolean isIdentical(Integer myDelta0, Integer myDelta1) {
    return delta2Flow.containsKey(myDelta0) && //
        delta2Flow.containsKey(myDelta1) && //
        delta2Flow.get(myDelta0).equals(delta2Flow.get(myDelta1));
  }
}
