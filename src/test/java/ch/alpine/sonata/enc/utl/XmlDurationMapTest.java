package ch.alpine.sonata.enc.utl;

import java.util.Arrays;
import java.util.Map.Entry;

import org.junit.jupiter.api.Test;

import ch.alpine.sonata.Division;
import ch.alpine.sonata.xml.XmlSize;

class XmlDurationMapTest {
  @Test
  void testBwv1017_3() {
    Division division = new Division(36, 12, 6, 3, 1);
    int quarter = 12;
    XmlDurationMap xmlDurationMap = new XmlDurationMap(division, quarter, 1);
    xmlDurationMap.printout();
  }

  @Test
  void testSimple() {
    XmlDurationMap xmlDurationMap = new XmlDurationMap(new Division(Arrays.asList(12, 6, 3, 1)), 3, 1);
    xmlDurationMap.printout();
    // ---
    for (Entry<Integer, XmlSize> entry : xmlDurationMap.getPartials().entrySet())
      System.out.println(entry.getKey() + " " + entry.getValue().toLyString());
  }
}
