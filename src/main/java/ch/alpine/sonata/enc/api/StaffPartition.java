// code by jph
package ch.alpine.sonata.enc.api;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ch.alpine.sonata.Voice;
import ch.alpine.sonata.scr.Score;
import ch.alpine.tensor.io.Primitives;

public enum StaffPartition {
  ;
  public static List<List<Voice>> getStaffList(Score score) { // ly
    List<Integer> staff_size = Primitives.toListInteger(score.getStaffPartition());
    List<List<Voice>> staffList = new LinkedList<>();
    int index = -1;
    for (int count : staff_size) {
      List<Voice> list = new ArrayList<>();
      for (int i = 0; i < count; ++i)
        list.add(score.voices.get(++index));
      staffList.add(list);
    }
    return staffList;
  }
  // ---
  // public static String getVoicePartition(Score score) {
  // String myString = "";
  // for (int voice : IntRange.positive(score.voices()))
  // myString += voice + StaffPartition.separator;
  // if (myString.endsWith(StaffPartition.separator))
  // myString = myString.substring(0, myString.length() - StaffPartition.separator.length());
  // return myString;
  // }
  //
  // public static String getClefPartition(Score score) {
  // String myString = "";
  // Clef prev_clef = Clef.TREBLE;
  // String myCollection = "";
  // int index = 0;
  // for (Voice voice : score.voices) {
  // if (prev_clef != voice.clef) {
  // if (!myCollection.isEmpty()) {
  // myString += myCollection.trim() + StaffPartition.separator;
  // myCollection = "";
  // }
  // prev_clef = voice.clef;
  // }
  // myCollection += " " + index;
  // ++index;
  // }
  // myString += myCollection.trim();
  // return myString.trim();
  // }
  //
  // public static String getChannelPartition(Score score) {
  // String myString = "";
  // MidiInstrument prev_channel = null;
  // String myCollection = "";
  // int index = 0;
  // for (Voice voice : score.voices) {
  // if (!voice.midiInstrument.equals(prev_channel)) {
  // if (!myCollection.isEmpty()) {
  // myString += myCollection.trim() + StaffPartition.separator;
  // myCollection = "";
  // }
  // prev_channel = voice.midiInstrument;
  // }
  // myCollection += " " + index;
  // ++index;
  // }
  // myString += myCollection.trim();
  // return myString.trim();
  // }
  //
  // public static String getStaffPartition(Score score) {
  // String myString = "";
  // MidiInstrument prev_channel = null;
  // Clef prev_clef = null;
  // String myCollection = "";
  // int index = 0;
  // for (Voice voice : score.voices) {
  // if (!voice.midiInstrument.equals(prev_channel) || !voice.clef.equals(prev_clef)) {
  // if (!myCollection.isEmpty()) {
  // myString += myCollection.trim() + StaffPartition.separator;
  // myCollection = "";
  // }
  // prev_channel = voice.midiInstrument;
  // prev_clef = voice.clef;
  // }
  // myCollection += " " + index;
  // ++index;
  // }
  // myString += myCollection.trim();
  // return myString.trim();
  // }
  // static final String separator = " | ";
  //
  // public static List<List<Integer>> getPartitionFrom(String partition) {
  // List<List<Integer>> list = new LinkedList<>();
  // for (String split : partition.split("\\|"))
  // list.add(TokenStream.of(split).map(Integer::parseInt).toList());
  // return list;
  // }
}
