// code by jph
package ch.alpine.sonata;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import ch.alpine.tensor.ext.Integers;
import sys.mat.IntRange;

public enum CircleFifths {
  ;
  private static final List<DiatoneAlter> LIST = Stream.of(QuintenZirkel.values()) //
      .map(QuintenZirkel::diatoneAlter) //
      .toList();

  public static List<DiatoneAlter> all() {
    return LIST;
  }

  // ---
  public static Map<Natur, DiatoneAlter> subMap(int type, int length) {
    Map<Natur, DiatoneAlter> map = new EnumMap<>(Natur.class);
    for (int index : IntRange.positive(length)) {
      DiatoneAlter diatoneAlter = QuintenZirkel.typeToDiatoneAlter(type + index);
      map.put(diatoneAlter.natur(), diatoneAlter);
    }
    Integers.requireEquals(map.size(), length);
    return map;
  }
}
