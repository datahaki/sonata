// code by jph
package ch.alpine.sonata.enc.ly;

import ch.alpine.sonata.xml.XmlSize;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;

/** Lilypond specific PropertyCacher for tuplets */
abstract class TupletCacher extends PropertyCacher {
  private Scalar tuplet = RealScalar.ONE;

  public void handle(XmlSize xmlNote) {
    boolean hasTimeModification = xmlNote.hasTimeModification();
    if (hasTimeModification)
      if (!tuplet.equals(RealScalar.ONE) && !tuplet.equals(xmlNote.getTuplet()))
        handle(false); // tear down old tuplet, and right away open new below
    tuplet = xmlNote.getTuplet();
    handle(hasTimeModification);
  }

  @Override
  protected final void beginProperty() {
    writeTuplet("\\tuplet " + tuplet.reciprocal() + " { ");
  }

  @Override
  protected final void endProperty() {
    writeTuplet("} ");
  }

  public abstract void writeTuplet(String string);
}
