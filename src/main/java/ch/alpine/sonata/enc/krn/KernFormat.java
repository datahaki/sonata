// code by jph
package ch.alpine.sonata.enc.krn;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;

import ch.alpine.bridge.lang.SI;
import ch.alpine.sonata.Divisions;
import ch.alpine.sonata.KeySignature;
import ch.alpine.sonata.Voice;
import ch.alpine.sonata.enc.api.DiatonicPrecision;
import ch.alpine.sonata.enc.api.ImportScoreFormat;
import ch.alpine.sonata.enc.api.KeyModeDatabaseRequest;
import ch.alpine.sonata.enc.api.TempoDatabaseRequest;
import ch.alpine.sonata.enc.md.MusedataMeta;
import ch.alpine.sonata.io.KernNoteFormat;
import ch.alpine.sonata.scr.Score;
import ch.alpine.sonata.scr.ScoreBuffer;
import ch.alpine.sonata.scr.UniformFlatten;
import ch.alpine.sonata.utl.Duplicates;
import ch.alpine.sonata.utl.Shuffler;
import ch.alpine.sonata.utl.Stylist;
import ch.alpine.sonata.utl.TimescalePacker;
import ch.alpine.sonata.utl.Timeshift;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Scalars;
import sys.dat.HtmlConversion;
import sys.dat.UniqueValue;
import sys.mat.IntRange;
import sys.mat.IntegerMath;

class Chunk {
  int[] myInt; // max number of spaces + 1
  List<List<String>> list = new ArrayList<>();

  public Chunk(int tabs) {
    myInt = new int[tabs];
    for (@SuppressWarnings("unused")
    int __ : IntRange.positive(tabs))
      list.add(new LinkedList<>());
  }

  public void max(int col, int length) {
    myInt[col] = Math.max(myInt[col], length);
  }

  @Override
  public String toString() {
    List<Integer> list = new LinkedList<>();
    for (int c0 : IntRange.positive(myInt.length))
      list.add(myInt[c0]);
    return list.toString();
  }
}

/** currently only scores with constant tab numbers are supported
 * 
 * "* *^ *" means the middle tab splits into two, and "* *v *v *" means the two center staffs merge into one
 * 
 * additional command is
 * !!! division, for instance as
 * !!!division 3 2 2 3 in Vivaldi_Opus06.vivaldi0602-3, Vivaldi_Opus08.op8n10c */
