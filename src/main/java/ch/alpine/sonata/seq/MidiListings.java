// code by jph
package ch.alpine.sonata.seq;

import java.nio.file.Path;

import ch.alpine.midkit.MidiListing;
import ch.alpine.tensor.ext.PathName;

public enum MidiListings {
  ;
  /** @param file
   * @throws Exception when file is not valid MIDI format */
  public static Path defaultExport(Path file) throws Exception {
    Path myListFile = PathName.of(file).withExtension("listing.txt");
    MidiListing.of(file).exportTo(myListFile);
    return myListFile;
  }

  /** @param file
   * @throws Exception when file is not valid MIDI format */
  public static Path defaultExportHtml(Path file) throws Exception {
    Path myListFile = PathName.of(file).withExtension("listing.html");
    MidiListing.of(file).exportToHtml(myListFile);
    return myListFile;
  }
}
