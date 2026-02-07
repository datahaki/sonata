// code by jph
package ch.alpine.sonata.tri;

import java.util.Map.Entry;

import ch.alpine.sonata.DiatoneAlter;
import ch.alpine.sonata.KeySignature;
import ch.alpine.sonata.Natur;
import ch.alpine.sonata.Scale;

public final class ChromaticFormat extends TriadFormat {
  public ChromaticFormat(KeySignature keySignature, ChromaticTriads chromaticTriads) {
    Scale scale = keySignature.dodecatonicScale();
    for (Entry<Natur, DiatoneAlter> entry : scale.map().entrySet()) {
      Natur natur = entry.getKey();
      DiatoneAlter diatoneAlter = entry.getValue();
      // int pitch = natur.ordinal();
      put(Triad.major(natur), chromaticTriads.major(diatoneAlter));
      put(Triad.minor(natur), chromaticTriads.minor(diatoneAlter));
      put(Triad.diminished(natur), chromaticTriads.diminished(diatoneAlter));
      put(Triad.majorSixth(natur), chromaticTriads.majorSixth(diatoneAlter));
    }
    for (int type : new int[] { -1, 0, 1 }) {
      Natur pitch = Natur.fromPitch(keySignature.tonic() + type * 7);
      DiatoneAlter diatoneAlter = scale.getDiatoneAlter(pitch);
      put(Triad.diminishedSeventh(pitch), chromaticTriads.diminishedSeventh(diatoneAlter));
    }
    for (int type : new int[] { -1, 0, 1, 2 }) {
      Natur pitch = Natur.fromPitch(keySignature.tonic() + type * 7);
      DiatoneAlter diatoneAlter = scale.getDiatoneAlter(pitch);
      put(Triad.augmented(pitch), chromaticTriads.augmented(diatoneAlter));
    }
  }
}
