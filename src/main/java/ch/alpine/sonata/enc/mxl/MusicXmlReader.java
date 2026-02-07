// code by jph
package ch.alpine.sonata.enc.mxl;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import ch.alpine.bridge.lang.EnumValue;
import ch.alpine.bridge.lang.SI;
import ch.alpine.midkit.MidiInstrument;
import ch.alpine.sonata.Alter;
import ch.alpine.sonata.Clef;
import ch.alpine.sonata.Diatone;
import ch.alpine.sonata.Divisions;
import ch.alpine.sonata.KeyMode;
import ch.alpine.sonata.KeySignature;
import ch.alpine.sonata.Metric;
import ch.alpine.sonata.Note;
import ch.alpine.sonata.Ornament;
import ch.alpine.sonata.ScoreEntry;
import ch.alpine.sonata.Voice;
import ch.alpine.sonata.enc.utl.BarLine;
import ch.alpine.sonata.scr.Score;
import ch.alpine.sonata.scr.ScoreBuffer;
import ch.alpine.sonata.utl.Stylist;
import ch.alpine.sonata.utl.Timescale;
import ch.alpine.sonata.utl.TimescalePacker;
import ch.alpine.sonata.utl.Timeshift;
import ch.alpine.sonata.xml.XmlMeta;
import ch.alpine.sonata.xml.XmlNote;
import ch.alpine.sonata.xml.XmlType;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.Tensors;
import sys.dat.MapOperations;
import sys.dat.UniqueValue;
import sys.io.XmlReader;
import sys.mat.IntRange;
import sys.mat.IntegerMath;
import sys.mat.Ratio;

/** known issues: <staff> tag is ignored (<staff> tag is like voice tag; also: <staves>2</staves> has a meaning)
 * 
 * Sibelius exports some accents/ornaments as <other-direction>
 * 
 * <direction> <direction-type> <other-direction default-y="12" print-object="no">Accent above</other-direction> </direction-type> <voice>1</voice>
 * <staff>1</staff> </direction> <direction> <direction-type> <dynamics default-x="108" default-y="-70" color="#000000" font-family=
 * "Plantin MT Std" font-style="italic" font-size="11.9365" font-weight="normal"> <mf /> </dynamics> </direction-type> <voice>1</voice>
 * <staff>1</staff> </direction> */
// TODO TPF support for figured bass
public class MusicXmlReader extends XmlReader {
  public static final List<String> barLines_noSplit = List.of();
  public static final List<String> barLines_light = List.of("light-light");
  // ---
  final Score myScore = new Score();
  // TODO TPF should be build when parsing "part-list", instead of part (later)
  List<ScoreBuffer> scoreBuffers = new LinkedList<>();
  int voices = 0;
  int credit = 2;
  Collection<String> linkedHashSet = new LinkedHashSet<>();
  List<String> creatorList = new LinkedList<>();
  List<Integer> quarters = new LinkedList<>();
  List<BarLine> barLines = new LinkedList<>();
  List<Clef> clefs = new LinkedList<>();
  Integer ratio_num = null;
  Integer ratio_den = null;
  // ---
  final UniqueValue<Scalar> myTempo = UniqueValue.empty();
  // ---
  ScoreBuffer scoreBuffer;
  UniqueValue<Integer> quarter;
  private XmlDirection xmlDirection = null;
  private XmlNote xmlNote = null; // not null to avoid warning!
  int ticks;
  BarLine barLine = null;
  NavigableMap<Integer, Ratio> ratios = new TreeMap<>();
  NavigableMap<Integer, String> mySeparators = new TreeMap<>();
  NavigableMap<Integer, KeySignature> keySignatures = new TreeMap<>(); // TODO TPF design is still preliminary
  // ---
  private boolean splitAtBarlines = false;
  private boolean logAll = false;
  private PrintStream printStream = null; // new BufferedWriter(new FileWriter(new File("musicxml_dump.txt")));

  private static String fromList(List<String> myList) {
    String myString = myList.toString();
    return myString.substring(1, myString.length() - 1);
  }

