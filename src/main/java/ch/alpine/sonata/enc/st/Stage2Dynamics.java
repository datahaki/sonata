// code by jph
package ch.alpine.sonata.enc.st;

import java.util.StringTokenizer;

import ch.alpine.sonata.Dynamic;

/** valid for {@link Stage2Reader} */
/* package */ enum Stage2Dynamics {
  ;
  public static Dynamic getFromLine(String string) {
    final int length = string.length();
    if (31 < length && !string.substring(31, Math.min(33, length)).trim().isEmpty())
      return getDynamics(new StringTokenizer(string.substring(31)).nextToken());
    return null;
  }

  private static Dynamic getDynamics(String token) {
    int[] myInt = new int[128];
    for (char myChar : token.toCharArray())
      ++myInt[myChar];
    if (myInt['m'] == 0) {
      switch (myInt['p']) {
      case 1:
        return Dynamic.P;
      case 2:
        return Dynamic.PP;
      case 3:
        return Dynamic.PPP;
      }
      switch (myInt['f']) {
      case 1:
        return Dynamic.F;
      case 2:
        return Dynamic.FF;
      case 3:
        return Dynamic.FFF;
      }
    } else {
      if (myInt['p'] == 1)
        return Dynamic.MP;
      if (myInt['f'] == 1)
        return Dynamic.MF;
    }
    return null;
  }
}
