// code by jph
package ch.alpine.sonata.enc.ly;

import java.awt.Color;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.stream.Collectors;

import ch.alpine.bridge.lang.SI;
import ch.alpine.midkit.VoiceGroup;
import ch.alpine.sonata.Attributes;
import ch.alpine.sonata.Dynamic;
import ch.alpine.sonata.FiguredBass;
import ch.alpine.sonata.Meter;
import ch.alpine.sonata.Note;
import ch.alpine.sonata.QuintenZirkel;
import ch.alpine.sonata.Torrent;
import ch.alpine.sonata.Voice;
import ch.alpine.sonata.enc.api.StaffPartition;
import ch.alpine.sonata.enc.utl.CoverageBundle;
import ch.alpine.sonata.enc.utl.MeasureChop;
import ch.alpine.sonata.enc.utl.XmlDurationMap;
import ch.alpine.sonata.lyr.LyricsFormatter;
import ch.alpine.sonata.scr.Score;
import ch.alpine.sonata.tri.ChromaticFormat;
import ch.alpine.sonata.tri.LilypondTriads;
import ch.alpine.sonata.tri.Triad;
import ch.alpine.sonata.tri.TriadFormat;
import ch.alpine.sonata.utl.FiguredTones;
import ch.alpine.sonata.xml.XmlNote;
import ch.alpine.sonata.xml.XmlSize;
import sys.col.HuePalette;
import sys.mat.IntRange;
import sys.mat.IntegerMath;

public class LilypondScore {
  public final Score myScore;
  private final LilypondLayout lilypondLayout;
  private final LilypondParam lilypondParam;

  public LilypondScore(Score score, LilypondLayout lilypondLayout, LilypondParam lilypondParam) {
    this.myScore = score;
    this.lilypondLayout = lilypondLayout;
    this.lilypondParam = lilypondParam;
  }

  public boolean lyricsAboveStaff = false;
  public boolean lyricsSwitchStaff = false;
  // ---
  private StringBuilder stringBuilder;

  String getString() {
    stringBuilder = new StringBuilder();
    writeScore();
    return stringBuilder.toString();
  }

