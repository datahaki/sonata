// code by jph
package ch.alpine.sonata.seq;

import java.nio.file.Path;

import ch.alpine.midkit.MidiListing;
import sys.Filename;

public enum MidiListings {
  ;
  /** @param file
   * @throws Exception when file is not valid MIDI format */
  public static Path defaultExport(Path file) throws Exception {
    Path myListFile = new Filename(file).withExtension("listing.txt");
    new MidiListing(file).exportTo(myListFile);
    return myListFile;
  }

  /** @param file
   * @throws Exception when file is not valid MIDI format */
  public static Path defaultExportHtml(Path file) throws Exception {
    Path myListFile = new Filename(file).withExtension("listing.html");
    new MidiListing(file).exportToHtml(myListFile);
    return myListFile;
  }
}
