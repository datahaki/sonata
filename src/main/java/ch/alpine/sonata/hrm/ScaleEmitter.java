// code by jph
package ch.alpine.sonata.hrm;

import java.io.Serializable;
import java.util.Set;

import ch.alpine.sonata.Scale;
import ch.alpine.sonata.Tone;

public abstract class ScaleEmitter implements Serializable {
  protected final Scale scale;

  public ScaleEmitter(Scale scale) {
    this.scale = scale;
    finish(); // mandatory call to initialize state variables
  }

  public final Scale getScale() {
    return scale;
  }

  public abstract Integer digest(int ticks, Set<Tone> set);

  /** finishes the current feed and resets {@link ScaleEmitter}
   * 
   * @return what digest would return at this point of feed */
  public abstract Integer finish();
}