  // arpeggio does not work on single notes, but requires a list of at least two:
  // <c'' b''>2\arpeggio
  /** exports entire score to LilyPond format
   * acciaccatura ornaments are substituted with note sequence
   * 
   * @param myScore
   * @param lilypondParam
   * @throws Exception */
  public void writeScore() {
    final Score score = Acciaccaturas.replaceAll(myScore);
    // ---
    final TriadFormat triadFormat = new ChromaticFormat(score.keySignature, new LilypondTriads());
    writeln("\\score {");
    writeln("<<");
    /** GrandStaff and StaffGroup draws vertical bar lines from first staff to last after each measure
     * GrandStaff creates curly bracket '{'
     * StaffGroup creates bracket '[' */
    // writeln(" \\new GrandStaff <<"); //
    writeln(" \\new StaffGroup <<"); //
    writeln("  \\accidentalStyle Score.piano");
    Meter meter = score.getMeter();
    final int measure = score.measure();
    XmlDurationMap xmlDurationMap = new XmlDurationMap(score.division, score.quarter, 2); // support for double dot, etc.
    int entry = 0;
    String myPartial = "";
    { // determine partial, i.e. amount of first measure that can be omitted
      int score_entry = score.anyEntry(0); // trouble if score is empty!?
      if (lilypondParam.partial && 0 < score_entry && score_entry < measure) {
        NavigableMap<Integer, XmlSize> navigableMap = xmlDurationMap.getPartials();
        Entry<Integer, XmlSize> myEntry = navigableMap.higherEntry(measure - score_entry - 1);
        if (Objects.nonNull(myEntry)) {
          entry = measure - myEntry.getKey();
          String substring = myEntry.getValue().toLyString();
          myPartial = "    \\partial " + substring + "\n";
        }
      }
    }
    // ---
    int total = score.ticks();
    if (lilypondParam.chord && !score.triad.isEmpty())
      total = Math.max(total, score.triad.lastKey());
    // ---
    // text can be at any ticks while lyrics are supposed to coincide with notes:
    if (lilypondParam.lyric && !score.text.isEmpty())
      total = Math.max(total, score.text.lastKey());
    // ---
    if (!lilypondParam.partial)
      total = IntegerMath.ceilDiv(total, measure) * measure; // round up
    // ---
    final MeasureChop measureChop = new MeasureChop(xmlDurationMap, xmlDurationMap);
    // BEGIN: chord names
    if (lilypondParam.chord && !score.triad.isEmpty()) {
      final NavigableMap<Integer, Triad> chord = CoverageBundle.fromChords(score.triad).projectOnsets();
      writeln("  \\new ChordNames {");
      writeln("    \\chordmode {");
      write(myPartial);
      writeln("    \\override ChordName #'font-size = #0");
      // \override ChordName #'font-series = #'bold
      // if desired more interception points can be inserted (for instance using ScoreJoint)
      insertMap(entry, measureChop.fromIntercepts((NavigableSet<Integer>) chord.keySet(), entry, total), new LilypondMap() {
        @Override
        public void intercept(int ticks, XmlNote xmlNote) {
          String duration = xmlNote.toLyString().substring(1); // substring(1) because of rest prefix r/s/R
          if (chord.containsKey(ticks)) {
            String myName = triadFormat.format(chord.get(ticks));
            int index = myName.indexOf(':'); // TODO TPF what is this!?
            if (index < 0)
              index = myName.length();
            write(myName.substring(0, index) + duration + myName.substring(index));
          } else
            write("s" + duration); // rest in between chords
        }
      });
      writeln("    }");
      writeln("  }");
    }
    // END: chord names
    // ---
    {
      if (lilypondParam.lyric && lyricsAboveStaff) {
        Voice myVoice = score.voices.get(0);
        insertLyrics(myVoice.lyric, myPartial, entry, total, measureChop);
      }
    }
    // BEGIN: voice & notes
    int staff_count = 0;
    // the use of keymode is discouraged so we use major for everything. for example:
    // instead of telling lilypond a \minor, we simply use c \major
    final String key = LilypondTriads.of(QuintenZirkel.typeToDiatoneAlter(score.keySignature.type()));
    System.out.println("score.staffPartition=" + score.staffPartition);
    for (List<Voice> myStaffRaw : StaffPartition.getStaffList(score)) {
      // TODO TPF ensure that myStaffRaw.is
      List<Voice> myStaffOrd = new LinkedList<>();
      {
        // permute voices of staff as recommended
        for (int index : LilypondVoiceOrdering.get(myStaffRaw.size()))
          myStaffOrd.add(myStaffRaw.get(index));
      }
      { // staff header
        final Voice myFirst = myStaffOrd.get(0); // first voice determines clef of staff
        writeln("  \\new Staff = " + staffId(staff_count)); // \accidentalStyle ... page 25
        if (false) {
          boolean big = false;
          big |= VoiceGroup.PIANO.isMember(myFirst.midiInstrument);
          big |= VoiceGroup.CHROMATIC_PERC.isMember(myFirst.midiInstrument);
          big |= VoiceGroup.ORGAN.isMember(myFirst.midiInstrument);
          if (!big)
            writeln("  \\with { \\magnifyStaff #5/7 }");
        }
        writeln("  {");
        if (Objects.nonNull(score.bpm))
          writeln("%    \\tempo 4 = " + SI.PER_MINUTE.intValue(score.bpm));
        writeln("    \\clef \"" + myFirst.clef.toString().toLowerCase() + "\"");
        writeln("    \\key " + key + " \\major"); // + score.keyMode().toString().toLowerCase()
        // writeln(" \\numericTimeSignature"); // forces 4/4 to appear instead of C
        writeln("    \\time " + meter);
      }
      writeln("  <<");
      int staff_voice = 0;
      final int staff_voices = myStaffOrd.size();
      for (Voice myVoice : myStaffOrd) {
        writeln("    {"); // voice header
        writeln("    \\set Score.currentBarNumber = #0"); // tpf standard: first measure has number 0
        writeln("    \\mergeDifferentlyHeadedOn"); // collision resolution
        writeln("    \\mergeDifferentlyDottedOn"); // collision resolution
        writeln("    \\shiftOn"); // collision resolution TODO TPF not recommended for all voices...
        writeln("    \\showStaffSwitch"); // antidote to \hideStaffSwitch
        // to change size of elements in voice: \huge \large \normalsize \small \tiny \teeny
        // mensural notation: neomensural, mensural, and petrucci
        // \override NoteHead.style = #'petrucci
        write(myPartial);
        int ticks = entry;
        int count = 0;
        Integer resetColor = null;
        String myIndex = "";
        TupletCacher tupletCacher = createTupletCacher();
        for (List<XmlNote> myList : measureChop.fromVoice(myVoice, ticks, total)) {
          barCheck(count);
          write("    ");
          boolean singleRest = myList.size() == 1 && myList.get(0).isRest();
          for (XmlNote xmlNote : myList) {
            if (myVoice.navigableMap.containsKey(ticks)) {
              Torrent torrent = myVoice.navigableMap.get(ticks);
              Attributes attributes = torrent.attributes;
              if (attributes.isAppended()) {
                if (lilypondParam.color) {
                  // in the pdf colors come out bright, thus 'value' darker
                  {
                    Color color = HuePalette.LUMA.getColor(attributes.getIndex(), 1, .8, 1);
                    writeln("\n    \\override Voice.NoteHead #'color = " + LilypondConstants.rgbColor(color));
                  }
                  {
                    Color color = HuePalette.LUMA.getColor(attributes.getIndex(), 1, .7, 1);
                    writeln("    \\override Voice.Stem #'color = " + LilypondConstants.rgbColor(color));
                  }
                  // {
                  // Color myColor = LumaPalette.default13.getColorCyclic(myAttributes.getIndex(), 1, .6, 1);
                  // writeln(" \\override Voice.Beam #'color = " + rgbColor(myColor));
                  // }
                  write("    ");
                  resetColor = ticks + torrent.ticks();
                }
                if (lilypondParam.index)
                  myIndex = "-" + (attributes.getIndex() + 1);
              }
            }
            tupletCacher.handle(xmlNote.xmlSize);
            // ---
            // lilypond ignores ornaments over rests (here it's not our job to check)
            xmlNote.ornament = myVoice.shake.get(ticks);
            List<Dynamic> press = new LinkedList<>(myVoice.press.subMap(ticks, ticks + xmlNote.xmlSize.duration).values());
            if (!press.isEmpty())
              xmlNote.dynamics = press.get(press.size() - 1);
            // ---
            if (lyricsSwitchStaff) {
              if (myVoice.lyric.containsKey(ticks)) {
                boolean staffTop = staff_count == 0 && 0 < staff_voice;
                boolean staffBot = staff_count > 0;
                if (staffTop || staffBot) {
                  String myVal = myVoice.lyric.get(ticks);
                  if (myVal.equals("2")) {
                    writeln("\\change Staff = \"s1\" \\stemUp");
                  }
                  if (myVal.equals("1")) {
                    writeln("\\change Staff = \"s0\" \\stemDown");
                  }
                }
              }
            }
            // ---
            String myString = xmlNote.toLyString();
            if (singleRest && (0 < count || myPartial.isEmpty())) {
              if (staff_voices <= 1)
                myString = "R" + myString.substring(1); // visible rest for entire measure
              else
                myString = "s" + myString.substring(1); // invisible rest TODO TPF make criteria more sophisticated
            }
            //
            write(myString + myIndex + " ");
            myIndex = "";
            // ---
            ticks += xmlNote.xmlSize.duration;
            // ---
            if (Objects.nonNull(resetColor) && resetColor == ticks) {
              // writeln("\n \\revert Voice.Beam #'color");
              writeln("    \\revert Voice.Stem #'color");
              // write("\\override NoteHead #'color = #black\n"); // previously
              writeln("    \\revert Voice.NoteHead #'color");
              write("    ");
              resetColor = null;
            }
          }
          tupletCacher.ground();
          ++count;
        }
        writeln("\\bar \"|.\"");
        writeln("    }");
        ++staff_voice;
        if (staff_voice < myStaffOrd.size()) // TODO TPF replace by staff_voices
          writeln("  \\\\ % next voice in same staff");
      }
      writeln("  >>");
      writeln("  }");
      // ---
      // myStaffRaw
      if (0 < staff_voices) {
        int index = staff_voices - 1;
        Voice myVoice = myStaffRaw.get(index);
        if (!myVoice.fbass.isEmpty()) {
          // BEGIN: figured bass
          final NavigableMap<Integer, FiguredBass> fbass = myVoice.fbass;
          writeln("  \\new FiguredBass {");
          writeln("    \\figuremode {");
          write(myPartial);
          // if desired more interception points can be inserted (for instance using ScoreJoint)
          insertMap(entry, measureChop.fromIntercepts((NavigableSet<Integer>) fbass.keySet(), entry, total), new LilypondMap() {
            @Override
            public void intercept(int ticks, XmlNote xmlNote) {
              Note note = myVoice.getNote(ticks, false);
              String duration = xmlNote.toLyString().substring(1); // substring(1) because of rest prefix r/s/R
              FiguredBass figuredBass = fbass.get(ticks);
              if (figuredBass == null || figuredBass.isDefault())
                write("s" + duration); // rest in between chords
              else {
                if (note == null) {
                  write("<" + figuredBass.toStringReversed() + ">" + duration);
                } else {
                  FiguredTones myFiguredTones = new FiguredTones(score.keySignature, note.tone());
                  write("<" + figuredBass.figuresReversed().stream() //
                      .map(myFiguredTones::getLilypondString) //
                      .collect(Collectors.joining(" ")) + ">" + duration);
                }
              }
            }
          });
          writeln("    }");
          writeln("  }");
        }
        // END: chord names
        // ---
      }
      // ---
      if (lilypondParam.lyric && !lyricsAboveStaff)
        for (Voice myVoice : myStaffRaw)
          insertLyrics(myVoice.lyric, myPartial, entry, total, measureChop);
      // ---
      ++staff_count;
    }
    // ---
    if (lilypondParam.lyric) // also used for TEXT
      insertLyrics(score.text, myPartial, entry, total, measureChop);
    // ---
    writeln(" >>"); // end GrandStaff
    writeln(">>");
    // ---
    writeln("  \\layout {");
    lilypondLayout.layout().forEach(s -> writeln("   " + s));
    writeln("  }");
    // ---
    writeln("}");
  }

