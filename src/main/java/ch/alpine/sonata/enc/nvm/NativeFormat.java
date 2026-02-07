// code by jph
package ch.alpine.sonata.enc.nvm;

import java.nio.file.Path;

import ch.alpine.sonata.enc.api.DiatonicPrecision;
import ch.alpine.sonata.enc.api.ExportScoreFormat;
import ch.alpine.sonata.enc.api.ImportScoreFormat;
import ch.alpine.sonata.scr.Score;

/** ThePirateFugues native encoding */
public class NativeFormat implements ImportScoreFormat, ExportScoreFormat, //
    DiatonicPrecision {
  @Override
  public void put(Path file, Score score, Object object) throws Exception {
    new NativeWriter().put(file, score); // ExportOptions have no power here
  }

  @Override
  public Score get(Path file) {
    return new NativeReader().get(file);
  }
}