  public void read(InputStream inputStream) throws Exception {
    parse(inputStream);
    // ---
    NavigableMap<Integer, String> myWords = new TreeMap<>(myScore.text);
    myScore.text.clear();
    // ---
    // System.out.println(myQuarters);
    myScore.quarter = IntegerMath.lcm(quarters);
    myScore.keySignature = keySignatures.firstEntry().getValue();
    // ---
    if (myTempo.isPresent())
      myScore.bpm = myTempo.orElseThrow();
    // ---
    {
      if (!creatorList.isEmpty())
        myScore.comment = fromList(creatorList);
    }
    {
      List<String> myList = new ArrayList<>(linkedHashSet); // new ArrayList<>(myTitleMap.values());
      int size = myList.size();
      if (myScore.comment.isEmpty())
        size = (size + 1) / 2;
      myScore.title = fromList(myList.subList(0, size));
      if (myScore.comment.isEmpty())
        myScore.comment = fromList(myList.subList(size, myList.size()));
    }
    // TODO TPF not sure if correct
    myScore.staffPartition = Tensors.vectorInt(scoreBuffers.stream().mapToInt(s -> s.voices()).toArray()).toString();
    // System.out.println("myScore.staffPartition = " + myScore.staffPartition);
    // ---
    Iterator<Integer> myQuarterIterator = quarters.iterator();
    Iterator<BarLine> myMeasureIterator = barLines.iterator();
    int bufferIndex = 0;
    for (ScoreBuffer scoreBuffer : scoreBuffers) {
      int part_quarter = myQuarterIterator.next();
      int part_stretch = myScore.quarter / part_quarter;
      // int part_measure = 4 * part_quarter * myNum.get() / myDen.get();
      BarLine myBarLine = myMeasureIterator.next();
      int part_delay = myBarLine.getDelay(); // TODO TPF this only works if barLine is not updated throughout part!!!
      for (Voice voice : scoreBuffer.getVoices()) {
        if (bufferIndex < midiInstruments.size())
          voice.midiInstrument = midiInstruments.get(bufferIndex);
        // TODO TPF use collection of clefs per voice, and only fallback on piano if empty!
        Stylist.assignBestClef(voice, Clef.piano_sheet);
        // if (!myClefs.isEmpty())
        // myVoice.myClef = myClefs.get(Math.min(myScore.voices(), myClefs.size() - 1));
        // System.out.println("part_delay: " + part_delay);
        Timeshift.by(voice, part_delay);
        // System.out.println("part_stretch: " + part_stretch);
        Timescale.stretchNotes(voice, part_stretch);
        myScore.voices.add(voice);
      }
      ++bufferIndex;
    }
    MapOperations.scaleMap(ratios, myScore.quarter);
    MapOperations.scaleMap(mySeparators, myScore.quarter);
    MapOperations.scaleMap(myWords, myScore.quarter);
    for (Entry<Integer, String> myEntry : myWords.entrySet())
      myScore.appendText(myEntry.getKey(), myEntry.getValue().toString(), true);
    // ---
    // if (1 < myRatios.size())
    // for (Entry<Integer, Ratio> myEntry : myRatios.entrySet())
    // myScore.appendText(myEntry.getKey(), myEntry.getValue().toString(), true);
  }

