// code by jph
package ch.alpine.sonata.tri;

import java.util.EnumMap;
import java.util.Map;

import ch.alpine.sonata.DiatoneAlter;
import ch.alpine.sonata.Natur;
import ch.alpine.sonata.Scale;

/** helps to transpose triad notes while remaining within the triad */
public class TriadScale extends Scale {
  public static TriadScale createFrom(Triad triad, Scale scale) {
    Map<Natur, DiatoneAlter> map = new EnumMap<>(Natur.class);
    for (Natur natur : triad.set())
      map.put(natur, scale.getDiatoneAlter(natur));
    return new TriadScale(map);
  }

  private TriadScale(Map<Natur, DiatoneAlter> map) {
    super(map);
  }
}
