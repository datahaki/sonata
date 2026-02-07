// code by jph
package ch.alpine.sonata.tri;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import ch.alpine.sonata.Natur;
import ch.alpine.tensor.ext.Integers;

/** 55 triads (55 = 12*4 + 3 + 4)
 * a triad consists of 3, or 4 pitches
 * closed under chromatic transposition
 * 
 * toString() is modified to omit leading underscore character */
public enum Triad {
  _047(TriadType.MAJOR, 0), // c
  _037(TriadType.MINOR, 0), // c
  _036(TriadType.DIMINISHED, 0), // c
  _079(TriadType.MAJOR_SIXTH, 0), // c
  _7b2(TriadType.MAJOR, 7), // g
  _7a2(TriadType.MINOR, 7), // g
  _7a1(TriadType.DIMINISHED, 7), // g
  _724(TriadType.MAJOR_SIXTH, 7), // g
  _269(TriadType.MAJOR, 2), // d
  _259(TriadType.MINOR, 2), // d
  _258(TriadType.DIMINISHED, 2), // d
  _29b(TriadType.MAJOR_SIXTH, 2), // d
  _914(TriadType.MAJOR, 9), // a
  _904(TriadType.MINOR, 9), // a
  _903(TriadType.DIMINISHED, 9), // a
  _946(TriadType.MAJOR_SIXTH, 9), // a
  _48b(TriadType.MAJOR, 4), // e
  _47b(TriadType.MINOR, 4), // e
  _47a(TriadType.DIMINISHED, 4), // e
  _4b1(TriadType.MAJOR_SIXTH, 4), // e
  _b36(TriadType.MAJOR, 11), // b
  _b26(TriadType.MINOR, 11), // b
  _b25(TriadType.DIMINISHED, 11), // b
  _b68(TriadType.MAJOR_SIXTH, 11), // b
  _6a1(TriadType.MAJOR, 6), // f# g&
  _691(TriadType.MINOR, 6), // f# g&
  _690(TriadType.DIMINISHED, 6), // f# g&
  _613(TriadType.MAJOR_SIXTH, 6), // f# g&
  _158(TriadType.MAJOR, 1), // c# d&
  _148(TriadType.MINOR, 1), // c# d&
  _147(TriadType.DIMINISHED, 1), // c# d&
  _18a(TriadType.MAJOR_SIXTH, 1), // c# d&
  _803(TriadType.MAJOR, 8), // g# a&
  _8b3(TriadType.MINOR, 8), // g# a&
  _8b2(TriadType.DIMINISHED, 8), // g# a&
  _835(TriadType.MAJOR_SIXTH, 8), // g# a&
  _37a(TriadType.MAJOR, 3), // d# e&
  _36a(TriadType.MINOR, 3), // d# e&
  _369(TriadType.DIMINISHED, 3), // d# e&
  _3a0(TriadType.MAJOR_SIXTH, 3), // d# e&
  _a25(TriadType.MAJOR, 10), // a# b&
  _a15(TriadType.MINOR, 10), // a# b&
  _a14(TriadType.DIMINISHED, 10), // a# b&
  _a57(TriadType.MAJOR_SIXTH, 10), // a# b&
  _590(TriadType.MAJOR, 5), // f
  _580(TriadType.MINOR, 5), // f
  _58b(TriadType.DIMINISHED, 5), // f
  _502(TriadType.MAJOR_SIXTH, 5), // f
  _0369(TriadType.DIMINISHED_SEVENTH, 0), // c dim7
  _7a14(TriadType.DIMINISHED_SEVENTH, 1), //
  _58b2(TriadType.DIMINISHED_SEVENTH, 2), //
  _048(TriadType.AUGMENTED, 0), // c+
  _591(TriadType.AUGMENTED, 1), //
  _26a(TriadType.AUGMENTED, 2), //
  _7b3(TriadType.AUGMENTED, 3);

