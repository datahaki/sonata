// code by jph
package ch.alpine.sonata.enc.utl;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

import ch.alpine.sonata.Division;
import ch.alpine.sonata.Note;
import ch.alpine.sonata.Tone;
import ch.alpine.sonata.xml.XmlNote;
import ch.alpine.sonata.xml.XmlSize;
import ch.alpine.sonata.xml.XmlType;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.Scalar;
import sys.mat.IntRange;

public class XmlDurationMap {
  public final Division division;
  private final int[] grad;
  private final Map<Integer, NavigableMap<Integer, XmlSize>> map = new HashMap<>();

  public XmlDurationMap(Division division, final int quarter, final int maxdots) {
    this.division = division;
    int measure = division.measure;
    grad = new int[measure];
    for (int ticks : IntRange.positive(measure)) {
      grad[ticks] = division.depthAt(ticks);
      map.put(ticks, new TreeMap<>());
    }
    for (XmlType xmlType : XmlType.values()) {
      for (int dots : IntRange.positive(maxdots + 1)) {
        Scalar multiplier = XmlSize.MULTIPLIER.get(dots);
        if (xmlType.isAvailable(quarter, multiplier)) {
          int duration = xmlType.duration(quarter, multiplier);
          for (int ticks : IntRange.positive(measure))
            if (ticks + duration <= measure && //
                canInsert(ticks, duration, 1)) {
              XmlSize xmlSize = new XmlSize();
              xmlSize.duration = duration;
              xmlSize.type = xmlType;
              xmlSize.dots = dots;
              put(ticks, xmlSize);
            }
        }
      }
      {
        Scalar multiplier = RationalScalar.of(2, 3); // triole
        if (xmlType.isAvailable(quarter, multiplier)) {
          int duration = xmlType.duration(quarter, multiplier);
          for (int ticks = 0; ticks < measure; ticks += duration)
            if (ticks + duration <= measure) {
              XmlSize xmlSize = new XmlSize();
              xmlSize.duration = duration;
              xmlSize.type = xmlType;
              xmlSize.ratio = multiplier;
              put(ticks, xmlSize);
            }
        }
      }
    }
    for (int ticks : IntRange.positive(measure))
      if (!map.containsKey(ticks))
        throw new RuntimeException("missing entries at ticks " + ticks);
  }

  private void put(int ticks, XmlSize xmlSize) {
    if (map.get(ticks).containsKey(xmlSize.duration))
      System.err.println("collision");
    // System.out.println(ticks + " " + xmlSize.duration + " " + xmlSize);
    map.get(ticks).put(xmlSize.duration, xmlSize);
  }

  private boolean canInsert(int ticks, int duration, int tolerance) {
    int cmp = grad[ticks];
    int measure = division.measure;
    boolean myBoolean = true;
    for (int ofs : IntRange.positive(duration)) {
      int val = grad[Math.floorMod(ticks + ofs, measure)];
      myBoolean &= cmp <= val + tolerance;
      myBoolean &= ticks + ofs < measure;
    }
    return myBoolean;
  }

  public List<XmlNote> getList(int ticks, Note note, Color color) {
    List<XmlNote> list = new LinkedList<>();
    int duration = note.ticks();
    XmlNote xmlNote = null;
    while (0 != duration) {
      Entry<Integer, XmlSize> entry = map.get(division.modMeasure(ticks)).lowerEntry(duration + 1);
      if (entry == null) {
        System.out.println("division " + division);
        System.out.println("ticks    " + ticks);
        System.out.println("duration " + duration);
        duration = 0;
        break;
      }
      // ---
      xmlNote = new XmlNote();
      Tone tone = note.tone();
      xmlNote.step = tone.diatoneAlter().diatone();
      xmlNote.alter = tone.diatoneAlter().alter();
      xmlNote.octave = tone.ivory().octave() - XmlNote.OCTAVE_XML_TO_NATIVE;
      xmlNote.xmlSize = entry.getValue().copy();
      xmlNote.tie = true;
      xmlNote.voice = 1;
      xmlNote.color = color;
      list.add(xmlNote);
      // ---
      ticks += entry.getKey();
      duration -= entry.getKey();
    }
    if (Objects.nonNull(xmlNote))
      xmlNote.tie = false;
    return list;
  }

  public List<XmlNote> getRestList(int ticks, int duration) {
    List<XmlNote> list = new LinkedList<>();
    while (0 != duration) {
      Entry<Integer, XmlSize> entry = map.get(division.modMeasure(ticks)).lowerEntry(duration + 1);
      // ---
      XmlNote xmlNote = new XmlNote();
      xmlNote.step = null; // to indicate that rest
      xmlNote.xmlSize = entry.getValue().copy();
      xmlNote.voice = 1;
      list.add(xmlNote);
      // ---
      ticks += entry.getKey();
      duration -= entry.getKey();
    }
    return list;
  }

  public NavigableMap<Integer, XmlSize> getPartials() {
    NavigableMap<Integer, XmlSize> navigableMap = new TreeMap<>();
    for (Entry<Integer, XmlSize> entry : map.get(0).entrySet())
      if (!entry.getValue().hasTimeModification())
        navigableMap.put(entry.getKey(), entry.getValue().copy());
    return navigableMap;
  }

  public void printout() {
    for (Entry<Integer, NavigableMap<Integer, XmlSize>> entry : new TreeMap<>(map).entrySet())
      System.out.println(String.format("%03d @=%d -> %s", entry.getKey(), grad[entry.getKey()], entry.getValue().keySet().toString()));
  }
}
