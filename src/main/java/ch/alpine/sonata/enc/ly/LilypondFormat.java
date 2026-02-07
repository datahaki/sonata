// code by jph
package ch.alpine.sonata.enc.ly;

import java.io.BufferedReader;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import ch.alpine.bridge.io.UserName;
import ch.alpine.sonata.enc.api.DiatonicPrecision;
import ch.alpine.sonata.enc.api.ExportScoreFormat;
import ch.alpine.sonata.scr.Score;
import ch.alpine.tensor.ext.HomeDirectory;

public final class LilypondFormat implements ExportScoreFormat, DiatonicPrecision {
  // FIXME
  public static Path COMMAND = UserName.is("datahaki") //
      ? HomeDirectory.path("Public", "lilypond-2.24.3/bin", "lilypond")
      : Path.of("lilypond");

  /** generates single .ly file (without further processing to pdf etc.)
   * 
   * @param file
   * @param score
   * @param exportOptions
   * @return
   * @throws Exception */
  public static Path putFile(Path file, Score score, LilypondLayout lilypondLayout, LilypondParam lilypondParam) throws Exception {
    LilypondWriter lilypondWriter = new LilypondWriter(new LilypondHeader(lilypondLayout));
    {
      LilypondScore lilypondScore = new LilypondScore(score, lilypondLayout, lilypondParam);
      lilypondWriter.tocItem(score);
      lilypondWriter.append(lilypondScore);
    }
    lilypondWriter.toFile(file);
    return file;
  }

  @Override
  public void put(Path file, Score score, Object object) throws Exception {
    putFile(file, score, new LilypondLayout(), (LilypondParam) object);
  }

  static void compile(Path file, String... strings) {
    List<String> list = new LinkedList<>();
    list.add(LilypondFormat.COMMAND.toString());
    Stream.of(strings).forEach(list::add);
    list.add(file.getFileName().toString());
    ProcessBuilder processBuilder = new ProcessBuilder(list);
    processBuilder.directory(file.getParent().toFile());
    try {
      Process process = processBuilder.start();
      process.waitFor();
      try (BufferedReader bufferedReader = process.errorReader()) {
        bufferedReader.lines().forEach(System.out::println);
      }
      try (BufferedReader bufferedReader = process.inputReader()) {
        bufferedReader.lines().forEach(System.out::println);
      }
      // file.delete();
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }
}
