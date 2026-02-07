// code by jph
package ch.alpine.sonata.enc.utl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Objects;

import ch.alpine.sonata.Attributes;
import ch.alpine.sonata.Note;
import ch.alpine.sonata.Torrent;
import ch.alpine.sonata.Voice;
import ch.alpine.sonata.xml.XmlBeam;
import ch.alpine.sonata.xml.XmlNote;
import sys.col.HuePalette;
import sys.mat.IntRange;

/** XML decomposition requires rest notes & ties, i.e. different from MultiVoiceRow */
public class MeasureChop {
  final XmlDurationMap noteDurations;
  final XmlDurationMap restDurations;
  public final int measure;

  public MeasureChop(XmlDurationMap noteDurations, XmlDurationMap restDurations) {
    this.noteDurations = noteDurations;
    this.restDurations = restDurations;
    measure = noteDurations.division.measure;
    if (measure != restDurations.division.measure)
      throw new RuntimeException("maps are incompatible");
  }

  public List<List<XmlNote>> fromVoice(Voice voice, final int entry, int total) {
    int ticks = entry;
    List<XmlNote> list = new LinkedList<>();
    for (Entry<Integer, Torrent> myEntry : voice) {
      int delta = myEntry.getKey() - ticks;
      if (0 < delta) {
        list.addAll(restDurations.getRestList(ticks, delta));
        ticks += delta;
      }
      Torrent torrent = myEntry.getValue();
      Attributes attributes = torrent.attributes;
      Color color = attributes.isAppended() //
          ? HuePalette.LUMA.getColor(attributes.getIndex())
          : Color.GRAY; // darker()
      for (Note note : myEntry.getValue()) {
        list.addAll(noteDurations.getList(ticks, note, color));
        ticks += note.ticks();
      }
    }
    int delta = total - ticks;
    if (0 < delta) {
      list.addAll(restDurations.getRestList(ticks, delta));
      ticks += delta; // obsolete
    }
    return chop(list, entry);
  }

  public List<List<XmlNote>> fromIntercepts(NavigableSet<Integer> myNavigableSet, final int entry, int total) {
    List<XmlNote> myList = new ArrayList<>();
    int count = entry;
    for (int ticks : myNavigableSet) {
      int delta = ticks - count;
      if (0 < delta) {
        myList.addAll(restDurations.getRestList(count, delta));
        count += delta;
      }
    }
    int delta = total - count;
    if (0 < delta) {
      myList.addAll(restDurations.getRestList(count, delta));
      count += delta; // obsolete
    }
    return chop(myList, entry);
  }

  private List<List<XmlNote>> chop(List<XmlNote> myLongList, int entry) {
    List<List<XmlNote>> myCollection = new ArrayList<>();
    List<XmlNote> myList = new ArrayList<>();
    for (XmlNote myXmlNote : myLongList) {
      myList.add(myXmlNote);
      entry += myXmlNote.xmlSize.duration;
      if (entry % measure == 0) {
        myCollection.add(compileBeams(myList));
        myList = new ArrayList<>();
      }
    }
    if (!myList.isEmpty())
      myCollection.add(compileBeams(myList));
    return myCollection;
  }

  /** beam computation for import in Finale
   * 
   * @param myList
   * @return */
  private static List<XmlNote> compileBeams(List<XmlNote> myList) {
    for (int c0 : IntRange.positive(myList.size() - 1)) {
      XmlNote myOne = myList.get(c0);
      if (!myOne.isRest()) {
        XmlNote myTwo = myList.get(c0 + 1);
        if (!myTwo.isRest() && myOne.xmlSize.type.equals(myTwo.xmlSize.type) && myOne.xmlSize.duration == myTwo.xmlSize.duration) {
          myOne.xmlBeam = Objects.isNull(myOne.xmlBeam) ? XmlBeam._begin : XmlBeam._continue;
          myTwo.xmlBeam = XmlBeam._end;
        }
      }
    }
    return myList;
  }
}
