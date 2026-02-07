// code by jph
package ch.alpine.sonata.tri;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ch.alpine.tensor.ext.Integers;

/** unique labels for all chords in Theory */
public abstract class TriadFormat implements Serializable {
  private final Map<Triad, String> labels = new HashMap<>();
  private final Map<String, Triad> triads = new HashMap<>();

  /** @param triad
   * @return string expression of triad */
  public final String format(Triad triad) {
    // if (!myLabels.containsKey(myTriad)) {
    // System.out.println(myTriad + " fail");
    // printTable();
    // }
    if (!labels.containsKey(triad))
      throw new RuntimeException();
    return labels.get(triad);
  }

  public final boolean hasTriad(String string) {
    return triads.containsKey(string);
  }

  public final Triad getTriad(String string) {
    if (!hasTriad(string))
      throw new RuntimeException(string);
    return triads.get(string);
  }

  public final boolean hasAllTriads(Collection<String> collection) {
    return !collection.stream().filter(string -> !hasTriad(string)).findAny().isPresent();
  }

  public final Collection<String> labels() {
    return Collections.unmodifiableSet(triads.keySet());
  }

  protected final void put(Triad triad, String string) {
    labels.put(triad, string);
    triads.put(string, triad);
    Integers.requireEquals(labels.size(), triads.size());
  }

  protected void check() {
    Integers.requireEquals(labels.size(), 55);
  }

  public void printTable() {
    for (Entry<Triad, String> entry : labels.entrySet())
      System.out.println(entry.getKey() + "\t" + entry.getValue());
  }
}
