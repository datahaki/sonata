// code by jph
package ch.alpine.sonata.enc.api;

import java.nio.file.Path;

import ch.alpine.sonata.scr.Score;

public interface ImportScoreFormat {
  Score get(Path file) throws Exception;
}