  public List<Score> getScores() {
    List<Score> myList = new LinkedList<>();
    NavigableSet<Integer> myNavigableSet = new TreeSet<>();
    myNavigableSet.addAll(ratios.keySet());
    if (splitAtBarlines)
      myNavigableSet.addAll(mySeparators.keySet());
    for (int beg : myNavigableSet) {
      Integer end = myNavigableSet.higher(beg);
      if (end == null)
        end = myScore.ticks();
      // ---
      IntRange myTicksRange = new IntRange(beg, end);
      if (!myTicksRange.isEmpty()) {
        Score myMvt = new Score();
        myMvt.takesHeaderFrom(myScore);
        for (Voice myVoice : myScore.voices) {
          Voice myExtract = myVoice.extract(beg, end);
          if (!myExtract.navigableMap.isEmpty())
            myMvt.voices.add(myExtract);
        }
        myMvt.text.putAll(myScore.text.subMap(beg, end));
        MapOperations.translate(myMvt.text, -beg);
        // ---
        TimescalePacker myTimescalePacker = new TimescalePacker(myMvt, false);
        if (myTimescalePacker.canPack())
          myTimescalePacker.pack();
        else {
          if (!Metric.quartersList.contains(myScore.quarter)) {
            System.out.println("quarter no good: " + myScore.quarter);
            System.out.println("pack fail, " + myTimescalePacker.gcd + ": " + myTimescalePacker.ticks + " reason " + myTimescalePacker.myString);
          }
        }
        Ratio ratio = ratios.lowerEntry(beg + 1).getValue();
        final int measure = Scalars.intValueExact(ratio.toFraction().multiply(RealScalar.of(4 * myMvt.quarter)));
        if (!Metric.measuresList.contains(measure))
          System.out.println("measure no good: " + measure);
        myMvt.division = Divisions.best(myMvt.quarter, measure, ratio);
        Stylist.defaultStyle(myMvt);
        if (!myMvt.voices.isEmpty())
          myList.add(myMvt);
      }
    }
    return myList;
  }

  public Score getScore() {
    List<Score> myList = getScores();
    if (1 < myList.size())
      log("multi-score file", false);
    return myList.get(0);
  }

  @Override
  public void push(String token, String group) throws Exception {
    // mutually exclusive so ordering does not matter
    // we order based on frequency
    if (token.equals("note")) {
      xmlNote = new XmlNote();
      // myXmlNote.isHidden = myGroup.contains(" print-object=\"no\"");
    } else if (token.equals("measure")) {
      try {
        ticks = barLine.markAt(ticks, false);
      } catch (Exception exception) {
        log(exception.getMessage(), false);
      }
      log(String.format("measure count=%4d", barLine.getBarCount()), true);
    } else if (token.equals("direction")) { // this does not always imply dynamics, but also just <words>
      xmlDirection = new XmlDirection();
    } else if (token.equals("time")) {
      ratio_num = null;
      ratio_den = null;
    } else if (token.equals("part")) {
      scoreBuffer = new ScoreBuffer();
      quarter = UniqueValue.empty();
      barLine = new BarLine();
      ticks = 0;
      log("--- new part ---", true);
    }
  }

  @Override
  public void pushpop(String token, String group) throws Exception {
    // System.out.println("pushPop " + myToken + " " + myGroup);
    if (Objects.nonNull(xmlNote)) {
      // BEGIN: for <note>
      if (token.equals("chord"))
        xmlNote.isChord = true;
      else if (token.equals("tie")) {
        xmlNote.tie |= group.contains("start");
      } else if (token.equals("tied")) {
        /** <notations><tied type="start"/></notations> */
        xmlNote.tie |= group.contains("start");
      } else if (token.equals("slur")) {
        /** does not always connect notes with identical tone. in case of a mismatch, the tie will be dropped.
         * 
         * <notations><slur number="1" placement="above" type="start"/></notations> */
        xmlNote.tie |= group.contains("start");
      } else if (token.equals("grace")) {
        xmlNote.isGrace = true;
        xmlNote.xmlSize.duration = 0;
      } else if (token.equals("dot")) { // increment for each <dot/>
        ++xmlNote.xmlSize.dots; // Finale MusicXML Export supports 2 dots: Bach_Flute.bwv1013_2
      } else if (token.equals("fermata")) {
        /** <fermata type=\"upright\"/> */
        xmlNote.ornament = Ornament.FERMATA;
      } else if (peek().equals("ornaments")) {
        xmlNote.ornament = XmlMeta.fromXmlNotationOrnament(token);
        log("ornament: " + token, true);
      }
      // END: for <note>
    } else if (token.equals("sound")) {
      int beg = group.indexOf("tempo=\"");
      if (0 < beg) {
        beg += 7;
        int end = group.indexOf("\"", beg);
        try {
          String string = group.substring(beg, end);
          myTempo.set(SI.PER_MINUTE.quantity(Scalars.fromString(string)));
        } catch (Exception exception) {
          myScore.appendText(ticks, "tempo " + group.substring(beg, end), true);
          // System.out.println("tempo: " + myException.getMessage());
        }
      }
    } else if (peek().equals("dynamics"))
      xmlDirection.dynamics = XmlMeta.fromXmlDynamics(token);
  }

