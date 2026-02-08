// code by jph
package ch.alpine.sonata.enc.api;

import java.nio.file.Path;

import ch.alpine.sonata.scr.Score;

public interface ExportScoreFormat {
  void put(Path file, Score score, Object object) throws Exception;
}
