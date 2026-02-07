// code by jph
package ch.alpine.sonata.hrm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;

import ch.alpine.sonata.tri.Triad;

public class HarmonicSequence implements Serializable {
  public final List<Triad> list = new ArrayList<>();
  /** only for faster searching */
  private final Map<Triad, NavigableSet<Integer>> map = new HashMap<>();

  public void add(Triad triad) {
    if (list.isEmpty() || !list.get(list.size() - 1).equals(triad)) {
      if (!map.containsKey(triad))
        map.put(triad, new TreeSet<>());
      map.get(triad).add(list.size());
      list.add(triad);
    }
  }

  public List<List<Triad>> fromTo(Triad myBeg, Triad myEnd, int maxLength) {
    List<List<Triad>> myReturn = new LinkedList<>();
    if (map.containsKey(myBeg))
      for (int head : map.get(myBeg)) {
        Integer tail = map.containsKey(myEnd) ? map.get(myEnd).higher(head) : null;
        if (Objects.nonNull(tail) && tail - head < maxLength)
          myReturn.add(list.subList(head, tail + 1));
      }
    return myReturn;
  }
}