  String clefSign = "G";
  int clefLine = 2;
  Note graceNote = null;

  private void processXmlNote() {
    if (Objects.nonNull(xmlNote.xmlSize.type) && !xmlNote.isGrace && xmlNote.xmlSize.hasTimeModification()) {
      try {
        quarter.set(Scalars.intValueExact(RationalScalar.of(xmlNote.xmlSize.duration, 4).divide(xmlNote.xmlSize.getCombinedFraction())));
      } catch (Exception exception) {
        myScore.appendText(ticks, xmlNote.toInfoString(), true);
        xmlNote.step = null; // condemn note to become rest
        log(xmlNote.xmlSize.getCombinedFraction() + " note discarded: " + exception.getMessage(), false);
      }
    }
    if (xmlNote.isRest()) {
      log(String.format("%s", xmlNote.toInfoString()), true);
    } else {
      if (!xmlNote.isHidden)
        try { // necessary in case myXmlNote is corrupt so that calls such as toNote, or toInfoString cause an exception
          // the majority of xml files define the attribute "type", but technically "type" is redundant... so we don't require it and check for null:
          if (Objects.nonNull(xmlNote.xmlSize.type) && !xmlNote.isGrace)
            try {
              quarter.set(Scalars.intValueExact(RationalScalar.of(xmlNote.xmlSize.duration, 4).divide(xmlNote.xmlSize.getCombinedFraction())));
            } catch (Exception exception) {
              log(exception.getMessage(), false);
            }
          if (xmlNote.isChord) {
            myScore.appendText(ticks, xmlNote.toInfoString(), true);
            log(String.format("skip chord %s", xmlNote.toInfoString()), false);
          } else //
          if (xmlNote.isGrace) {
            graceNote = xmlNote.toNote();
            log(String.format("buffer grace %s", xmlNote.toInfoString()), true);
          } else { //
            if (scoreBuffer.isFreeAt(ticks, xmlNote.voice)) {
              if (Objects.nonNull(graceNote) && xmlNote.ornament == null) {
                try {
                  Note note = xmlNote.toNote();
                  int delta = note.tone().ivory().ivory() - graceNote.tone().ivory().ivory();
                  switch (delta) {
                  case 1:
                    xmlNote.ornament = Ornament.UP_ACCIACCATURA;
                    break;
                  case -1:
                    xmlNote.ornament = Ornament.DOWN_ACCIACCATURA;
                    break;
                  default:
                    myScore.appendText(ticks, graceNote.toString(), true);
                    break;
                  }
                  log("grace " + graceNote + " to " + note + " => " + xmlNote.ornament, true);
                } catch (Exception exception) {
                  log(exception.getMessage(), false);
                }
              }
              try {
                scoreBuffer.put(xmlNote.toNote(), xmlNote.tie, ticks, xmlNote.voice);
              } catch (Exception exception) {
                log(exception.getMessage(), false);
                log("untie", false);
                scoreBuffer.untie(xmlNote.voice);
                try {
                  scoreBuffer.put(xmlNote.toNote(), xmlNote.tie, ticks, xmlNote.voice);
                } catch (Exception myException2) {
                  myScore.appendText(ticks, "?", true);
                  log(myException2.getMessage(), false);
                }
              }
              ScoreEntry myScoreEntry = new ScoreEntry(ticks, xmlNote.voice);
              scoreBuffer.shake(myScoreEntry, xmlNote.ornament);
              if (!xmlNote.lyric.isEmpty())
                scoreBuffer.lyric(xmlNote.getLyric(), ticks, xmlNote.voice);
              log(String.format("insert %s", xmlNote.toInfoString()), true); // might throw an exception
            } else {
              log(String.format("problem inserting %s !", xmlNote.toInfoString()), false);
            }
            graceNote = null;
          }
        } catch (Exception exception) {
          exception.printStackTrace();
        }
      // fi: note is hidden
    }
    // ---
    if (xmlNote.isChord)
      log("part of chord: " + xmlNote.toInfoString(), false);
    else
      ticks += xmlNote.xmlSize.duration;
    xmlNote = null;
  }

