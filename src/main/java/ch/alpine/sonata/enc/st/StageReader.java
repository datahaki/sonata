// code by jph
package ch.alpine.sonata.enc.st;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import ch.alpine.sonata.Divisions;
import ch.alpine.sonata.KeySignature;
import ch.alpine.sonata.enc.Analysis;
import ch.alpine.sonata.enc.api.DiatonicPrecision;
import ch.alpine.sonata.enc.api.ImportScoreFormat;
import ch.alpine.sonata.enc.api.KeyModeDatabaseRequest;
import ch.alpine.sonata.enc.api.TempoDatabaseRequest;
import ch.alpine.sonata.scr.Score;
import ch.alpine.sonata.utl.Stylist;
import ch.alpine.sonata.utl.TimescalePacker;
import ch.alpine.sonata.utl.Timeshift;
import sys.dat.UniqueValue;
import sys.mat.IntegerMath;

/** provides common functionality for {@link Stage1Reader} and {@link Stage2Reader} */
public abstract class StageReader implements ImportScoreFormat, //
    KeyModeDatabaseRequest, TempoDatabaseRequest, DiatonicPrecision {
  protected abstract StageHeader getHeader(File myFile) throws Exception;

  protected abstract void getContent(Score score, SortedMap<File, StageHeader> sortedMap) throws Exception;

  @Override
  public final Score get(Path directory) throws Exception {
    Score score = new Score();
    score.isKeyModeValid = false;
    SortedSet<Integer> setType = new TreeSet<>();
    Set<Integer> setMeasure = new HashSet<>();
    Set<Integer> setQuarter = new HashSet<>();
    UniqueValue<String> uniqueMeter = UniqueValue.empty();
    SortedMap<File, StageHeader> headers = new TreeMap<>();
    // ---
    for (File myFile : new TreeSet<>(List.of(directory.toFile().listFiles((_, myString) -> myString.matches("\\d\\d"))))) {
      StageHeader stageHeader = getHeader(myFile);
      score.comment = stageHeader.comment;
      setType.addAll(stageHeader.type);
      setQuarter.add(stageHeader.quarter.orElseThrow());
      uniqueMeter.set(stageHeader.meter.orElseThrow());
      setMeasure.add(stageHeader.getMeasure());
      headers.put(myFile, stageHeader);
    }
    if (setType.size() != 1)
      System.out.println(directory.getFileName() + " tonics " + setType);
    score.keySignature = KeySignature.fromType(setType.first());
    int measure = IntegerMath.lcm(setMeasure); // this ... is ... Stanford!
    score.quarter = IntegerMath.lcm(setQuarter);
    score.division = Divisions.best(score.quarter, measure, uniqueMeter.orElseThrow());
    // ---
    getContent(score, headers);
    new TimescalePacker(score, true).pack();
    Timeshift.advanceEntry(score);
    Stylist.defaultStyle(score);
    Stylist.reasonableMeter(score);
    Analysis.assertEntryOnTicks(score);
    return score;
  }
}
