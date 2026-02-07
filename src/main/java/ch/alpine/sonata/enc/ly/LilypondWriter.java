// code by jph
package ch.alpine.sonata.enc.ly;

import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import ch.alpine.sonata.scr.Score;

/** LilypondWriter is typically optimized for the latest Lilypond version.
 * The ly-file is saved in UTF-8.
 * 
 * The "\relative" keyword is not used.
 * The "\relative" notation is advantageous for entering a score manually.
 * 
 * Line breaks are also permitted at manually inserted bar lines even within incomplete measures.
 * To allow a line break without printing a bar line, use the following: \bar ""
 * 
 * \pageBreak forces page break */
public class LilypondWriter {
  private final StringBuilder stringBuilder = new StringBuilder();

  public LilypondWriter(LilypondHeader lilypondHeader) {
    write(lilypondHeader.getString());
  }

  /** declare entry for table of contents in multi-score document
   * 
   * @param score
   * @throws Exception */
  public void tocItem(Score score) {
    tocItem(score.title + (score.comment.isEmpty() ? "" : " — " + score.comment));
  }

  public void tocItem(String inQuotes) {
    writeln("\\tocItem");
    writeln(LilypondConstants.asMarkup(false, "", inQuotes));
    writeln(LilypondConstants.asMarkup(true, "\\huge \\bold", inQuotes)); // \large
    writeln("\\markup \\vspace #1");
    writeln("\\noPageBreak");
  }

  public void append(LilypondScore lilypondScore) {
    write(lilypondScore.getString());
  }

  void paragraph(String string) {
    writeln("\\markuplist {");
    writeln(" \\justified-lines {");
    // some characters are not safe, use: \concat { a \char ##x0022 b }
    writeln(string);
    writeln(" }");
    writeln("}");
  }

  public void pageBreak() {
    writeln("\\pageBreak");
  }

  private void writeln(String string) {
    write(string);
    stringBuilder.append('\n'); // character
  }

  private void write(String string) {
    stringBuilder.append(string);
  }

  public String getString() {
    return stringBuilder.toString();
  }

  public void toFile(Path file) throws Exception {
    try (Writer myWriter = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
      myWriter.write(getString());
    }
  }
}
