// code by jph
package ch.alpine.sonata.jnt;

import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.alpine.sonata.Joint;
import ch.alpine.sonata.Relation;

public abstract class ScoreJoint {
  public final NavigableMap<Integer, Joint> navigableMap = new TreeMap<>();

  public final void apply(Function<Stream<Relation>, Joint> function) {
    for (Entry<Integer, Joint> entry : navigableMap.entrySet())
      navigableMap.put(entry.getKey(), //
          function.apply(entry.getValue().stream()));
  }

  /** removes Relation.nada in all Joints */
  public final void reduce() {
    apply(stream -> new Joint(stream.filter(Relation::nonNada)));
  }

  public final void removeEmpty() {
    Set<Integer> set = navigableMap.entrySet().stream() //
        .filter(entry -> entry.getValue().isEmpty()) //
        .map(entry -> entry.getKey()) //
        .collect(Collectors.toSet()); //
    set.forEach(navigableMap::remove);
  }
}
