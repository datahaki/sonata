// code by jph
package ch.alpine.sonata;

import java.util.HashMap;
import java.util.Map;

import sys.dat.MapOperations;

public class ClipboardMap<Type> {
  private final Map<Integer, Type> storage = new HashMap<>();

  public void setContent(int beg, Map<Integer, Type> map) {
    storage.clear();
    MapOperations.translate(map, -beg, storage);
  }

  public void getContent(int beg, Map<Integer, Type> map) {
    MapOperations.translate(storage, beg, map);
  }
}
