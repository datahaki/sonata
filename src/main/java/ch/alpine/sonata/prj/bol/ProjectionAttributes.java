// code by jph
package ch.alpine.sonata.prj.bol;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import ch.alpine.tensor.ext.Serialization;
import sys.dat.Manager;
import sys.mat.IntRange;

/** policy/contract: none of the {@link Projection} alter values */
public class ProjectionAttributes implements Serializable {
  public int transpose = 0; // TODO TPF duplicate at the moment
  // ---
  /** all false by default */
  public BitSet bitSet = new BitSet();
  // ---
  public int crossing_threshold = 0;
  public int hovering_threshold = 4;
  public int handling_threshold = 12;
  // ---
  public int tracking_maxlength = 5;

  public ProjectionAttributes getCopy() throws ClassNotFoundException, IOException {
    return Serialization.copy(this);
  }

  public void loadFrom(Manager manager) {
    final String key = getClass().getSimpleName() + ".";
    try {
      bitSet.clear();
      for (int index : manager.getListInteger(key + "bitSet", new ArrayList<>()))
        bitSet.set(index);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
    manager.getInteger(key + "crossing_threshold", crossing_threshold);
    manager.getInteger(key + "hovering_threshold", hovering_threshold);
    manager.getInteger(key + "handling_threshold", handling_threshold);
    manager.getInteger(key + "tracking_maxlength", tracking_maxlength);
  }

  public void manifest(Manager manager) {
    final String key = getClass().getSimpleName() + ".";
    manager.setProperty(key + "bitSet", bitSet.stream().boxed().toList().toString());
    manager.setProperty(key + "crossing_threshold", crossing_threshold);
    manager.setProperty(key + "hovering_threshold", hovering_threshold);
    manager.setProperty(key + "handling_threshold", handling_threshold);
    manager.setProperty(key + "tracking_maxlength", tracking_maxlength);
  }

  public List<IntRange> getVoicePartition(int voices) {
    return getVoicePartition(bitSet, voices);
  }

  public IntRange getVoiceRange(int voice, int voices) {
    int min = bitSet.previousSetBit(voice); // on or before
    if (min < 0)
      min = 0;
    int max = bitSet.nextSetBit(voice + 1); // on or after
    if (max < 0)
      max = voices;
    IntRange intRange = new IntRange(min, max);
    if (!intRange.isInside(voice))
      throw new RuntimeException();
    return intRange;
  }

  private static List<IntRange> getVoicePartition(BitSet bitSet, int voices) {
    List<Integer> list = bitSet.stream().boxed().collect(Collectors.toList());
    list.add(voices);
    // ---
    int seed = 0;
    List<IntRange> partition = new LinkedList<>();
    for (int next : list) {
      partition.add(new IntRange(seed, next));
      seed = next;
    }
    return partition;
  }
}
