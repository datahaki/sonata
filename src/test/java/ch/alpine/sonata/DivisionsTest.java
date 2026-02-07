package ch.alpine.sonata;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;

import sys.mat.Ratio;

class DivisionsTest {
  @RepeatedTest(9)
  void testAll(RepetitionInfo repetitionInfo) {
    int c1 = repetitionInfo.getCurrentRepetition();
    System.out.println(Divisions.all(c1));
  }

  @Test
  void testCases() {
    Divisions.forMeter(4 * 2 * 3, 12 * 2 * 3, new Ratio(6, 8)).forEach(System.out::println);
    System.out.println("---");
    // ---
    Divisions.forMeter(4, 16 + 8, new Ratio(3, 2)).forEach(System.out::println);
    Divisions.forMeter(4, 16 + 8, new Ratio(6, 4)).forEach(System.out::println);
    System.out.println("---");
    // ---
    Divisions.forMeter(8, 32, new Ratio(1, 1)).forEach(System.out::println);
    Divisions.forMeter(8, 32, new Ratio(2, 2)).forEach(System.out::println);
    Divisions.forMeter(8, 32, new Ratio(4, 4)).forEach(System.out::println);
    System.out.println("---");
    {
      System.out.println("out 1");
      Divisions.forMeter(6, 48, new Ratio(8, 4)).forEach(System.out::println);
      System.out.println("out 2");
      System.out.println(Divisions.best(6, 48, "8/4"));
      System.out.println(Divisions.best(8, 64, "8/4"));
      Meter myMeter = Meter.of(new Ratio(8, 4), 8, 64); // in order to standardize input
      System.out.println(myMeter);
      Divisions.all(64).forEach(System.out::println);
      Divisions.all(64).stream() //
          .map(myD -> Meter.of(8, myD)) //
          .forEach(System.out::println);
    }
    {
      // Division myDivision = Division.fromFactorList(Arrays.asList(new Integer[] {}));
      // System.out.println("here " + myDivision.myList);
      System.out.println("--- THIS IS IT");
      for (Division myDivision : Divisions.all(12))
        System.out.println(myDivision.getFactorList() + " " + Meter.of(8, myDivision));
      // ---
      System.out.println("---");
      for (Division myDivision : Divisions.all(12))
        System.out.println(myDivision.getFactorList() + " " + Meter.of(4, myDivision));
      System.out.println("---");
      for (Division myDivision : Divisions.all(48))
        System.out.println(myDivision.getFactorList() + " " + Meter.of(12, myDivision));
      for (Division myDivision : Divisions.all(9))
        System.out.println(myDivision.getFactorList() + " " + Meter.of(2, myDivision));
      System.out.println("---");
      for (Division myDivision : Divisions.all(32)) {
        System.out.println(Meter.of(8, myDivision).toString());
      }
      // System.out.println(myDivision.meterString(6));
    }
  }
}
