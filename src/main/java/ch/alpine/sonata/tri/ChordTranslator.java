// code by jph
package ch.alpine.sonata.tri;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public final class ChordTranslator {
  private final Map<String, String> map = new HashMap<>();

  public ChordTranslator(TriadFormat src, TriadFormat dst) {
    for (String string : src.labels())
      map.put(string, dst.format(src.getTriad(string)));
    if (map.size() != src.labels().size())
      throw new RuntimeException("chord translation not bijective");
  }

  public boolean canTranslate(String string) {
    return map.containsKey(string);
  }

  public String translate(String string) {
    if (canTranslate(string))
      return map.get(string);
    throw new RuntimeException("chord label unknown: " + string);
  }

  public List<String> translateAll(Collection<String> collection) {
    return collection.stream() //
        .map(this::translate) //
        .collect(Collectors.toList());
  }

  public boolean isIdentity() {
    boolean status = true;
    for (Entry<String, String> entry : map.entrySet())
      status &= entry.getKey().equals(entry.getValue());
    return status;
  }

  public void printOut() {
    for (Entry<String, String> entry : map.entrySet()) {
      String myL = entry.getKey();
      String myR = entry.getValue();
      System.out.println(myL + "\t-> " + myR + (myL.equals(myR) ? "\tid" : ""));
    }
  }
}
