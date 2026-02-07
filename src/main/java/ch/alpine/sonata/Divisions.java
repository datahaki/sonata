// code by jph
package ch.alpine.sonata;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.ext.PackageTestAccess;
import sys.mat.Ratio;

public enum Divisions {
  ;
  public static List<Division> all(int ticks) { // intended for measure and quarter
    List<Division> list = new ArrayList<>();
    List<Integer> myStart = new ArrayList<>();
    myStart.add(ticks);
    Divisions.all(myStart, list);
    return list;
  }

  private static void all(List<Integer> myStart, List<Division> myList) {
    int measure = myStart.get(myStart.size() - 1);
    boolean myBoolean = false;
    if (measure % 3 == 0) {
      List<Integer> myCopy = new ArrayList<>(myStart);
      myCopy.add(measure / 3);
      all(myCopy, myList);
      myBoolean = true;
    }
    if (measure % 2 == 0) {
      List<Integer> myCopy = new ArrayList<>(myStart);
      myCopy.add(measure / 2);
      all(myCopy, myList);
      myBoolean = true;
    }
    if (measure % 5 == 0) {
      List<Integer> myCopy = new ArrayList<>(myStart);
      myCopy.add(measure / 5);
      all(myCopy, myList);
      myBoolean = true;
    }
    if (measure % 7 == 0) {
      List<Integer> myCopy = new ArrayList<>(myStart);
      myCopy.add(measure / 7);
      all(myCopy, myList);
      myBoolean = true;
    }
    if (!myBoolean)
      myList.add(new Division(myStart));
  }

  @PackageTestAccess
  static List<Division> forMeter(int quarter, int measure, Ratio ratio) {
    Meter meter = Meter.of(ratio, quarter, measure); // in order to standardize input
    List<Division> list = all(measure).stream().collect(Collectors.toList());
    return list.size() == 1 ? list
        : list.stream() //
            .filter(division -> Meter.of(quarter, division).equals(meter)) //
            .collect(Collectors.toList());
  }

  // TODO NOTATION Chopin_Preludes.prelude28-01 q=30 m=30 ratio=2/8 !!!
  public static Division best(int quarter, int measure, String ratio) {
    try {
      return best(quarter, measure, Ratio.of(Scalars.fromString(ratio)));
    } catch (Exception exception) {
      System.out.println("q=" + quarter + " m=" + measure + " ratio=" + ratio);
      exception.printStackTrace();
    }
    return Division.FALLBACK;
  }

  public static Division best(int quarter, int measure, Ratio ratio) {
    try {
      List<Division> list = Divisions.forMeter(quarter, measure, ratio);
      return list.get(list.size() - 1);
    } catch (Exception exception) {
      System.out.println("q=" + quarter + " m=" + measure + " ratio=" + ratio);
      exception.printStackTrace();
    }
    return Division.FALLBACK;
  }
}
