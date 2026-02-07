// code by jph
package ch.alpine.sonata.enc.ly;

import ch.alpine.bridge.ref.ann.ReflectionMarker;

@ReflectionMarker
public class LilypondParam {
  public Boolean partial = true; // ly
  public Boolean color = true; // ly
  public Boolean index = true; // ly
  public Boolean chord = true; // ly xml
  public Boolean lyric = true; // ly xml midi
}
