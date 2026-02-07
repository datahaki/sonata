// code by jph
package ch.alpine.sonata.enc;

import java.nio.file.Path;

import ch.alpine.sonata.enc.ly.LilypondParam;
import ch.alpine.sonata.scr.Score;
import ch.alpine.tensor.ext.FileBaseName;

public enum ScoreIO {
  ;
  public static Score read(Encoding encoding, Path file) {
    try {
      Score score = encoding.getScore(file);
      return score;
    } catch (Exception exception) {
      exception.printStackTrace();
    }
    throw new RuntimeException("fail to import " + file);
  }

  public static Score read(Path file) {
    return read(Encodings.INSTANCE.getEncoding(file).orElseThrow(), file);
  }

  // ---
  /** @param file extension determines {@link Encoding}
   * @param score
   * @param exportOptions */
  public static Path write(Path file, Score score, LilypondParam exportOptions) {
    return write(Encodings.INSTANCE.getEncoding(file).orElseThrow(), file.getParent(), FileBaseName.of(file), score, exportOptions);
  }

  public static Path write(Path file, Score score) {
    return write(Encodings.INSTANCE.getEncoding(file).orElseThrow(), file.getParent(), FileBaseName.of(file), score, null);
  }

  public static Path write(Encoding encoding, Path directory, String title, Score score, Object object) {
    return encoding.putScore(directory, title, score, object);
  }
}
