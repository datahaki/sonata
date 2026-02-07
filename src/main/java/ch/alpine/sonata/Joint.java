// code by jph
package ch.alpine.sonata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.alpine.sonata.utl.TpfStatics;
import sys.mat.IntRange;

/** Joint is immutable */
public final class Joint implements Comparable<Joint>, Iterable<Relation> {
  public static Joint empty() {
    return new Joint(Stream.empty());
  }

  // ---
  /** use getList() */
  private final List<Relation> list;
  private final int hash;

  /** preferred constructor
   * 
   * @param stream */
  public Joint(Stream<Relation> stream) {
    this(stream.toList());
  }

  /** policy requires that myList is not modified from outside
   * 
   * @param list */
  public Joint(List<Relation> list) {
    this.list = list;
    hash = list.hashCode();
  }

  public Joint transpose(int delta) {
    return new Joint(list.stream() //
        .map(relation -> relation.transpose(delta)));
  }

  public Joint backwards() {
    return new Joint(list.stream().map(Relation::backwards));
  }

  /** @return copy of joint but with all Relation.nada dropped */
  public Joint reducedCopy() {
    return new Joint(list.stream().filter(Relation::nonNada)); //
  }

  public Joint drop(int index) {
    return new Joint(Stream.concat( //
        list.subList(0, index).stream(), //
        list.subList(index + 1, list.size()).stream()));
  }

  public Joint stripStrict(Joint mySub) {
    List<Relation> copy = new ArrayList<>(list);
    boolean myBoolean = true;
    for (Relation relation : mySub.list)
      myBoolean &= copy.remove(relation);
    if (!myBoolean)
      throw new RuntimeException("could not remove all elements");
    return new Joint(copy);
  }

  public Stream<Integer> pitchAnteUnsorted() {
    return list.stream() //
        .map(Relation::pitchAnte) //
        .filter(Objects::nonNull);
  }

  /** hold, kill, jump contribute their Integer0 values to the ringing list (for nada, live these values are null and will not be appended to the list)
   * typically ringing is computed for joints that have a mod12 projection, in order to search for order1div supersets
   * 
   * @return sorted list of non-null pitches */
  public List<Integer> pitchAnteSorted() {
    return pitchAnteUnsorted().sorted().collect(Collectors.toList());
  }

  public Stream<Integer> pitchPostUnsorted() {
    return list.stream() //
        .map(Relation::pitchPost) //
        .filter(Objects::nonNull);
  }

  /** function introduced only for ranking */
  public List<Integer> pitchPostUnsortedList() {
    return pitchPostUnsorted().collect(Collectors.toList());
  }

  public List<Integer> pitchPostSorted() {
    return pitchPostUnsorted().sorted().collect(Collectors.toList());
  }

  public List<Integer> pitchAfterSorted12() {
    return list.stream() //
        .map(relation -> relation.pitchPost()) //
        .filter(Objects::nonNull) //
        .map(TpfStatics::mod12) //
        .sorted() //
        .collect(Collectors.toList());
  }

  public Set<Integer> getInteger0Set(Link myLink) {
    return list.stream() //
        .filter(relation -> relation.link.equals(myLink)) //
        .map(relation -> relation.integer0) //
        .collect(Collectors.toSet());
  }

  public int getLinkCount(Link link) {
    return (int) list.stream() //
        .filter(relation -> relation.link.equals(link)) //
        .count();
  }

  public boolean isEmpty() {
    return list.isEmpty();
  }

  public int size() {
    return list.size();
  }

  public Relation get(int index) {
    return list.get(index);
  }

  /** @return unmodifiable */
  public List<Relation> getList() {
    return list;
  }

  /** this comparator makes most sense if both joints contain the same amount of relations */
  @Override
  public int compareTo(Joint myJoint) {
    int min = Math.min(list.size(), myJoint.list.size());
    for (int index : IntRange.positive(min)) {
      int cmp = list.get(index).compareTo(myJoint.list.get(index));
      if (cmp != 0)
        return cmp;
    }
    return list.size() - myJoint.list.size();
  }

  @Override
  public Iterator<Relation> iterator() {
    return list.iterator();
  }

  public Stream<Relation> stream() {
    return list.stream();
  }

  @Override
  public boolean equals(Object object) {
    return list.equals(((Joint) object).list);
  }

  @Override
  public int hashCode() {
    return hash;
  }
}
