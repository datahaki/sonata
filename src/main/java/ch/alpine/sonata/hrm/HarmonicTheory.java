// code by jph
package ch.alpine.sonata.hrm;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import ch.alpine.sonata.KeyMode;
import ch.alpine.sonata.KeySignature;
import ch.alpine.sonata.Natur;
import ch.alpine.sonata.tri.Chord;
import ch.alpine.sonata.tri.SheafFormat;
import ch.alpine.sonata.tri.Triad;

// this has to be data driven! call AdaptedTheory!?
@Deprecated
class HarmonicTheory implements AbstractTheory { // this class is not used, but it's like ChromaticTheory
  static final int[][] ionian = { //
      // ---
      { 99, 0, 17, 0, 83, 16, 2, 77, 0, 7, 1, 6, }, // I (5458)
      { 99, 0, 48, 93, 5, 19, 19, 83, 4, 14, 2, 13, }, // i (122)
      { 84, 2, 14, 98, 18, 2, 94, 28, 4, 24, 4, 24, }, // Io (60)
      { 87, 0, 26, 1, 18, 17, 16, 70, 0, 99, 3, 32, }, // I6 (947)
      { 22, 0, 75, 0, 11, 8, 2, 99, 0, 13, 0, 80, }, // V (4096)
      { 17, 5, 75, 0, 12, 11, 2, 99, 0, 31, 79, 1, }, // v (427)
      { 15, 96, 29, 8, 22, 12, 5, 98, 3, 39, 72, 12, }, // Vo (63)
      { 30, 8, 83, 0, 99, 28, 7, 77, 0, 21, 2, 16, }, // V6 (715)
      { 16, 0, 99, 0, 10, 2, 73, 39, 1, 78, 0, 19, }, // II (1057)
      { 10, 1, 99, 0, 33, 92, 0, 27, 2, 76, 2, 11, }, // ii (2300)
      { 13, 2, 99, 4, 40, 70, 2, 4, 65, 15, 0, 8, }, // IIo (103)
      { 41, 1, 87, 0, 26, 14, 7, 20, 9, 71, 0, 99, }, // II6 (588)
      { 3, 70, 34, 2, 77, 18, 4, 16, 0, 99, 2, 7, }, // VI (483)
      { 92, 0, 23, 1, 77, 9, 5, 11, 1, 99, 1, 23, }, // vi (3003)
      { 93, 0, 24, 74, 22, 10, 6, 17, 1, 99, 13, 25, }, // VIo (97)
      { 26, 1, 25, 7, 75, 4, 98, 32, 8, 99, 0, 29, }, // VI6 (176)
      { 20, 1, 13, 0, 99, 3, 4, 3, 71, 40, 0, 77, }, // III (599)
      { 21, 1, 14, 1, 99, 20, 9, 94, 0, 22, 0, 77, }, // iii (1564)
      { 23, 3, 10, 0, 88, 46, 1, 99, 0, 49, 74, 5, }, // IIIo (426)
      { 0, 78, 21, 26, 69, 4, 21, 13, 0, 26, 8, 95, }, // III6 (17)
      { 4, 7, 1, 74, 63, 0, 85, 21, 3, 18, 0, 99, }, // VII (180)
      { 39, 2, 96, 0, 24, 1, 76, 30, 5, 25, 0, 99, }, // vii (407)
      { 41, 0, 99, 0, 42, 71, 0, 22, 2, 11, 0, 90, }, // VIIo (1560)
      { 0, 55, 11, 0, 33, 22, 88, 22, 55, 22, 11, 66, }, // VII6 (5)
      { 0, 77, 0, 11, 22, 0, 88, 22, 0, 0, 66, 33, }, // SH (8)
      { 9, 70, 27, 15, 15, 0, 97, 20, 4, 72, 0, 29, }, // sh (52)
      { 85, 1, 28, 2, 8, 3, 91, 50, 1, 99, 2, 45, }, // SHo (754)
      { 0, 0, 0, 50, 0, 0, 50, 0, 0, 0, 0, 0, }, // SH6 (1)
      { 0, 72, 0, 0, 0, 90, 0, 0, 45, 0, 0, 0, }, // SP (6)
      { 0, 92, 30, 0, 53, 0, 0, 7, 46, 15, 0, 0, }, // sp (11)
      { 7, 79, 63, 1, 99, 44, 12, 80, 1, 30, 4, 7, }, // SPo (320)
      { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, }, // SP6 (0)
      { 94, 0, 15, 78, 0, 0, 5, 0, 94, 0, 0, 5, }, // SQ (19)
      { 36, 9, 27, 36, 27, 0, 9, 0, 72, 54, 0, 90, }, // sq (10)
      { 53, 2, 79, 1, 33, 3, 2, 5, 74, 57, 2, 99, }, // SQo (359)
      { 0, 0, 0, 85, 0, 42, 0, 0, 28, 0, 0, 0, }, // SQ6 (7)
      { 19, 0, 19, 88, 3, 0, 0, 96, 3, 11, 76, 0, }, // ST (26)
      { 16, 0, 33, 33, 33, 0, 83, 50, 16, 50, 33, 16, }, // st (4)
      { 4, 2, 4, 79, 56, 2, 99, 59, 3, 88, 2, 40, }, // STo (138)
      { 87, 0, 0, 50, 0, 0, 0, 0, 0, 0, 75, 0, }, // ST6 (9)
      { 20, 2, 99, 0, 27, 90, 0, 20, 0, 20, 93, 3, }, // SD (299)
      { 0, 18, 0, 0, 18, 90, 0, 27, 0, 27, 36, 0, }, // sd (8)
      { 6, 81, 34, 0, 86, 4, 13, 20, 0, 22, 97, 6, }, // SDo (54)
      { 20, 2, 19, 0, 34, 86, 0, 99, 0, 43, 64, 4, }, // SD6 (164)
      { 76, 0, 13, 0, 12, 99, 0, 22, 0, 89, 4, 15, }, // IV (2360)
      { 94, 1, 12, 0, 8, 98, 0, 42, 96, 12, 3, 21, }, // iv (68)
      { 6, 0, 14, 1, 29, 72, 0, 3, 75, 16, 0, 98, }, // IVo (71)
      { 69, 0, 99, 0, 37, 84, 0, 26, 0, 13, 2, 25, }, // IV6 (934)
      { 4, 0, 77, 2, 8, 80, 0, 1, 98, 3, 0, 98, }, // IVo7 (103)
      { 73, 1, 13, 86, 17, 2, 98, 17, 2, 94, 0, 17, }, // Io7 (64)
      { 1, 98, 12, 0, 93, 0, 4, 69, 0, 6, 80, 1, }, // Vo7 (71)
      { 6, 74, 39, 0, 37, 99, 3, 31, 3, 92, 6, 12, }, // IV+ (111)
      { 99, 0, 38, 0, 90, 10, 6, 7, 61, 41, 0, 29, }, // I+ (178)
      { 14, 1, 10, 62, 21, 10, 17, 98, 1, 17, 0, 89, }, // V+ (73)
      { 16, 33, 95, 0, 33, 8, 91, 16, 0, 33, 75, 16, }, // II+ (27)
  };
  static final String[] ionian_sheaf = { //
      // ---
      "I", "i", "Io", "I6", "V", "v", "Vo", "V6", "II", "ii", "IIo", "II6", "VI", "vi", "VIo", "VI6", //
      "III", "iii", "IIIo", "III6", "VII", "vii", "VIIo", "VII6", "SH", "sh", "SHo", "SH6", "SP", "sp", //
      "SPo", "SP6", "SQ", "sq", "SQo", "SQ6", "ST", "st", "STo", "ST6", "SD", "sd", "SDo", "SD6", "IV", "iv", "IVo", "IV6", "IVo7", "Io7", "Vo7", "IV+", "I+",
      "V+", "II+", };
  // ---
  final KeySignature keySignature;
  final KeyMode keyMode;
  SheafFormat sheafFormat;
  List<String> labels = null;

