// code by jph
package ch.alpine.sonata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public enum IvoryFlow {
  hold(new Integer[] { null }), // null helps the function isIdentical (because jump by delta null == hold)
  unison(0), //
  second_up(+1), //
  second_dn(-1), //
  third_up(+2), //
  third_dn(-2), //
  fourth_fifth_up(+3, +4), //
  fourth_fifth_dn(-3, -4), //
  sixth_up(+5), //
  sixth_dn(-5), //
  seventh_up(+6), //
  seventh_dn(-6), //
  octave_up(+7), //
  octave_dn(-7), //
  second8_up(+8), //
  second8_dn(-8), //
  third8_up(+9), //
  third8_dn(-9), //
  fourth8_fifth_up(+10, +11), //
  fourth8_fifth_dn(-10, -11), //
  sixth8_up(+12), //
  sixth8_dn(-12), //
  seventh8_up(+13), //
  seventh8_dn(-13), //
  octave8_up(+14), //
  octave8_dn(-14), //
  sink_up(//
      +15, +16, +17, +18, +19, +20, +21, +22, +23, +24, //
      +25, +26, +27, +28, +29, +30, +31, +32, +33, +34, +35, +36, //
      +37, +38, +39, +40, +41, +42, +43, +44, +45, +46, +47, +48, //
      +49, +50, +51, +52, +53, +54, +55, +56, +57, +58, +59, +60, //
      +61, +62, +63, +64, +65, +66, +67, +68, +69, +70, +71, +72), //
  sink_dn(//
      -15, -16, -17, -18, -19, -20, -21, -22, -23, -24, //
      -25, -26, -27, -28, -29, -30, -31, -32, -33, -34, -35, -36, //
      -37, -38, -39, -40, -41, -42, -43, -44, -45, -46, -47, -48, //
      -49, -50, -51, -52, -53, -54, -55, -56, -57, -58, -59, -60, //
      -61, -62, -63, -64, -65, -66, -67, -68, -69, -70, -71, -72); //
  // ---

  public final List<Integer> list;

  private IvoryFlow(Integer... myInteger) {
    list = List.of(myInteger);
  }

  /** cannot be a TreeMap because key can be null */
  public static final Map<Integer, IvoryFlow> delta2Flow = new HashMap<>();
  static {
    Set<Integer> set = new HashSet<>();
    for (IvoryFlow ivoryFlow : IvoryFlow.values())
      for (Integer myInteger : ivoryFlow.list) {
        if (!set.add(myInteger))
          throw new RuntimeException("duplicate " + myInteger);
        delta2Flow.put(myInteger, ivoryFlow);
      }
    for (int ivory = -72; ivory <= 72; ++ivory)
      if (!set.contains(ivory))
        throw new RuntimeException("ivory unassigned " + ivory);
  }
}
