// code by jph
package ch.alpine.sonata.enc.st;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import ch.alpine.sonata.Figure;
import ch.alpine.sonata.FiguredBass;
import ch.alpine.sonata.Note;
import ch.alpine.sonata.ScoreEntry;
import ch.alpine.sonata.Suffix;
import ch.alpine.sonata.enc.md.MusedataMeta;
import ch.alpine.sonata.io.MusedataNoteFormat;
import ch.alpine.sonata.scr.Score;
import ch.alpine.sonata.scr.ScoreBuffer;
import ch.alpine.sonata.utl.Timeshift;
import sys.mat.TokenStream;

/** only Stanford format that encodes figured bass */
// ??? in reject haendel clori/triosonata -> requires that Q:8 Q:2 Q:4 multiple quarters
public class Stage2Reader extends StageReader {
  @Override
  protected StageHeader getHeader(File file) throws Exception {
    StageHeader stageHeader = new StageHeader();
    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
      String string = null;
      int header_count = 0;
      while (Objects.nonNull(string = bufferedReader.readLine()))
        if (string.startsWith("$"))
          if (0 < string.indexOf(" K:") || 0 < string.indexOf(" Q:") || 0 < string.indexOf("T:")) {
            ++header_count;
            try {
              StringTokenizer stringTokenizer = new StringTokenizer(string);
              while (stringTokenizer.hasMoreTokens()) {
                final String token = stringTokenizer.nextToken();
                switch (token.charAt(0)) {
                case 'K': {
                  int myInt = Integer.parseInt(token.substring(2));
                  stageHeader.type.add(myInt);
                  break;
                }
                case 'Q':
                  stageHeader.quarter.set(Integer.parseInt(token.substring(2)));
                  break;
                case 'T':
                  stageHeader.meter.set(MusedataMeta.getSafeRatio(token.substring(2)));
                  break;
                case 'D':
                  // D:A tempo di gavotta, that means, one should read until end of line!
                  // molto something, a 4., etc., also during piece
                  break;
                case 'd':
                  System.out.println(file + " obsolete: " + token);
                  break;
                }
              }
            } catch (Exception exception) {
              System.out.println(file);
              if (1 < header_count) {
                System.out.println("multiple headers in one file " + header_count);
                System.out.println("previous quarter= " + stageHeader.quarter.orElseThrow() + "   meter= " + stageHeader.meter.orElseThrow());
                System.out.println(string);
              }
              throw exception;
            }
          }
    }
    return stageHeader;
  }

  @Override
  protected void getContent(Score score, SortedMap<File, StageHeader> sortedMap) throws Exception {
    ScoreBuffer scoreBuffer = new ScoreBuffer();
    int voice = 0;
    int ticks_next = 0;
    SortedSet<Integer> entries = new TreeSet<>();
    SortedSet<Integer> firstMeasure = new TreeSet<>();
    for (Entry<File, StageHeader> entry : sortedMap.entrySet()) {
      File file = entry.getKey();
      StageHeader stageHeader = entry.getValue();
      try (BufferedReader myBufferedReader = new BufferedReader(new FileReader(file))) {
        int voice_offset = 0;
        int voice_offset_max = 0;
        int offset = -1;
        int ticks = 0;
        int line_count = 0;
        final int measure = stageHeader.getMeasure();
        boolean myComment = false;
        boolean myLaunched = false;
        boolean entries_fuse = true;
        String string;
        int ticks_sink = -1;
        boolean print_flag = true;
        Integer fbass_forward = null;
        while (Objects.nonNull(string = myBufferedReader.readLine())) {
          if (string.startsWith("/FINE") || string.startsWith("/END"))
            break;
          if (string.startsWith("/CUTOFF")) {
            score.process.append("/cut");
            break;
          }
          myComment ^= string.startsWith("&"); // & or &1
          if (myLaunched) { // has parsed header line
            // if (myString.startsWith("$"))
            // if (0 < myString.indexOf(" K:") || 0 < myString.indexOf(" Q:") || 0 < myString.indexOf("T:"))
            // System.out.println(myString); // the definition of key and quarter should not occur after init
            if (!myComment) { // between & ... &, or &1 ... &1
              boolean valid = false;
              if (string.startsWith("back")) {
                // in Stage 2 "back" does not need to point back to the measure beginning
                valid = true;
                ticks_sink = ticks;
                // System.out.println("back " + getTicks(myString) + " at " + ticks_sink + " v=" + voice_offset);
                ticks -= MusedataMeta.getTicks(string);
                ++voice_offset;
                // voice_offset_max = Math.max(voice_offset_max, voice_offset);
              }
              if (string.startsWith("rest") || string.startsWith("irest") || string.startsWith("irst")) {
                valid = true;
                ticks += MusedataMeta.getTicks(string);
              }
              if (string.startsWith("measure")) {
                if (offset < 0) {
                  firstMeasure.add(Integer.parseInt(TokenStream.of(string).skip(1).findFirst().get()));
                  offset = ticks * score.measure() / measure;
                }
                entries.add((ticks * score.measure() / measure) % score.measure());
                if (entries_fuse && 1 < entries.size()) {
                  System.out.println("entries " + file + " line " + line_count + " " + string + " " + entries);
                  entries_fuse = false;
                }
              }
              byte myByte = (byte) string.charAt(0);
              {
                if (myByte == 'c') {
                  throw new RuntimeException(" line " + line_count + " character c encountered");
                }
                // ---
                if (myByte == 'f') { // figured bass bwv0006_05
                  int token_count = Integer.parseInt(string.substring(1, 2));
                  if (16 < string.length()) {
                    final int ticks_now = (ticks + (fbass_forward == null ? 0 : fbass_forward)) * score.measure() / measure;
                    // System.out.println(myString);
                    final String myFigures = string.substring(16);
                    FiguredBass myFiguredBass = new FiguredBass();
                    StringTokenizer myStringTokenizer = new StringTokenizer(myFigures);
                    while (myStringTokenizer.hasMoreTokens()) {
                      final String myToken = myStringTokenizer.nextToken();
                      if (myToken.equals("b")) {
                        // blank, i.e. no figure
                      } else //
                      if (Figure.PATTERN.matcher(myToken).matches()) // 9 4+
                        // standard expression
                        myFiguredBass.insert(Figure.fromString(myToken));
                      else //
                      if (myToken.matches("[#f]")) // #
                        // abbreviation for 3 with modifier
                        myFiguredBass.insert(new Figure(3, Suffix.fromAbbreviation(myToken.charAt(0))));
                      else //
                      if (myToken.equals("#9"))
                        // 9 with strike through cop212_01
                        myFiguredBass.insert(new Figure(9, Suffix.INCR));
                      else { //
                        // cannot be parsed
                        score.appendText(ticks_now, myToken, true);
                        System.out.println(String.format("%4d %4d |%s|", line_count, ticks, string));
                      }
                      --token_count;
                    }
                    ScoreEntry myScoreEntry = new ScoreEntry(ticks_now, voice + voice_offset);
                    scoreBuffer.fbass(myScoreEntry, myFiguredBass);
                  }
                  if (token_count != 0) {
                    System.out.println("figure count invalid: " + string);
                  }
                  {
                    final int length = string.length();
                    String myCont = string.substring(7, Math.min(10, length)).trim();
                    if (myCont.isEmpty())
                      fbass_forward = null;
                    else {
                      if (myCont.startsWith("@"))
                        myCont = myCont.substring(1);
                      fbass_forward = Integer.parseInt(myCont);
                    }
                  }
                  valid |= true;
                }
              }
              valid |= myByte == '*'; // Da capo, Dal Segno, * D con sordini
              valid |= myByte == 'P'; // Print Suggestion
              valid |= myByte == 'S'; // Sound
              valid |= myByte == '$'; // Musical Attributes: quarter, meter, clef
              valid |= myByte == 'g'; // Grace Note
              valid |= myByte == 'm'; // measure, mheavy, mheavy2, mdouble, ...
              valid |= myByte == '@'; //
              valid |= myByte == '&'; // comment !?
              if (!valid) {
                Note note = MusedataNoteFormat.INSTANCE.parseNote(string.substring(0, 8));
                if (string.startsWith(" ")) {
                  ticks -= ticks_next;
                  ++voice_offset;
                } else {
                  if (ticks_sink == -1)
                    voice_offset = 0;
                }
                note.setTicks(note.ticks() * (score.measure() / measure));
                // note.setTone(note.tone().transposeByOctaves(-4));
                final int ticks_now = ticks * score.measure() / measure;
                final ScoreEntry scoreEntry = new ScoreEntry(ticks_now, voice + voice_offset);
                try {
                  // if (0 < voice_offset)
                  // System.out.println(myNote);
                  scoreBuffer.put(note, string.charAt(8) == '-', ticks_now, voice + voice_offset);
                  // System.out.println(String.format("%4d %s", ticks_now, myNote.toString()));
                } catch (Exception exception) {
                  scoreBuffer.untie(voice + voice_offset);
                  scoreBuffer.put(note, string.charAt(8) == '-', ticks_now, voice + voice_offset);
                }
                try {
                  scoreBuffer.shake(scoreEntry, Stage2Ornaments.getFromLine(string));
                  scoreBuffer.press(scoreEntry, Stage2Dynamics.getFromLine(string));
                } catch (Exception exception) {
                  if (print_flag) {
                    System.out.println(file);
                    print_flag = false;
                  }
                  System.out.println(string + " [" + exception.getMessage() + "][len=" + string.length() + " / in=" + (line_count + 1) + "]");
                }
                try {
                  if (43 < string.length())
                    scoreBuffer.lyric(MusedataMeta.format(string.substring(43)), ticks_now, voice + voice_offset);
                  // ---
                  voice_offset_max = Math.max(voice_offset_max, voice_offset);
                  if (!string.startsWith(" "))
                    ticks_next = MusedataMeta.getTicks(string);
                  else if (MusedataMeta.getTicks(string) != ticks_next)
                    System.out.println("ticks of note in chord not unique");
                  ticks += ticks_next;
                } catch (Exception exception) {
                  if (print_flag) {
                    System.out.println(file);
                    print_flag = false;
                  }
                  System.out.println(String.format(" [%4d] +vo=%d %s %s", line_count, voice_offset, string, exception.getMessage()));
                  throw exception;
                }
              }
              if (ticks == ticks_sink) {
                // System.out.println(" \\-u " + ticks);
                ticks_sink = -1;
                --voice_offset;
              }
            } else {
              if (!string.startsWith("&")) { // myString holds comment
                final int ticks_now = (ticks - ticks_next) * score.measure() / measure;
                score.appendText(ticks_now, string, true);
              }
            }
          } else {
            if (string.startsWith("$")) {
              myLaunched |= true; // signals begin of music info
              int index = string.indexOf("D:");
              if (0 <= index) {
                final int ticks_now = ticks * score.measure() / measure;
                score.appendText(ticks_now, string.substring(index + 2).trim(), true);
              }
            }
            // ---
            MusedataMeta.handleLine(score, line_count, string);
          }
          ++line_count;
        }
        voice += 1 + voice_offset_max;
      }
    }
    score.voices.addAll(scoreBuffer.getVoices()); // add all voices
    if (entries.size() != 1)
      System.out.println("too many measures " + entries + " " + score.measure());
    else {
      if (entries.first() != 0)
        Timeshift.by(score, score.measure() - entries.first());
    }
  }
}
