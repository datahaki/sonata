// code by jph
package ch.alpine.sonata.enc.mxl;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import ch.alpine.sonata.scr.Score;

/** MusicXML is for score interchange between music notation softwares.
 * Import and Export tested with
 * Finale 2014,
 * Sibelius 7.5,
 * MuseScore 2.0.3 */
public class MusicXmlFormat extends MusicXmlAbstract {
  @Override
  public Score get(Path file) throws Exception {
    try (InputStream inputStream = Files.newInputStream(file)) {
      return fromStream(inputStream);
    }
  }

  @Override
  public void put(Path file, Score score, Object object) throws Exception {
    try (OutputStream outputStream = Files.newOutputStream(file)) {
      new MusicXmlWriter(outputStream, score);
    }
  }
}