  @Override
  public void pop(String token) throws Exception {
    if (token.equals("note")) {
      processXmlNote();
    } else //
    if (token.equals("direction")) {
      ScoreEntry myScoreEntry = new ScoreEntry(ticks, xmlDirection.voice);
      scoreBuffer.press(myScoreEntry, xmlDirection.dynamics);
      xmlDirection = null;
    } else //
    if (token.equals("clef")) {
      Clef clef = Clef.fromSignLineSafe(clefSign, clefLine);
      clefs.add(clef);
      log("convert " + clefSign + " " + clefLine + " to " + clef, true);
    } else if (token.equals("part")) {
      log("part finished", true);
      scoreBuffers.add(scoreBuffer);
      voices += scoreBuffer.voices();
      quarters.add(quarter.orElseThrow());
      barLines.add(barLine);
      scoreBuffer = null;
      barLine = null;
    } else if (token.equals("time")) {
      Scalar meter = new Ratio(ratio_num, ratio_den).toFraction();
      Scalar whole = RealScalar.of(4 * quarter.orElseThrow());
      int measure = Scalars.intValueExact(meter.multiply(whole));
      log("meter: " + meter + " whole: " + whole + " m=" + measure + " t=" + ticks, true);
      int key = ticks / quarter.orElseThrow();
      Entry<Integer, Ratio> entry = ratios.lowerEntry(key);
      if (Objects.nonNull(entry)) {
        barLine.changeTo(ticks, measure);
        log("barline change " + measure, true);
      }
      ratios.put(key, new Ratio(ratio_num, ratio_den));
      ratio_num = null; // pop time
      ratio_den = null;
    } else if (token.equals("fermata")) { // could be also at pop, since no parameters are used
      /** <fermata type="upright">angled</fermata> */
      xmlNote.ornament = Ornament.FERMATA;
    }
  }

