// code by jph
package ch.alpine.sonata.enc.mxl;

import java.io.InputStream;

import ch.alpine.sonata.enc.api.DiatonicPrecision;
import ch.alpine.sonata.enc.api.ExportScoreFormat;
import ch.alpine.sonata.enc.api.ImportScoreFormat;
import ch.alpine.sonata.scr.Score;

/** provides common functionality for MusicXML based encoding */
public abstract class MusicXmlAbstract implements ImportScoreFormat, ExportScoreFormat, //
    DiatonicPrecision {
  protected static Score fromStream(InputStream inputStream) throws Exception {
    MusicXmlReader musicXmlReader = new MusicXmlReader();
    musicXmlReader.read(inputStream);
    return musicXmlReader.getScore();
  }
}
