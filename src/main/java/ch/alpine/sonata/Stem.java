// code by jph
package ch.alpine.sonata;

public enum Stem {
  UP(+1),
  DN(-1);
  // "no stem" is not an option (if desired have to revert to other filter)

  private final int signature;
  // private final ImageIcon imageIcon;

  private Stem(final int signature) {
    this.signature = signature;
    // imageIcon = ;
  }

  public int signature() {
    return signature;
  }

  public Stem toggle() {
    return equals(UP) ? DN : UP;
  }
  // public ImageIcon imageIcon() {
  // return imageIcon;
  // }
}
