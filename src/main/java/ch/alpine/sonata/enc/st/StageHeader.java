// code by jph
package ch.alpine.sonata.enc.st;

import java.util.HashSet;
import java.util.Set;

import ch.alpine.sonata.KeySignature;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalars;
import sys.dat.UniqueValue;

class StageHeader {
  String comment;
  /** type of {@link KeySignature} */
  final Set<Integer> type = new HashSet<>();
  final UniqueValue<Integer> quarter = UniqueValue.empty();
  final UniqueValue<String> meter = UniqueValue.empty();

  int getMeasure() {
    return Scalars.intValueExact(Scalars.fromString(meter.orElseThrow()).multiply(RealScalar.of(4 * quarter.orElseThrow())));
  }

  /** only assigned by {@link Stage1Reader} */
  int measure_st1;

  boolean isConsistent_st1() {
    return getMeasure() == measure_st1;
  }
}