  private void insertLyrics(NavigableMap<Integer, String> navigableMap, String partial, int entry, int total, MeasureChop measureChop) {
    LyricsFormatter myLyricsFormatter = new LyricsFormatter(navigableMap, false);
    for (int pass : IntRange.positive(myLyricsFormatter.max_pass)) {
      writeln("  \\new Lyrics {");
      writeln("    \\lyricmode {");
      // absolute size: \teeny, \tiny, \small, \normalsize, \large, \huge
      // relative changes: \smaller, \larger
      writeln("    \\tiny");
      write(partial);
      // ---
      NavigableMap<Integer, String> textPass = myLyricsFormatter.getMap(pass);
      insertMap(entry, measureChop.fromIntercepts((NavigableSet<Integer>) textPass.keySet(), entry, total), new LilypondMap() {
        @Override
        public void intercept(int ticks, XmlNote myXmlNote) {
          /** \skip should be used in \lyricmode (see "Invisible rests" p.54) */
          write(textPass.containsKey(ticks) ? LilypondConstants.asMarkup(false, "", textPass.get(ticks)) : "\\skip ");
          /** substring(1) because of rest prefix r/s/R */
          write(myXmlNote.toLyString().substring(1));
        }
      });
      writeln("    }");
      writeln("  }");
    }
  }

