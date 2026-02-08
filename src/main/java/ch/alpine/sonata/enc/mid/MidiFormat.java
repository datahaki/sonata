// code by jph
package ch.alpine.sonata.enc.mid;

import java.nio.file.Path;

import javax.sound.midi.Sequence;

import ch.alpine.midkit.Midi;
import ch.alpine.sonata.enc.api.ExportScoreFormat;
import ch.alpine.sonata.enc.api.ImportScoreFormat;
import ch.alpine.sonata.enc.ly.LilypondParam;
import ch.alpine.sonata.scr.Score;
import ch.alpine.sonata.seq.MidiListings;
import ch.alpine.sonata.seq.MidiSequence;
import ch.alpine.sonata.seq.VoicedSequence;
import ch.alpine.tensor.ext.PathName;

/** Support for MIDI format type 1.
 * 
 * MIDI files are not necessarily voice encoded. At any tick there can be multiple events and messages.
 * 
 * In order to successfully read a MIDI file, the class requires each voice to occupy a separate channel.
 * 
 * For instance NOTE_ON|0x02 has channel 2. The distribution of these events to the tracks has no effect on the score.
 * 
 * This limitation ensures that identical notes can be started and stopped across all voices, at any tick.
 * 
 * To properly convert a MIDI file to a {@link Score}, a '.properties' file is required with the following information: title, comment, keyMode, division
 * optional: keySignature, quarter, bpm
 * 
 * Books encoded in midi include Bach_Canons, Bach_Cellosuite, Bach_Goldberg, Bach_Flute, Bach_Lute, Bach_Violin
 * 
 * {@link MidiFormat} does not make use of {@link LilypondParam}. All voices and text are exported. */
public class MidiFormat implements ImportScoreFormat, ExportScoreFormat {
  /** @param file recommended with extension ".mid"
   * @param score
   * @param exportOptions
   * @throws Exception */
  public static void putFile(Path file, Score score) throws Exception {
    MidiSequence midiSequence = new VoicedSequence(score, score.voices);
    midiSequence.setText(true);
    Sequence mySequence = midiSequence.getSequence();
    Midi.write(mySequence, file);
  }

  @Override
  public void put(Path file, Score score, Object object) throws Exception {
    final PathName filename = PathName.of(file);
    Path output = filename.withExtension("mid");
    putFile(output, score);
    // ---
    new MidiProperties(filename.withExtension("properties")).storeForMidiFormat(score);
    // ---
    MidiListings.defaultExportHtml(output);
  }

  @Override
  public Score get(Path file) throws Exception {
    return new MidiScoreReader().get(file);
  }
}
