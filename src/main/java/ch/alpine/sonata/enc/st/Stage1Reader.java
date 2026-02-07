// code by jph
package ch.alpine.sonata.enc.st;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.SortedMap;
import java.util.StringTokenizer;

import ch.alpine.sonata.Note;
import ch.alpine.sonata.io.MusedataNoteFormat;
import ch.alpine.sonata.scr.Score;
import ch.alpine.sonata.scr.ScoreBuffer;
import sys.mat.IntRange;

public class Stage1Reader extends StageReader {
  @Override
  protected final StageHeader getHeader(File file) throws Exception {
    StageHeader stageHeader = new StageHeader();
    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
      String header = bufferedReader.readLine().trim(); // total lines in file
      if (header.isEmpty())
        header = bufferedReader.readLine().trim(); // total lines in file
      // int total_lines =
      Integer.parseInt(header);
      {
        StringTokenizer stringTokenizer = new StringTokenizer(bufferedReader.readLine());
        stageHeader.comment = stringTokenizer.nextToken(); // ??? append more tokens
      }
      bufferedReader.readLine(); // Bach Gesells
      bufferedReader.readLine(); // chaft xliii/
      bufferedReader.readLine(); // empty or '1'
      {
        StringTokenizer stringTokenizer = new StringTokenizer(bufferedReader.readLine());
        Integer.parseInt(stringTokenizer.nextToken()); // total voices
        Integer.parseInt(stringTokenizer.nextToken()); // voice
      }
      {
        String string = bufferedReader.readLine();
        StringTokenizer stringTokenizer = new StringTokenizer(string);
        Integer.parseInt(stringTokenizer.nextToken()); // total measures
        stageHeader.type.add(Integer.parseInt(stringTokenizer.nextToken())); // KeySignature type
        stageHeader.measure_st1 = Integer.parseInt(stringTokenizer.nextToken()); // ticks per measure (redundant)
        stageHeader.quarter.set(Integer.parseInt(stringTokenizer.nextToken())); // quarter
        if (stringTokenizer.hasMoreTokens()) { // inserted manually into files, whenever divider != 0
          // for some mozart pieces these tokens still remain, since mozart is challenging...
          // System.out.println(myFile + " obsolete division in: " + myString);
        }
      }
      {
        StringTokenizer stringTokenizer = new StringTokenizer(bufferedReader.readLine());
        stageHeader.meter.set(stringTokenizer.nextToken() + "/" + stringTokenizer.nextToken());
        if (!stageHeader.isConsistent_st1())
          System.out.println(file + " inconsistent meter " + stageHeader.meter.orElseThrow());
      }
    }
    return stageHeader;
  }

  @Override
  protected void getContent(Score score, SortedMap<File, StageHeader> sortedMap) throws Exception {
    ScoreBuffer scoreBuffer = new ScoreBuffer();
    int voice = 0;
    int ticks_prev = 0;
    int ticks_next = 0;
    for (Entry<File, StageHeader> entry : sortedMap.entrySet()) {
      File myFile = entry.getKey();
      int ticks = 0;
      int voice_offset = 0;
      int voice_offset_max = 0;
      try (BufferedReader bufferedReader = new BufferedReader(new FileReader(myFile))) {
        String header = bufferedReader.readLine(); // total lines in file
        int skip = header.trim().isEmpty() ? 6 : 5;
        for (@SuppressWarnings("unused")
        int __ : IntRange.positive(skip))
          bufferedReader.readLine(); // bwv mvt
        StringTokenizer stringTokenizer = new StringTokenizer(bufferedReader.readLine());
        stringTokenizer.nextToken(); // total measures
        stringTokenizer.nextToken(); // key
        int measure = Integer.parseInt(stringTokenizer.nextToken());
        bufferedReader.readLine();
        // ---
        int count = skip + 3;
        String string;
        while (Objects.nonNull(string = bufferedReader.readLine())) {
          ++count;
          if (string.startsWith("END")) {
            if (ticks % measure != 0)
              System.out.println("stage1" + " : out of sync in voice " + voice);
            break;
          }
          boolean valid = false;
          if (string.startsWith("@")) // inserted to comment out lines
            valid = true;
          if (string.startsWith("measure")) {
            valid = true;
            if (ticks % measure != 0) {
              // partials are probably not encoded in Stage1Format
              throw new RuntimeException(String.format("bar line not %d %% %d != mod 0", ticks, measure));
            }
          }
          if (string.startsWith("dele")) // deleted, or dele #
            valid = true;
          if (string.startsWith("rest")) {
            ticks += getTicks(string);
            valid = true;
          }
          if (!valid) {
            try {
              Note note;
              if (string.startsWith(" ")) {
                note = new Note(MusedataNoteFormat.INSTANCE.parseTone(new StringTokenizer(string).nextToken()), ticks_prev);
                ticks -= ticks_next;
                ++voice_offset;
              } else {
                note = MusedataNoteFormat.INSTANCE.parseNote(string.substring(0, 7));
                ticks_prev = note.ticks();
                voice_offset = 0;
              }
              note.setTicks(note.ticks() * (score.measure() / measure));
              // note.setTone(note.tone().transposeByOctaves(-4));
              boolean tie = 7 < string.length() && string.charAt(7) == '-';
              try {
                scoreBuffer.put(note, tie, ticks * score.measure() / measure, voice + voice_offset);
              } catch (Exception exception) {
                scoreBuffer.untie(voice + voice_offset);
                scoreBuffer.put(note, tie, ticks * score.measure() / measure, voice + voice_offset);
                // try {
                // } catch (Exception m) {
                // System.out.println(myFile + " " + myString + " problem [" + count + "]:");
                // myException.printStackTrace();
                // break;
                // }
              }
              voice_offset_max = Math.max(voice_offset_max, voice_offset);
              if (!string.startsWith(" "))
                ticks_next = getTicks(string);
              ticks += ticks_next;
            } catch (Exception exception) {
              System.out.println(myFile);
              System.out.println("line " + count);
              System.out.println(string);
              throw exception;
            }
          }
        }
      }
      voice += 1 + voice_offset_max;
    }
    score.voices.addAll(scoreBuffer.getVoices());
    // myScore.title = getTitle(mySortedMap.firstKey().getParentFile()); // BAD
  }

  protected static int getTicks(String string) {
    return Integer.parseInt(string.substring(4, 7).trim()); // there are 3 digits ticks, e.g. '144', and char 7 might have '-'
  }
  // @Deprecated
  // private static final String getTitle(File myFile) {
  // String myString = myFile.getName();
  // Matcher myMatcher;
  // myMatcher = Pattern.compile("[a-zA-Z]+").matcher(myString);
  // boolean myBoolean = myMatcher.find();
  // if (myBoolean) {
  // String myChars = myMatcher.group().toLowerCase();
  // myMatcher = Pattern.compile("\\d+").matcher(myString);
  // myMatcher.find();
  // int myBeg = myMatcher.start();
  // String myAppendix = myString.substring(myBeg).replace('_', '.');
  // // ---
  // // if (myChars.equals("grosso"))
  // // return "Concerto Grosso " + myAppendix;
  // // if (myChars.equals("varioushwv"))
  // // return "Various " + myAppendix;
  // // if (myChars.equals("ariodant"))
  // // return "Ariodante " + myAppendix;
  // // if (myChars.equals("clori"))
  // // return "Clori, Tirsi e Fileno " + myAppendix;
  // // if (myChars.equals("trioson"))
  // // return "Trio Sonata " + myAppendix;
  // // ---
  // // if (myChars.equals("divertims"))
  // // return "Divertimento " + myAppendix;
  // // if (myChars.equals("squartet"))
  // // return "String Quartet " + myAppendix;
  // // if (myChars.equals("trio"))
  // // return "Trio " + myAppendix;
  // // if (myChars.equals("duo"))
  // // return "Duo " + myAppendix;
  // // ---
  // // if (myChars.equals("detable"))
  // // return "Musique de table, production " + myAppendix;
  // // if (myChars.equals("hgdienst"))
  // // return "Harmonischer Gottestdienst " + myAppendix;
  // // if (myChars.equals("vsonatas"))
  // // return "Solo Sonata for Violin " + myAppendix;
  // }
  // return "untitled";
  // }
}
