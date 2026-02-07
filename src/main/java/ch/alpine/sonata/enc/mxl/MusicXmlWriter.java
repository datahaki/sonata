// code by jph
package ch.alpine.sonata.enc.mxl;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Optional;

import ch.alpine.bridge.lang.SI;
import ch.alpine.sonata.Dynamic;
import ch.alpine.sonata.FiguredBass;
import ch.alpine.sonata.Meter;
import ch.alpine.sonata.Voice;
import ch.alpine.sonata.enc.api.StaffPartition;
import ch.alpine.sonata.enc.utl.CoverageBundle;
import ch.alpine.sonata.enc.utl.MeasureChop;
import ch.alpine.sonata.enc.utl.XmlDurationMap;
import ch.alpine.sonata.scr.Score;
import ch.alpine.sonata.seq.SimpleChannelMap;
import ch.alpine.sonata.tri.ChromaticFormat;
import ch.alpine.sonata.tri.MusicXmlChords;
import ch.alpine.sonata.tri.Triad;
import ch.alpine.sonata.tri.TriadFormat;
import ch.alpine.sonata.xml.XmlMeta;
import ch.alpine.sonata.xml.XmlNote;
import sys.io.XmlReader;
import sys.io.XmlWriter;
import sys.mat.IntRange;

/** implementation does NOT support: partial measures; in-between figured-bass */
public class MusicXmlWriter extends XmlWriter {
  /** @param outputStream has to be closed outside
   * @param score
   * @throws Exception */
  public MusicXmlWriter(OutputStream outputStream, Score score) throws Exception {
    // TODO TPF text is not exported: this is how
    // <direction>
    // <direction-type>
    // <words default-x="170" default-y="13" justify="left" valign="middle" font-family="Plantin MT Std" font-style="normal" font-size="11.9365"
    // font-weight="normal">MYTEXT4</words>
    // </direction-type>
    // <voice>1</voice>
    // <staff>1</staff>
    // </direction>
    /** <lyric default-y="-80" number="1" color="#000000"> <syllabic>single</syllabic> <text>why</text> </lyric>
     * 
     * <lyric default-y="-80" number="1" color="#000000"> <syllabic>begin</syllabic> <text>is</text> </lyric>
     * 
     * <lyric default-y="-80" number="1" color="#000000"> <syllabic>end</syllabic> <text>this</text> </lyric> */
    super(new BufferedWriter(new OutputStreamWriter(outputStream, XmlReader.CHARSET)));
    try {
      writeln("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      writeln("<!DOCTYPE score-partwise PUBLIC \"-//Recordare//DTD MusicXML 2.0 Partwise//EN\"");
      writeln("      \"http://www.musicxml.org/dtds/partwise.dtd\">");
      push("score-partwise");
      push("work");
      push("work-number");
      pop("work-number");
      push("work-title");
      content(score.title);
      pop("work-title");
      pop("work");
      push("movement-number");
      pop("movement-number");
      // ---
      push("movement-title");
      content(score.title);
      pop("movement-title");
      // ---
      push("identification");
      push("creator", "type=\"composer\"");
      content(score.comment);
      pop("creator");
      // <creator type="lyricist">lyricist</creator>
      // <creator type="arranger">arranger</creator>
      // <rights>copyright</rights>
      push("encoding");
      push("software");
      content("ThePirateFugues");
      pop("software");
      // ---
      push("encoding-date");
      content(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
      pop("encoding-date");
      pop("encoding");
      pop("identification");
      // ---
      push("part-list");
      SimpleChannelMap simpleChannelMap = new SimpleChannelMap(score.voices);
      List<List<Voice>> myParts = StaffPartition.getStaffList(score);
      for (int _part : IntRange.positive(myParts.size())) {
        final int part = _part + 1;
        push("score-part", "id=\"P" + part + "\"");
        push("part-name");
        /** MusicXML requires a title for each part-name/staff currently in use: s1, s2, ... classic: SMB; SATB; SAMTB Josquin: Superius, Contratenor/Altus,
         * Tenor, Bassus Female voices: Soprano, Mezzo-soprano, Contralto Male voices: Countertenor, Tenor, Baritone, Bass */
        content("s" + part);
        pop("part-name");
        // ---
        {
          push("score-instrument", "id=\"P" + part + "-I1\"");
          push("instrument-name");
          content(myParts.get(_part).get(0).midiInstrument.name());
          pop("instrument-name");
          pop("score-instrument");
        }
        {
          push("midi-instrument", "id=\"P" + part + "-I1\"");
          {
            push("midi-channel");
            content(Integer.toString(simpleChannelMap.channel(myParts.get(_part).get(0).midiInstrument) + 1));
            pop("midi-channel");
          }
          {
            push("midi-program");
            content(Integer.toString(myParts.get(_part).get(0).midiInstrument.ordinal() + 1));
            pop("midi-program");
          }
          {
            push("pan");
            content("0");
            pop("pan");
          }
          pop("midi-instrument");
        }
        pop("score-part");
      }
      pop("part-list");
      // ---
      final int measure = score.measure();
      final int total = score.measures() * measure;
      // ---
      final TriadFormat triadFormat = new ChromaticFormat(score.keySignature, new MusicXmlChords());
      final NavigableMap<Integer, Triad> chord = CoverageBundle.fromChords(score.triad).projectOnsets();
      // ---
      MeasureChop measureChop = new MeasureChop( //
          new XmlDurationMap(score.division, score.quarter, 1), //
          new XmlDurationMap(score.division, score.quarter, 0)); // Finale 2011 misinterprets dotted rests
      int part = 0;
      for (List<Voice> myStaff : myParts) {
        ++part;
        // writeln("<part id=\"P" + part + "\">");
        push("part", "id=\"P" + part + "\"");
        // ---
        MusicXmlPart musicXmlPart = new MusicXmlPart(measureChop, myStaff, total);
        int ticks = 0;
        // BEGIN: all measures in part
        for (final int measureCount : musicXmlPart.measureIndices()) {
          {
            Voice myVoice = musicXmlPart.getVoice(0); // take clef etc from first voice
            push("measure", "number=\"" + (measureCount + 1) + "\"");
            if (measureCount == 0) {
              push("attributes");
              writeln("<divisions>" + score.quarter + "</divisions>");
              push("key");
              writeln("<fifths>" + score.keySignature.type() + "</fifths>");
              writeln(XmlMeta.toModeString(score.keyMode()));
              pop("key");
              Meter meter = Meter.of(score.quarter, score.division);
              push("time");
              writeln("<beats>" + meter.num() + "</beats>");
              writeln("<beat-type>" + meter.den() + "</beat-type>");
              pop("time");
              writeln(XmlMeta.toClefString(myVoice.clef));
              pop("attributes");
              writeln("<sound tempo=\"" + SI.PER_MINUTE.magnitude(score.bpm) + "\"/>");
            }
          }
          // BEGIN: all voices in measure
          for (final int voiceCount : musicXmlPart.voiceIndices()) {
            if (0 < voiceCount) {
              writeln("<backup><duration>" + measure + "</duration></backup>");
              ticks -= measure;
            }
            // ---
            Voice voice = musicXmlPart.getVoice(voiceCount);
            // BEGIN: all notes of voice in measure
            for (XmlNote xmlNote : musicXmlPart.getChopAt(voiceCount, measureCount)) {
              // ---
              // does not capture 'in-between' figures
              if (voice.fbass.containsKey(ticks)) {
                FiguredBass figuredBass = voice.fbass.get(ticks);
                if (!figuredBass.isDefault())
                  writeln(XmlFiguredBass.format(figuredBass).toString());
              }
              // ---
              xmlNote.voice = 1 + voiceCount;
              xmlNote.ornament = voice.shake.get(ticks);
              List<Dynamic> press = new LinkedList<>( //
                  voice.press.subMap(ticks, ticks + xmlNote.xmlSize.duration).values());
              if (!press.isEmpty())
                xmlNote.dynamics = press.get(press.size() - 1);
              // ---
              if (voice.lyric.containsKey(ticks))
                xmlNote.lyric = voice.lyric.get(ticks);
              // ---
              writeln(xmlNote.toXmlString(musicXmlPart.tie_stop[voiceCount]));
              musicXmlPart.tie_stop[voiceCount] = xmlNote.tie;
              // ---
              ticks += xmlNote.xmlSize.duration;
            }
            // END: all notes of voice in measure
            if (part == 1 && voiceCount == 0) {
              NavigableMap<Integer, Triad> navigableMap = (NavigableMap<Integer, Triad>) chord.subMap(ticks - measure, ticks);
              if (!navigableMap.isEmpty()) {
                int caret = navigableMap.firstKey();
                writeln("<backup><duration>" + (ticks - caret) + "</duration></backup>");
                for (Entry<Integer, Triad> myEntry : navigableMap.entrySet()) {
                  writeln(triadFormat.format(myEntry.getValue()));
                  int durat = Optional.ofNullable(navigableMap.higherKey(myEntry.getKey())).orElse(ticks) - caret;
                  writeln("<forward><duration>" + durat + "</duration></forward>");
                  caret += durat;
                }
                if (caret != ticks)
                  throw new RuntimeException("ups! check your math"); // can be removed after a while of testing
              }
            }
          }
          // END: all voices in measure
          if (ticks == total)
            writeln("<barline><bar-style>light-heavy</bar-style></barline>");
          pop("measure");
        }
        // END: all measures in part
        pop("part");
      }
      // ---
      pop("score-partwise");
    } finally {
      flush();
    }
    checkStackEmpty();
  }
}
