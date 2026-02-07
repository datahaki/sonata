// code by jph
package ch.alpine.sonata;

import java.io.Serializable;

/** comparable for alignment of Inlay */
public class Attributes implements Comparable<Attributes>, Serializable {
  private final int index;
  /** 7-bits: 0 ... 127 */
  public int velocity = Dynamic.MF.velocity; // FIXME TPF FUUUU!!!! this used to be even 0

  public Attributes(int index) {
    this.index = index;
  }

  public Attributes() {
    this(-1);
  }

  public boolean isAppended() {
    return 0 <= index;
  }

  /** @return index of torrent with attributes in repertoire */
  public int getIndex() {
    return index;
  }

  @Override
  public int compareTo(Attributes attributes) {
    return Integer.compare(index, attributes.index);
  }
}
