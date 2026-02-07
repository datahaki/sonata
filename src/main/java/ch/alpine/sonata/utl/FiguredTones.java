// code by jph
package ch.alpine.sonata.utl;

import java.util.List;
import java.util.stream.Collectors;

import ch.alpine.sonata.Figure;
import ch.alpine.sonata.FiguredBass;
import ch.alpine.sonata.KeySignature;
import ch.alpine.sonata.Note;
import ch.alpine.sonata.ScaleTranspose;
import ch.alpine.sonata.Suffix;
import ch.alpine.sonata.Tone;

public class FiguredTones {
  public final KeySignature keySignature;
  public final Note note;

  public FiguredTones(KeySignature keySignature, Tone tone) {
    this.keySignature = keySignature;
    note = new Note(keySignature.diatonicScale().getToneFromIvory(tone.ivory()), 1); // project tone to note in diatonic scale
  }

  /** @param figuredBass usually completed bass
   * @return list of tones. myTone is member if myFiguredBass contains 1 */
  public List<Tone> getTones(FiguredBass figuredBass) {
    return figuredBass.figures().stream() //
        .map(this::getTone) //
        .collect(Collectors.toList());
  }

  public Tone getTone(Figure figure) {
    Note myMove = null;
    ScaleTranspose scaleTranspose = new ScaleTranspose(keySignature.diatonicScale());
    myMove = scaleTranspose.transpose(note, figure.delta_diatonic()); // apply diatonic shift
    Tone tone = Tone.from(myMove.tone().ivory(), myMove.tone().diatoneAlter().alter().delta() + figure.suffix.delta());
    return tone;
  }

  public String getConventionalString(Figure myFigure) {
    Tone tone = getTone(myFigure);
    if (myFigure.number == 3)
      return keySignature.diatonicScale().containsExact(tone.diatoneAlter()) ? myFigure.toString() : tone.diatoneAlter().alter().getAccidentalAbsolute();
    // ---
    if (keySignature.diatonicScale().containsExact(tone.diatoneAlter()))
      return myFigure.toString();
    // ---
    return myFigure.number + tone.diatoneAlter().alter().getAccidentalAbsolute();
    // if (myFigure.equals(new Figure(6,Suffix.INCR)))
    // return "6̶";
    // return myFigure.toString();
  }

  private static final String[] lilypondFigureAbsoluteAccidentals = { "--", "-", "!", "+", "++" };

  public String getLilypondString(Figure figure) {
    Tone tone = getTone(figure);
    if (figure.number == 3)
      return keySignature.diatonicScale().containsExact(tone.diatoneAlter()) ? figure.toString()
          : "_" + lilypondFigureAbsoluteAccidentals[tone.diatoneAlter().alter().delta() + 2];
    // ---
    if (keySignature.diatonicScale().containsExact(tone.diatoneAlter()))
      return figure.toString();
    // ---
    if (figure.equals(new Figure(4, Suffix.INCR)) || figure.equals(new Figure(6, Suffix.INCR)))
      return figure.number + "\\\\";
    // ---
    return figure.number + lilypondFigureAbsoluteAccidentals[tone.diatoneAlter().alter().delta() + 2];
  }
}
