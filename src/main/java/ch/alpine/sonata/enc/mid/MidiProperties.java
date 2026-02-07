// code by jph
package ch.alpine.sonata.enc.mid;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import ch.alpine.bridge.lang.EnumValue;
import ch.alpine.bridge.lang.SI;
import ch.alpine.sonata.Division;
import ch.alpine.sonata.KeyMode;
import ch.alpine.sonata.KeySignature;
import ch.alpine.sonata.scr.Score;
import sys.dat.Manager;

public class MidiProperties {
  public static final String key_title = "Score.Title";
  public static final String key_comment = "Score.Comment";
  public static final String key_keySignatureType = "Score.KeySignature.Type";
  public static final String key_keyMode = "Score.KeyMode";
  public static final String key_quarter = "Score.Quarter";
  public static final String key_division = "Score.Division";
  public static final String key_bpm = "Score.Bpm";
  // ---
  static final String[] keys = new String[] { //
      // ---
      key_title, //
      key_comment, //
      key_keySignatureType, //
      key_keyMode, //
      key_quarter, //
      key_division, //
      key_bpm, //
  };
  // ---
  final Path file;
  public final Manager manager; // don't call myManager.manifest();

  public MidiProperties(Path managerFile) {
    file = managerFile;
    manager = new Manager(managerFile);
  }

  public void equip(Score score) {
    score.title = manager.getString(key_title, score.title);
    score.comment = manager.getString(key_comment, score.comment);
    if (manager.containsKey(key_keySignatureType)) {
      if (score.keySignature.equals(KeySignature.fromType(manager.getInteger(key_keySignatureType, score.keySignature.type()))))
        log("keySignature redundant");
      score.keySignature = KeySignature.fromType(manager.getInteger(key_keySignatureType, score.keySignature.type()));
    }
    if (manager.containsKey(key_keyMode))
      score.setKeyMode(EnumValue.match(KeyMode.class, manager.getString(key_keyMode, score.keyMode().toString())));
    if (manager.containsKey(key_quarter)) {
      if (score.quarter == manager.getInteger(key_quarter, score.quarter))
        log("quarter redundant");
      score.quarter = manager.getInteger(key_quarter, score.quarter);
    }
    if (manager.containsKey(key_division)) {
      if (score.division.getList().equals(manager.getListInteger(key_division, score.division.getList())))
        log("division list redundant");
      // System.out.println(myScore.myDivision + " --> in ");
      score.division = new Division(manager.getListInteger(key_division, score.division.getList()));
      // System.out.println(myScore.myDivision + " <-- out");
    }
    if (manager.containsKey(key_bpm))
      score.bpm = SI.PER_MINUTE.quantity(manager.getFloat(key_bpm, SI.PER_MINUTE.floatValue(score.bpm)));
  }

  private void log(String string) {
    System.out.println(file + " " + string);
  }

  /** only call for midi file export
   * 
   * @param score
   * @throws Exception */
  public void storeForMidiFormat(Score score) throws Exception {
    // myManager is not cleared to preserve additional manual settings and remarks
    manager.setProperty(key_title, score.title);
    manager.setProperty(key_comment, score.comment);
    manager.setProperty(key_keyMode, score.keyMode().toString());
    manager.setProperty(key_division, score.division.toString());
    ordered_manifest();
  }

  public void ordered_manifest() throws Exception {
    try (Writer writer = Files.newBufferedWriter(file)) {
      for (String myString : keys)
        if (manager.containsKey(myString))
          writer.write(myString.trim() + "=" + manager.getString(myString, null).trim() + '\n');
    }
  }
}
