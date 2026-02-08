// code by jph
package ch.alpine.sonata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class MeterTest {
  private static void dividerTest(Division myDivision) {
    System.out.println(myDivision + " " + Meter.divider(myDivision.getFactorList()));
  }

  @Test
  void testSimple() {
    dividerTest(new Division(Arrays.asList(8, 4, 2, 1)));
    dividerTest(new Division(Arrays.asList(12, 4, 2, 1)));
    dividerTest(new Division(Arrays.asList(12, 6, 2, 1)));
    dividerTest(new Division(Arrays.asList(12, 6, 3, 1)));
    dividerTest(new Division(Arrays.asList(72, 36, 18, 9, 3, 1)));
  }

  // ---
  private static boolean match(int quarter, Division myDivision, Meter myMeter2) {
    Meter myMeter = Meter.of(quarter, myDivision);
    // System.out.println(myMeter);
    boolean myBoolean = myMeter.equals(myMeter2);
    if (!myBoolean) {
      System.out.println(myDivision + " , q=" + quarter + " == " + myMeter + " !=  " + myMeter2);
    }
    return myBoolean;
  }

  private static boolean regressionTest() {
    boolean pass = true;
    pass &= match(2, Division.fromFactorList(Arrays.asList(3, 2, 2)), new Meter(3, 2));
    pass &= match(4, Division.fromFactorList(Arrays.asList(3, 2, 2)), new Meter(3, 4));
    pass &= match(8, Division.fromFactorList(Arrays.asList(3, 2, 2)), new Meter(3, 8));
    // ---
    pass &= match(4, Division.fromFactorList(Arrays.asList(2, 3, 2)), new Meter(6, 8));
    pass &= match(4, Division.fromFactorList(Arrays.asList(2, 2, 3)), new Meter(12, 16));
    // ---
    pass &= match(4, Division.fromFactorList(Arrays.asList(2, 2, 2)), new Meter(2, 4));
    pass &= match(2, Division.fromFactorList(Arrays.asList(2, 2, 2)), new Meter(4, 4));
    pass &= match(1, Division.fromFactorList(Arrays.asList(2, 2, 2)), new Meter(4, 2));
    // pass &= new Meter(Division.fromFactorList(Arrays.asList(new Integer[] { 3, 2, 2 })), 8).equals(new Meter(3, 8));
    return pass;
  }

  @Test
  void testRegression() {
    assertEquals(new Meter(4, 4).toString(), "4/4");
    assertTrue(regressionTest());
  }
}
