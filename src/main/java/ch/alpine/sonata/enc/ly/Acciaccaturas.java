// code by jph
package ch.alpine.sonata.enc.ly;

import ch.alpine.sonata.scr.Score;

/** for export to Lilypond */
enum Acciaccaturas {
  ;
  public static Score replaceAll(Score score) {
    return private_replaceAll(score.cloneScore());
  }

  private static Score private_replaceAll(Score score) {
    // Timescale.stretch(score, 2);
    // // ---
    // HeptaCoverage heptaCoverage = new HeptaCoverage(score);
    // // ---
    // for (Voice voice : score.voices) {
    // NavigableMap<Integer, Note> navigableMap = voice.getNoteMapAbsolute();
    // for (Entry<Integer, Note> entry : navigableMap.entrySet()) {
    // int ticks = entry.getKey();
    // Note note = entry.getValue();
    // if (voice.shake.containsKey(ticks)) {
    // Ornament ornament = voice.shake.get(ticks);
    // if (ornament.isAcciaccatura()) {
    // HeptatonicScale heptatonicScale = score.keySignature.diatonicScale();
    // Hepta hepta = heptaCoverage.estimateDiatones(ticks);
    // if (Objects.nonNull(hepta))
    // heptatonicScale = new HeptatonicScale(hepta);
    // Melody melody = ornament.convert(note, 1, heptatonicScale);
    // voice.spliceInsert(ticks, melody);
    // voice.shake.remove(ticks);
    // }
    // }
    // }
    // }
    return score;
  }
}