  @Override
  public void string(String token, String string) throws Exception {
    if (Objects.nonNull(xmlNote)) {
      // System.out.println(myToken+" ");
      if (token.equals("step"))
        xmlNote.step = Diatone.valueOf(string);
      else if (token.equals("alter"))
        xmlNote.alter = Alter.fromDelta(Integer.parseInt(string));
      else if (token.equals("octave"))
        xmlNote.octave = Integer.parseInt(string);
      else if (token.equals("duration"))
        xmlNote.xmlSize.duration = Integer.parseInt(string);
      else if (token.equals("voice"))
        xmlNote.voice = Integer.parseInt(string);
      else if (token.equals("type"))
        xmlNote.xmlSize.type = XmlType.from(string.trim());
      if (contains("lyric")) {
        if (token.equals("text")) {
          if (!xmlNote.lyric.isEmpty())
            xmlNote.lyric += "|";
          xmlNote.lyric += string;
        } else if (token.equals("syllabic")) { // begin middle end
          xmlNote.lyric_dash |= string.equals("begin") || string.equals("middle");
        }
      } else if (contains("time-modification")) {
        // System.out.println("time-modification");
        if (token.equals("actual-notes"))
          xmlNote.xmlSize.ratio = xmlNote.xmlSize.ratio.divide(Scalars.fromString(string));
        else if (token.equals("normal-notes"))
          xmlNote.xmlSize.ratio = xmlNote.xmlSize.ratio.multiply(Scalars.fromString(string));
      }
    } else { // outside of <note>
      if (contains("direction")) {
        if (token.equals("words")) {
          // System.out.println("words " + myString);
          myScore.appendText(ticks / quarter.orElseThrow(), string, true);
        }
      } else {
        if (contains("barline"))
          if (token.equals("bar-style")) {
            if (string.equals("light-light") || //
                string.equals("light-heavy")) {
              mySeparators.put(ticks / quarter.orElseThrow(), string);
            }
          }
      }
      if (Objects.nonNull(xmlDirection)) {
        if (token.equals("voice"))
          xmlDirection.voice = Integer.parseInt(string);
      } else // else inserted but needs testing
      if (token.equals("duration")) {
        int delta = Integer.parseInt(string);
        if (contains("backup")) { // <backup><duration>1024</duration></backup>
          log("backup by " + delta, true);
          ticks -= delta;
        } else //
        if (contains("forward")) {
          log("forward by " + delta, true);
          ticks += delta;
        }
      } else //
      if (contains("attributes")) {
        // System.out.println(peek());
        if (token.equals("divisions")) {
          quarter.set(Integer.parseInt(string));
        } else if (token.equals("fifths")) {
          if (!keySignatures.containsKey(ticks))
            keySignatures.put(ticks, KeySignature.fromType(Integer.parseInt(string)));
        } else if (token.equals("mode")) {
          KeyMode myKeyMode = KeyMode.fromModernName(string);
          if (Objects.nonNull(myKeyMode))
            myScore.setKeyMode(myKeyMode);
        } else if (token.equals("beats"))
          ratio_num = Integer.parseInt(string);
        else if (token.equals("beat-type"))
          ratio_den = Integer.parseInt(string);
        else if (peek().equals("clef")) {
          if (token.equals("sign")) {
            clefSign = string.trim();
          } else //
          if (token.equals("line")) {
            clefLine = Integer.parseInt(string.trim());
          }
        }
      } else if (token.equals("work-title"))
        addHeaderInfo(linkedHashSet, string);
      else if (token.equals("movement-title"))
        addHeaderInfo(linkedHashSet, string);
      else if (token.equals("credit-words") && contains("credit")) {
        // <credit-type>title</credit-type>
        // <credit-type>composer</credit-type>
        addHeaderInfo(linkedHashSet, string);
      } else if (token.equals("creator")) {
        // <creator type="composer">Guillaume Du Fay</creator>
        // <creator type="lyricist">Rodin 17 July 2014</creator>
        // <creator type="arranger">Planchart</creator>
        addHeaderInfo(creatorList, string);
      } else if (token.equals("words")) {
        // outside of <note>
        // <direction placement="above">
        // <direction-type>
        // <words default-y="15" font-size="9.2" font-weight="bold" relative-x="-19">Section: Sanctus</words>
        // </direction-type>
        // </direction>
        // myScore.appendText(ticks, myString, newLine);
      } else if (token.equals("midi-program")) {
        MidiInstrument midiInstrument = EnumValue.fromOrdinal(MidiInstrument.class, Integer.parseInt(string.trim()) - 1);
        midiInstruments.add(midiInstrument);
      }
    }
  }

  private final List<MidiInstrument> midiInstruments = new ArrayList<>();
  static final Pattern charPattern = Pattern.compile("[a-zA-Z0-9]");

  private static void addHeaderInfo(Collection<String> collection, String string) {
    string = string.trim();
    if (charPattern.matcher(string).find()) {
      // System.out.println("add: " + myString);
      // System.out.println(" xx " + FriendlyFormat.hexString(myString.getBytes(), 0, myString.length(), "%02x "));
      collection.add(string);
    }
  }

  public void setSplitAtBarlines(boolean myBoolean) {
    splitAtBarlines = myBoolean;
  }

  public void setPrintStream(PrintStream printStream, boolean logAll) {
    this.printStream = printStream;
    this.logAll = logAll;
  }

  public void log(String string, boolean myBoolean) {
    if (Objects.nonNull(printStream) && (logAll || !myBoolean))
      printStream.println(String.format("%5d @=%6d %s%s", getLine(), ticks, myBoolean ? "" : "[ERROR] ", string));
  }
}
