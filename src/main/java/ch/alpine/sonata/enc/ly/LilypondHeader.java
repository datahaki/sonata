// code by jph
package ch.alpine.sonata.enc.ly;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class LilypondHeader {
  public static final String VERSION = "2.24.1";
  private final LilypondLayout lilypondLayout;
  public String title;
  public String composer;
  // ---
  private StringBuilder stringBuilder;

  public LilypondHeader(LilypondLayout lilypondLayout) {
    this.lilypondLayout = lilypondLayout;
  }

  String getString() {
    stringBuilder = new StringBuilder();
    writeHeader();
    writePaper();
    writeToc();
    return stringBuilder.toString();
  }

  private void writeHeader() {
    writeln("\\version " + LilypondConstants.inQuotes(VERSION));
    // event-listener does not seem to help to determine how much is on one page
    // writeln("\\include \"event-listener.ly\"");
    if (!lilypondLayout.global_staff_size.isBlank())
      writeln("#(set-global-staff-size " + lilypondLayout.global_staff_size + ")");
    if (!lilypondLayout.default_paper_size.isBlank())
      // writeln("#(set! paper-alist (cons '(\"HD\" . (cons (* 192 mm) (* 108 mm))) paper-alist))");
      writeln("#(set-default-paper-size " + lilypondLayout.default_paper_size + ")");
    writeln("\\header {");
    if (lilypondLayout.copyright)
      writeln("  copyright = \\markup{\n" + //
          "\"The Pirate Fugues\"\n" + //
          "\\char ##x2013\n" + //
          "\"code by jph, artwork and sound by mm\"\n" + //
          "\\char ##x2013\n" + //
          "\"2015\"\n" + //
          "}\n");
    writeln("  tagline = " + LilypondConstants.of(lilypondLayout.tagline));
    // ---
    writeln("  encodingsoftware = ThePirateFugues");
    writeln("  encodingdate = " + LilypondConstants.inQuotes(new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
    // dedication = "xyz"
    if (Objects.nonNull(title) && !title.isEmpty())
      writeln("  title = " + LilypondConstants.inQuotes(title));
    // subtitle = "xyz"
    // subsubtitle = "xyz"
    if (Objects.nonNull(composer) && !composer.isEmpty())
      writeln("  composer = " + LilypondConstants.inQuotes(composer));
    // piece = see p.454
    // opus =
    writeln("}");
  }

  private void writePaper() {
    List<String> list = lilypondLayout.paper();
    writeln("\\paper { ");
    for (String myString : list)
      writeln("  " + myString);
    writeln("}");
  }

  /** insert table of contents for multi-score document
   * 
   * @throws Exception */
  public void writeToc() {
    if (lilypondLayout.toc) {
      writeln("\\markup \\vspace #2");
      writeln("\\markuplist \\table-of-contents");
      writeln("\\markup \\vspace #2");
    }
  }

  private void writeln(String string) {
    stringBuilder.append(string);
    stringBuilder.append('\n');
  }
}