  private TupletCacher createTupletCacher() { // private because write() is called
    return new TupletCacher() {
      @Override
      public void writeTuplet(String string) {
        write(string);
      }
    };
  }

  private void insertMap(int entry, Collection<List<XmlNote>> collection, LilypondMap lilypondMap) {
    int ticks = entry;
    int count = 0;
    TupletCacher tupletCacher = createTupletCacher();
    for (List<XmlNote> list : collection) {
      barCheck(count);
      write("    ");
      for (XmlNote xmlNote : list) {
        tupletCacher.handle(xmlNote.xmlSize);
        lilypondMap.intercept(ticks, xmlNote);
        write(" ");
        ticks += xmlNote.xmlSize.duration;
      }
      tupletCacher.ground();
      ++count;
    }
    writeln("");
  }

  private static String staffId(int staff) {
    return "\"s" + staff + "\"";
  }

  /** <i>"Though not strictly necessary, bar checks should be used in the input code to show where bar lines are expected to fall. They are entered using the
   * bar symbol, |."</i>
   * 
   * @param count of measures
   * @throws Exception */
  private void barCheck(int count) {
    if (0 < count)
      writeln("| % m" + count);
  }

  private void write(String string) {
    stringBuilder.append(string);
  }

  private void writeln(String string) {
    stringBuilder.append(string);
    stringBuilder.append('\n');
  }
}
