// code by jph
package ch.alpine.sonata.tri;

public enum TriadType {
  MAJOR(12),
  MINOR(12),
  DIMINISHED(12),
  MAJOR_SIXTH(12),
  DIMINISHED_SEVENTH(3),
  AUGMENTED(4);

  private final int total;

  private TriadType(int total) {
    this.total = total;
  }

  public int total() {
    return total;
  }
}