public class KernFormat implements ImportScoreFormat, //
    KeyModeDatabaseRequest, TempoDatabaseRequest, DiatonicPrecision {
  @Override
  public Score get(Path file) throws Exception {
    Score score = new Score();
    score.isKeyModeValid = false;
    String altComment = "";
    // ---
    List<Integer> rmDuplicates = new LinkedList<>();
    Set<Integer> lcmSet = new HashSet<>();
    final UniqueValue<Integer> type = UniqueValue.empty();
    Set<Float> bpms = new LinkedHashSet<>();
    final UniqueValue<String> ratio = UniqueValue.empty();
    // ---
    List<Chunk> chunks = new ArrayList<>();
    {
      Chunk chunk = null;
      try (BufferedReader bufferedReader = Files.newBufferedReader(file)) {
        String myString;
        int tabs = 0;
        while (Objects.nonNull(myString = bufferedReader.readLine()))
          if (myString.startsWith("!!!")) { // properties
            if (myString.startsWith("!!!OTL")) { // title
              int beg = myString.indexOf(':');
              if (0 < beg)
                score.title = HtmlConversion.removeHtml(myString.substring(beg + 2));
            }
            if (myString.startsWith("!!!division")) // ??? by now this is obsolete
              System.out.println(file + " obsolete !!!division");
            if (myString.startsWith("!!!removeDuplicates ")) {
              StringTokenizer myStringTokenizer = new StringTokenizer(myString);
              myStringTokenizer.nextToken();
              while (myStringTokenizer.hasMoreTokens())
                rmDuplicates.add(Integer.parseInt(myStringTokenizer.nextToken()));
            }
            if (myString.startsWith("!!!SCT") || myString.startsWith("!!!SCA") || myString.startsWith("!!!ONM")) { // catalog
              int beg = myString.indexOf(':');
              if (0 < beg)
                score.comment += (score.comment.isEmpty() ? "" : ", ") + HtmlConversion.removeHtml(myString.substring(beg + 2));
            }
            if (myString.startsWith("!!!COM") || myString.startsWith("!!!ONB")) {
              int beg = myString.indexOf(':');
              if (0 < beg)
                altComment += (altComment.isEmpty() ? "" : ", ") + HtmlConversion.removeHtml(myString.substring(beg + 2));
            }
          } else if (myString.startsWith("!!")) {
            // a comment
          } else if (myString.startsWith("/CUTOFF")) { // manual inserted cut
            score.process.append("/cut");
            break;
          } else {
            String[] mySplit = myString.split("\\t");
            if (tabs != mySplit.length) {
              tabs = mySplit.length;
              chunk = new Chunk(tabs);
              chunks.add(chunk);
            }
            int col = 0;
            for (String tab : mySplit) {
              if (tab.matches("\\*k\\[.*\\]")) { // key is encoded for instance as f#c#, or b-e-a-
                int key = 0;
                for (char myChar : tab.toCharArray()) {
                  key += myChar == '#' ? 1 : 0;
                  key -= myChar == '-' ? 1 : 0;
                }
                type.set(key);
              } else if (tab.matches("\\*M\\d+/\\d+")) { // measure
                ratio.set(tab.substring(2));
              } else if (tab.matches("\\*MM\\d+")) { // tempo
                bpms.add(Float.parseFloat(tab.substring(3)));
              } else if (Objects.nonNull(chunk)) {
                if (!tab.equals("."))
                  chunk.list.get(col).add(tab);
                String[] splitSpace = tab.split(" ");
                chunk.max(col, splitSpace.length);
                for (String space : splitSpace)
                  if (KernNoteFormat.containsNote(space) || KernNoteFormat.containsRest(space)) {
                    Matcher matcher = KernNoteFormat.PATTERN_DIGIT.matcher(space);
                    matcher.find();
                    int value = Integer.parseInt(matcher.group());
                    if (value != 0)
                      lcmSet.add(value);
                  }
              }
              ++col;
            }
          }
      }
    }
    // ---
    if (score.comment.isEmpty())
      score.comment = altComment;
    // ---
    final int basis = IntegerMath.lcm(lcmSet);
    final Scalar meter = Scalars.fromString(ratio.orElseThrow());
    int measure = Scalars.intValueExact(meter.multiply(RealScalar.of(basis)));
    if (1 < chunks.size())
      score.process.append("/chunks=" + chunks.size());
    int ticks_offset = 0;
    UniqueValue<Integer> entry = UniqueValue.empty();
    int permuted = 0;
    for (Chunk chunk : chunks) {
      ScoreBuffer scoreBuffer = new ScoreBuffer();
      UniqueValue<Integer> ticks = UniqueValue.empty();
      int count = 0;
      int voice = 0;
      for (List<String> myCol : chunk.list) {
        KernColumn kernColumn = new KernColumn(myCol, basis, measure, voice, scoreBuffer);
        for (int myInt : kernColumn.sortedSet)
          try {
            entry.set((ticks_offset + myInt) % measure);
          } catch (Exception exception) {
            // this is usually not severe
            new RuntimeException("measures are irregular intervals " + measure + ": " + exception.getMessage()).printStackTrace();
          }
        if (0 < kernColumn.ticks)
          ticks.set(kernColumn.ticks);
        voice += chunk.myInt[count];
        ++count;
      }
      if (ticks.isPresent()) {
        Score scoreTemp = new Score();
        scoreTemp.voices.addAll(scoreBuffer.getVoices().reversed());
        boolean myBoolean = Shuffler.arrange(scoreTemp);
        if (myBoolean) {
          // myScore.process.append("/p");
          ++permuted;
        }
        Collections.reverse(scoreTemp.voices);
        // ---
        voice = 0;
        for (Voice myVoice : scoreTemp.voices) {
          Timeshift.by(myVoice, ticks_offset);
          if (score.voices() <= voice)
            score.voices.add(new Voice());
          score.voices.get(voice).navigableMap.putAll(myVoice.navigableMap);
          ++voice;
        }
        ticks_offset += ticks.orElseThrow();
      }
    }
    Collections.reverse(score.voices);
    if (0 < permuted)
      score.process.append("/perms=" + permuted);
    // ---
    if (0 < entry.orElseThrow())
      Timeshift.by(score, measure - entry.orElseThrow());
    score.bpm = 1 == bpms.size() //
        ? SI.PER_MINUTE.quantity(bpms.iterator().next())
        : null;
    if (type.isPresent())
      score.keySignature = KeySignature.fromType(type.orElseThrow());
    else
      new RuntimeException("warning: key undefined").printStackTrace();
    // ---
    score.quarter = Scalars.intValueExact(RationalScalar.of(measure, 4).divide(meter));
    score.division = Divisions.best(score.quarter, measure, MusedataMeta.getSafeRatio(ratio.orElseThrow()));
    // ---
    int count = 0;
    for (int voice : rmDuplicates)
      count += Duplicates.removeDuplicateNotes(score, voice);
    if (0 < count) {
      score.process.append("/dropNotes=" + count);
      System.out.println(count);
    }
    Duplicates.removeEmptyVoices(score);
    // ---
    new TimescalePacker(score, true).pack();
    Stylist.defaultStyle(score);
    // ---
    for (Voice voice : score.voices)
      UniformFlatten.applyTo(voice); // for some reason some of the above ops make the voices scattered
    return score;
  }
}
