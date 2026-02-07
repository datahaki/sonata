// code by jph
package ch.alpine.sonata.enc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import ch.alpine.sonata.enc.api.DiatonicPrecision;
import ch.alpine.sonata.enc.api.ExportScoreFormat;
import ch.alpine.sonata.enc.api.ImportScoreFormat;
import ch.alpine.sonata.enc.api.KeyModeDatabaseRequest;
import ch.alpine.sonata.enc.api.TempoDatabaseRequest;
import ch.alpine.sonata.enc.krn.KernFormat;
import ch.alpine.sonata.enc.ly.LilypondFormat;
import ch.alpine.sonata.enc.ly.LilypondPdfFormat;
import ch.alpine.sonata.enc.ly.LilypondPngFormat;
import ch.alpine.sonata.enc.md.MusedataReader;
import ch.alpine.sonata.enc.mid.MidiFormat;
import ch.alpine.sonata.enc.mxl.MusicMxlFormat;
import ch.alpine.sonata.enc.mxl.MusicXmlFormat;
import ch.alpine.sonata.enc.nvm.NativeFormat;
import ch.alpine.sonata.enc.st.Stage1Reader;
import ch.alpine.sonata.enc.st.Stage2Reader;
import ch.alpine.sonata.scr.Score;
import ch.alpine.tensor.ext.Integers;

// {Native=4453, MUSEDATA=123, STAGE1=1425, STAGE2=1423, MUSIC_MXL=148, MUSIC_XML=49, MIDI=96, KERN=472}
public enum Encoding {
  /** {@link NativeFormat} is the default score format of {@link PirateFugues}
   * the score database contains at least 4450 scores in native format */
  NATIVE(new NativeFormat(), "nvm"),
  /** the score database contains at least 120 scores in native format */
  MUSEDATA(new MusedataReader(), "md"),
  /** the score database contains at least 1425 scores in native format */
  STAGE1(new Stage1Reader(), "st1"),
  /** the score database contains at least 1423 scores in native format */
  STAGE2(new Stage2Reader(), "st2"),
  /** {@link MusicMxlFormat} is zip compressed output of {@link MusicXmlFormat}
   * the score database contains at least 148 scores in mxl format */
  MUSIC_MXL(new MusicMxlFormat(), "mxl"),
  /** {@link MusicXmlFormat} is suitable for score import and export with Finale and Sibelius
   * the score database contains at least 148 scores in xml format */
  MUSIC_XML(new MusicXmlFormat(), "xml"),
  /** {@link MidiFormat} assigns each voice to a separate channel
   * the score database contains at least 148 scores in midi format */
  MIDI(new MidiFormat(), "mid", "midi"),
  /** ScriptHumdrum
   * the score database contains at least 472 scores in kern format */
  KERN(new KernFormat(), "krn"),
  /** {@link LilypondFormat} is an open-source <i>Einbahnstrassen</i>-format for superb score rendering
   * this option only produces ly file without compilation */
  LILYPOND(new LilypondFormat(), "ly"),
  /** produces pdf file by compiation of ly file */
  LILYPOND_PDF(new LilypondPdfFormat(), "pdf"),
  /** produces png files by compiation of ly file */
  LILYPOND_PNG(new LilypondPngFormat(), "png");

  /** {@link HtmlFormat} for score view in Firefox or Chrome */
  // HTML(new HtmlFormat(), ""),
  // /** {@link MatlabFormat} is intended for bulk statistical analysis */
  // MATLAB(new MatlabFormat(), "m"),
  // /** {@link ZipFormat} bundles several other {@link AbstractFormat}s into a zip file */
  // ZIP(new ZipFormat(), "zip"),
  private final Object abstractFormat;
  private final ImportScoreFormat importScoreFormat;
  private final ExportScoreFormat exportScoreFormat;
  private final List<String> extensions;

  private Encoding(Object abstractFormat, String... extension) {
    this.abstractFormat = abstractFormat;
    this.importScoreFormat = abstractFormat instanceof ImportScoreFormat importScoreFormat ? importScoreFormat : null;
    this.exportScoreFormat = abstractFormat instanceof ExportScoreFormat exportScoreFormat ? exportScoreFormat : null;
    extensions = Stream.of(extension).toList();
    Integers.requireLessEquals(1, extensions.size());
  }

  /* package */ Score getScore(Path file) throws Exception {
    if (Files.exists(file))
      return importScoreFormat.get(file);
    throw new IllegalArgumentException("does not exist: " + file);
  }

  /* package */ Path putScore(Path directory, String title, Score score, Object exportOptions) {
    Path file = directory.resolve(title + '.' + extension());
    try {
      exportScoreFormat.put(file, score, exportOptions);
      return file;
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  public boolean isImportable() {
    return abstractFormat instanceof ImportScoreFormat;
  }

  public boolean isExportable() {
    return abstractFormat instanceof ExportScoreFormat;
  }

  public boolean hasDiatonicPrecision() {
    return abstractFormat instanceof DiatonicPrecision;
  }

  public boolean hasTempoDatabaseRequest() {
    return abstractFormat instanceof TempoDatabaseRequest;
  }

  public boolean hasKeyModeDatabaseRequest() {
    return abstractFormat instanceof KeyModeDatabaseRequest;
  }

  /** @return preferred extension for encoding */
  public String extension() {
    return extensions.get(0);
  }

  /** @return immutable list */
  public List<String> extensions() {
    return extensions;
  }
}
