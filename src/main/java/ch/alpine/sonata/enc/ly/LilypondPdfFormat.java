// code by jph
package ch.alpine.sonata.enc.ly;

import java.nio.file.Path;
import java.util.Objects;

import ch.alpine.sonata.enc.api.DiatonicPrecision;
import ch.alpine.sonata.enc.api.ExportScoreFormat;
import ch.alpine.sonata.scr.Score;
import sys.Filename;

public final class LilypondPdfFormat implements ExportScoreFormat, DiatonicPrecision {
  public static final LilypondLayout LAYOUT = LilypondLayout.pdf_default();

  @Override
  public void put(Path file, Score score, Object object) throws Exception {
    LilypondParam lilypondParam = Objects.isNull(object) //
        ? new LilypondParam()
        : (LilypondParam) object;
    Path lyFile = new Filename(file).withExtension("ly");
    LilypondFormat.putFile(lyFile, score, LAYOUT, lilypondParam);
    LilypondFormat.compile(lyFile, "--pdf", "-dno-point-and-click");
  }
}
