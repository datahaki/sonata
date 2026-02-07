// code by jph
package ch.alpine.sonata;

import java.awt.Dimension;

import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.sca.Ceiling;
import sys.mat.IntRange;

/** VERZIERUNGEN - Anmerkungen zu Ornamenten in der Klaviermusik by J. Gedan,
 * March 2006, www.pian-e-forte.de
 * some documentation from Wikipedia */
public enum Ornament {
  PRALL(null, new int[] { 1, 0 }, null),
  MORDENT(new int[] { 0, -1, 0 }),
  // MordentUp(new int[] { 0, 1, 0 }),
  PRALL_MORDENT(new int[] { 1, 0 }, new int[] { 1, 0 }, new int[] { -1, 0 }),
  TURN(new int[] { 1, 0, -1, 0 }),
  REVERSE_TURN(new int[] { -1, 0, 1, 0 }),
  UP_PRALL(new int[] { -1, 0, 1, 0 }, new int[] { 1, 0 }, null),
  DOWN_PRALL(new int[] { 1, 0, -1, 0 }, new int[] { 1, 0 }, null),
  UP_MORDENT(new int[] { -1, 0 }, new int[] { 1, 0 }, new int[] { -1, 0 }),
  DOWN_MORDENT(new int[] { 1, 0, -1, 0 }, new int[] { 1, 0 }, new int[] { -1, 0 }),
  /** Acciaccatura an unmeasured grace note indicated by a slurred note with a slashed stem.
   * The Duration of an Acciaccatura is half as long as Note with ticks==1 */
  DOWN_ACCIACCATURA(new int[] { 1, 0 }),
  UP_ACCIACCATURA(new int[] { -1, 0 }),
  /** Appoggiatura takes a fixed fraction of the main note, but should be composed explicitly in score */
  /** Trill goes ad infty */
  DOWN_TRILL(null, new int[] { 1, 0 }, null),
  UP_TRILL(null, new int[] { 0, 1 }, null),
  /** Accent indicates that the marked note should have an emphasized beginning
   * and then taper off rather quickly.
   * This mark is correctly known by classically trained musicians as marcato,
   * though it is usually simply referred to as an accent.
   * In jazz articulation, it is stated as "dah". */
  ACCENT(new int[] { 0 }, 1 / 6.), // >
  /** Marcato (Italian for "hammered", marked) is generally accepted to be
   * as loud as an accent mark and as short as a staccato.
   * Marcato is a musical instruction indicating a note, chord, or passage
   * is to be played louder or more forcefully than surrounding music.
   * This is essentially an intensified version of the regular Accent.
   * Marcato asks for a greater dynamic accent.
   * Like the regular Accent, however, it is often interpreted to suggest a sharp
   * attack tapering to the original dynamic.
   * In jazz big-band scores the marcato symbol usually indicates a note
   * is to be shortened to approximately 2/3 its normal duration,
   * and given a moderate accent.
   * In jazz articulation, it is stated as "daht". */
  MARCATO(new int[] { 0 }, 1 / 3.), // ^
  FERMATA(new int[] { 0 });

  // ---
  private final int[] head;
  private final int[] loop; // loops at least once
  private final int[] tail;
  public final double velocity_delta;

  private Ornament(int[] tail) {
    this(tail, 0);
  }

  private Ornament(int[] tail, double incr) {
    this(null, null, tail, incr);
  }

  private Ornament(int[] head, int[] loop, int[] tail) {
    this(head, loop, tail, 0);
  }

  private Ornament(int[] head, int[] loop, int[] tail, double incr) {
    this.head = head == null ? new int[0] : head;
    this.loop = loop == null ? new int[0] : loop;
    this.tail = tail == null ? new int[0] : tail;
    velocity_delta = incr;
  }

  private int maxLoopCount() {
    return equals(PRALL) ? 3 : -1;
  }

  private int minLength() {
    return head.length + loop.length + tail.length;
  }

  public int getUnit(int factor, int score_quarter, Scalar score_atom) {
    return Ceiling.intValueExact(getAtom(score_quarter, score_atom).multiply(RealScalar.of(score_quarter * 4 * factor)));
  }

  public boolean isAcciaccatura() {
    return equals(DOWN_ACCIACCATURA) || equals(UP_ACCIACCATURA);
  }

  private Scalar getAtom(int score_quarter, Scalar score_atom) {
    switch (this) {
    case DOWN_ACCIACCATURA:
    case UP_ACCIACCATURA:
      return RealScalar.of(2 * 4 * score_quarter).reciprocal();
    default:
      return score_atom;
    }
  }

  public boolean canConvert(int note_ticks, int unit) {
    return 0 < unit //
        && minLength() * unit <= note_ticks;
  }

  public Melody convert(Note note, int unit, HeptatonicScale heptatonicScale) {
    if (!canConvert(note.ticks(), unit))
      throw new RuntimeException("note with ticks=" + note.ticks() + " is too short to install ornament of length=" + minLength() + "*" + unit);
    // ---
    Melody melody = new Melody();
    // install head
    for (int delta : head)
      melody.list.add(new Note(heptatonicScale.getToneFromIvory(note.tone().ivory().add(delta)), unit));
    // install loop
    int ticks = (head.length + tail.length) * unit;
    int loops = maxLoopCount();
    if (0 < loop.length)
      while (ticks + (loop.length * unit) <= note.ticks()) {
        for (int count : IntRange.positive(loop.length)) {
          int delta = loop[count];
          melody.list.add(new Note(heptatonicScale.getToneFromIvory(note.tone().ivory().add(delta)), Math.min(note.ticks() - ticks, unit)));
          ticks += unit;
        }
        --loops;
        if (loops == 0)
          break;
      }
    // install tail
    for (int delta : tail)
      melody.list.add(new Note(heptatonicScale.getToneFromIvory(note.tone().ivory().add(delta)), unit));
    // ---
    int remain = note.ticks() - ticks;
    if (remain < 0)
      throw new RuntimeException("overshot");
    melody.last().setTicks(melody.last().ticks() + remain);
    if (melody.ticks() != note.ticks())
      throw new RuntimeException("myMelody.ticks()=" + melody.ticks() + " != note.ticks=" + note.ticks());
    return melody;
  }

  /** image icons are 32x16 unscaled
   * 
   * @param zoom
   * @return */
  public static Dimension getDimension(int zoom) {
    int height = 10 + zoom * 2;
    return new Dimension(height * 2, height); // 2. == 32 / 16.
  }
}
