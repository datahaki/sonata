// code by jph
package ch.alpine.sonata.tri;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import sys.mat.IntegerMath;

// TODO NOTATION this class is a mess should be record
public class TriadTransition implements Serializable {
  private static final Set<TriadTransition> set = new LinkedHashSet<>();
  private static final Map<TriadTransition, TriadTransition> map = new HashMap<>();
  static {
    for (Triad ante : Triad.values()) {
      for (Triad post : Triad.values()) {
        TriadTransition triadTransition = new TriadTransition(ante, post);
        boolean insert = true;
        for (TriadTransition representative : set)
          if (representative.isTransposed(triadTransition)) {
            map.put(triadTransition, representative);
            insert = false;
            break;
          }
        if (insert) {
          set.add(triadTransition);
          map.put(triadTransition, triadTransition);
        }
      }
    }
  }

  public static TriadTransition getRepresentative(TriadTransition triadTransition) {
    return map.get(triadTransition);
  }

  public final Triad prev;
  public final Triad next;

  public TriadTransition(Triad prev, Triad next) {
    this.prev = prev;
    this.next = next;
  }

  public int gcd() {
    return IntegerMath.gcd(prev.triadType.total(), next.triadType.total());
  }

  private boolean isTransposed_v2(TriadTransition triadTransition) {
    int gcd = gcd();
    int delta_beg = Math.floorMod(triadTransition.prev.natur.ordinal() - prev.natur.ordinal(), gcd); // beg.myTriadType.total);
    int delta_end = Math.floorMod(triadTransition.next.natur.ordinal() - next.natur.ordinal(), gcd); // end.myTriadType.total);
    return delta_beg == delta_end;
  }

  public boolean isTransposed(TriadTransition triadTransition) {
    boolean isTransposed = true;
    isTransposed &= prev.triadType.equals(triadTransition.prev.triadType);
    isTransposed &= next.triadType.equals(triadTransition.next.triadType);
    return isTransposed ? isTransposed_v2(triadTransition) : false;
    // if (isTransposed)
    // boolean v1 = isTransposed_v1(myTriadTransition);
    // boolean v2 = isTransposed_v2(myTriadTransition);
    // if (v1 != v2) {
    // TriadFormat myTriadFormat = new ChromaticFormat(KeySignature.C, new ChromaticAscii());
    // System.out.println(this.toString(myTriadFormat));
    // System.out.println(myTriadTransition.toString(myTriadFormat));
    // System.out.println("---" + v1 + " " + v2);
    // }
    // GlobalSwitch.assert_(v1 == v2);
    // return v1;
    // return isTransposed_v2(myTriadTransition);
    // return isTransposed;
  }

  public String toString(TriadFormat triadFormat) {
    return "(" + triadFormat.format(prev) + ", " + triadFormat.format(next) + ")";
  }

  @Override
  public String toString() {
    Set<TriadType> set = new LinkedHashSet<>();
    set.add(prev.triadType);
    set.add(next.triadType);
    int delta = Math.floorMod(next.natur.ordinal() - prev.natur.ordinal(), gcd());
    return set + " " + delta;
  }

  @Override
  public boolean equals(Object object) {
    TriadTransition triadTransition = (TriadTransition) object;
    return prev.equals(triadTransition.prev) && next.equals(triadTransition.next);
  }

  @Override
  public int hashCode() {
    return prev.hashCode() + next.hashCode();
  }
}
