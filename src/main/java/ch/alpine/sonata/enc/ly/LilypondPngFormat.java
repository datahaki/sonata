// code by jph
package ch.alpine.sonata.enc.ly;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import ch.alpine.sonata.enc.api.DiatonicPrecision;
import ch.alpine.sonata.enc.api.ExportScoreFormat;
import ch.alpine.sonata.scr.Score;
import sys.Filename;

public final class LilypondPngFormat implements ExportScoreFormat, DiatonicPrecision {
  public static final LilypondLayout LAYOUT = LilypondLayout.png_default();

  @Override
  public void put(Path file, Score score, Object object) throws Exception {
    LilypondParam lilypondParam = Objects.isNull(object) //
        ? new LilypondParam()
        : (LilypondParam) object;
    Filename filename = new Filename(file);
    Path lyFile = filename.withExtension("ly");
    LilypondFormat.putFile(lyFile, score, LAYOUT, lilypondParam);
    cleanOldPngFiles(filename);
    LilypondFormat.compile(lyFile, "--png", "-danti-alias-factor=" + LAYOUT.anti_alias_factor);
    { // single page becomes numbered page
      Path pngFile = filename.withExtension("png");
      if (Files.isRegularFile(pngFile))
        Files.move(pngFile, filename.file().getParent().resolve(filename.title() + "-page1.png"));
    }
  }

  private static void cleanOldPngFiles(Filename filename) throws Exception {
    Optional.of(filename.withExtension("png")) //
        .filter(Files::isRegularFile) //
        .ifPresent(t -> {
          try {
            Files.delete(t);
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
    {
      int count = 0;
      while (true) {
        Path pngFile = filename.file().getParent().resolve(filename.title() + "-page" + ++count + ".png");
        if (Files.isRegularFile(pngFile))
          Files.delete(pngFile);
        else
          break;
      }
    }
  }
}