  HarmonicTheory(KeySignature keySignature, KeyMode keyMode) {
    this.keySignature = keySignature;
    this.keyMode = keyMode;
    sheafFormat = new SheafFormat(keySignature, keyMode);
    switch (keyMode) {
    case IONIAN:
      labels = Arrays.asList(ionian_sheaf);
      break;
    default:
      break;
    }
  }

  @Override
  public Chord major(Natur pitch) {
    Triad triad = Triad.major(pitch);
    int index = labels.indexOf(sheafFormat.format(triad));
    Collection<Natur> myCollection = EnumSet.noneOf(Natur.class);
    Collection<Natur> myTolerance = EnumSet.noneOf(Natur.class); // not used
    for (Natur myNatur : Natur.values()) {
      int ord = myNatur.ordinal();
      int prc = ionian[index][ord];
      if (20 < prc)
        myCollection.add(myNatur);
      if (2 < prc)
        myTolerance.add(myNatur);
    }
    return new Chord(triad, myCollection);
  }

  @Override
  public Chord minor(Natur pitch) {
    return null;
  }

  @Override
  public Chord augmented(Natur pitch) {
    return null;
  }

  @Override
  public Chord diminished(Natur pitch) {
    return null;
  }

  @Override
  public Chord majorSixth(Natur pitch) {
    return null;
  }

  @Override
  public Chord diminishedSeventh(Natur pitch) {
    return null;
  }
}
