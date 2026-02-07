// code by jph
package ch.alpine.sonata.xml;

public enum XmlBeam {
  _begin, //
  _continue, //
  _end; //
  // ---

  @Override
  public String toString() {
    return name().substring(1);
  }
}
