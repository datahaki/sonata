// code by jph
package ch.alpine.sonata.tri;

import java.util.Collection;

import ch.alpine.sonata.KeySignature;
import ch.alpine.sonata.Natur;

public class VerbatimFormat extends TriadFormat {
  public VerbatimFormat(KeySignature keySignature) {
    for (Triad triad : Triad.values())
      put(triad, getString(keySignature, triad.list));
  }

  public static String getString(KeySignature keySignature, Collection<Natur> collection) {
    return collection.stream() //
        .map(natur -> keySignature.dodecatonicScale().getDiatoneAlter(natur).toString()) //
        .toList() //
        .toString().replace(" ", "");
  }
}
