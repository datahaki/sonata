// code by jph
package ch.alpine.sonata.enc.mxl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import ch.alpine.sonata.scr.Score;
import ch.alpine.tensor.ext.PathName;

/** information about content is contained in
 * META-INF/container.xml (if present)
 * however, the implementation finds xml files in root directory */
public class MusicMxlFormat extends MusicXmlAbstract {
  @Override
  public Score get(Path file) throws Exception {
    Score score = null;
    try (ZipFile zipFile = new ZipFile(file.toFile())) {
      Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
      int count = 0;
      while (enumeration.hasMoreElements()) {
        ZipEntry zipEntry = enumeration.nextElement();
        String string = zipEntry.getName();
        if (!zipEntry.isDirectory()) {
          int myInt = string.indexOf('/');
          if (myInt < 0 && string.toLowerCase().endsWith(".xml")) {
            score = fromStream(zipFile.getInputStream(zipEntry));
            ++count;
          }
        }
      }
      if (count != 1)
        throw new RuntimeException("contents=" + count);
    }
    return score;
  }

  @Override
  public void put(Path file, Score score, Object object) throws Exception {
    putScore(file, score);
  }

  /** @param file should have extension mxl
   * @param score
   * @param exportOptions
   * @throws Exception */
  static void putScore(Path file, Score score) throws Exception {
    PathName filename = PathName.of(file);
    try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(file))) {
      final String myString = filename.title() + ".xml";
      // ---
      zipOutputStream.putNextEntry(new ZipEntry("META-INF/container.xml"));
      zipOutputStream.write(container(myString).getBytes());
      zipOutputStream.closeEntry();
      // ---
      zipOutputStream.putNextEntry(new ZipEntry(myString));
      new MusicXmlWriter(zipOutputStream, score);
      zipOutputStream.closeEntry();
    }
  }

  private static String container(String myString) {
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" //
        + "<container>\n" //
        + " <rootfiles>\n" //
        + "  <rootfile full-path=\"" + myString + "\" media-type=\"application/vnd.recordare.musicxml+xml\"/>\n" //
        + " </rootfiles>\n" //
        + "</container>\n";
  }
}
