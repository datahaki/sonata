// code by jph
package ch.alpine.sonata;

enum DynamicDemo {
  ;
  static void main() {
    for (Dynamic d : Dynamic.values())
      System.out.println(d + "\t" + d.velocity);
  }
}