  /** for matching */
  private static final Map<Set<Natur>, Triad> MAP = new HashMap<>();
  static {
    for (Triad triad : values())
      MAP.put(triad.set, triad);
    Integers.requireEquals(Triad.values().length, MAP.size());
  }
  private static final List<Triad> pitchList_major = List.of(_047, _158, _269, _37a, _48b, _590, _6a1, _7b2, _803, _914, _a25, _b36);
  private static final List<Triad> pitchList_minor = List.of(_037, _148, _259, _36a, _47b, _580, _691, _7a2, _8b3, _904, _a15, _b26);
  private static final List<Triad> pitchList_diminished = List.of(_036, _147, _258, _369, _47a, _58b, _690, _7a1, _8b2, _903, _a14, _b25);
  private static final List<Triad> pitchList_majorSixth = List.of(_079, _18a, _29b, _3a0, _4b1, _502, _613, _724, _835, _946, _a57, _b68);
  private static final List<Triad> pitchList_diminishedSeventh = List.of(_0369, _7a14, _58b2);
  private static final List<Triad> pitchList_augmented = List.of(_048, _591, _26a, _7b3);

  public static Optional<Triad> getOptional(Collection<Natur> collection) {
    return Optional.ofNullable(MAP.get(EnumSet.copyOf(collection)));
  }

  private static final Map<String, Triad> stringMap = new HashMap<>();
  static {
    for (Triad triad : values())
      stringMap.put(triad.toString(), triad);
  }

  /** inverse function of toString()
   * 
   * @param string
   * @return may return null, if myString is not associated */
  public static Triad fromString(String string) {
    Triad triad = stringMap.get(string);
    return Objects.isNull(triad) //
        ? fromStringOld(string)
        : triad;
  }

  /** for compatibility with legacy string format
   * 
   * @param string
   * @return */
  private static Triad fromStringOld(String string) {
    Set<Natur> list = EnumSet.noneOf(Natur.class);
    for (char chr : string.toCharArray())
      list.add(Natur.fromPitch(Integer.parseInt("" + chr, 16)));
    return MAP.get(list);
  }

  public static List<Triad> transposed(List<Triad> list, int delta) {
    return list.stream().map(triad -> triad.transposed(delta)).toList();
  }

  public static List<Triad> all() {
    List<Triad> list = new ArrayList<>();
    for (Triad triad : values())
      list.add(triad);
    return list;
  }

  private static Triad getFrom(List<Triad> list, Natur pitch) {
    int natur = Math.floorMod(pitch.ordinal(), list.size());
    Triad triad = list.get(natur);
    // FIXME
    Integers.requireEquals(triad.natur.ordinal(), natur);
    return triad;
  }

  /** @param pitch
   * @return C-major when pitch == 0 */
  public static Triad major(Natur pitch) {
    return getFrom(pitchList_major, pitch);
  }

  /** @param pitch
   * @return C-minor when pitch == 0 */
  public static Triad minor(Natur pitch) {
    return getFrom(pitchList_minor, pitch);
  }

  /** @param pitch
   * @return C-diminished when pitch == 0 */
  public static Triad diminished(Natur pitch) {
    return getFrom(pitchList_diminished, pitch);
  }

  /** @param pitch
   * @return C-(major/minor)-sixth when pitch == 0 */
  public static Triad majorSixth(Natur pitch) {
    return getFrom(pitchList_majorSixth, pitch);
  }

  /** @param pitch
   * @return C-diminished-seventh when pitch == 0 */
  public static Triad diminishedSeventh(Natur pitch) {
    return getFrom(pitchList_diminishedSeventh, pitch);
  }

  /** @param pitch
   * @return C-augmented when pitch == 0 */
  public static Triad augmented(Natur pitch) {
    return getFrom(pitchList_augmented, pitch);
  }

  // ---
  /** unmodifiable */
  public final TriadType triadType;
  public final Natur natur;
  private final String private_toString = name().substring(1);
  private final Set<Natur> set;
  /** intuitive ordering of tones; unmodifiable */
  public final List<Natur> list;

  private Triad(TriadType triadType, int _natur) {
    this.triadType = triadType;
    this.natur = Natur.fromPitch(_natur);
    // "".chars().map(a->Integer.parseUnsignedInt(a, 16));
    List<Natur> _list = new ArrayList<>();
    for (char myChar : name().substring(1).toCharArray())
      _list.add(Natur.fromPitch(Integer.parseInt("" + myChar, 16)));
    this.set = Collections.unmodifiableSet(EnumSet.copyOf(_list));
    list = Collections.unmodifiableList(_list);
  }

  public Set<Natur> set() {
    return set;
  }

  @Override
  public String toString() {
    return private_toString;
  }

  public Triad transposed(int delta) {
    return MAP.get(set.stream() //
        .map(natur -> natur.ascend(delta)) //
        .collect(Collectors.toSet()));
  }

  public int indexOf(int pitch) {
    return list.indexOf(Natur.fromPitch(pitch));
  }
}
