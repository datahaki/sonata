// code by jph
package ch.alpine.sonata.mid.cmp;

import java.util.List;

import ch.alpine.sonata.Voice;

// TODO TPF class design still not optimal !
public class AudioConfiguration {
  public final List<Voice> voices;
  public final boolean accompany;
  public final boolean figuredbass;

  public AudioConfiguration(List<Voice> voices, boolean accompany, boolean figuredbass) {
    this.voices = voices;
    this.accompany = accompany;
    this.figuredbass = figuredbass;
  }
}
