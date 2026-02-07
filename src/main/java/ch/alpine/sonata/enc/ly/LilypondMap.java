// code by jph
package ch.alpine.sonata.enc.ly;

import ch.alpine.sonata.xml.XmlNote;

interface LilypondMap {
  /** function is required to write a LilyPond element of the same duration as XmlNote
   * 
   * @param ticks position in score
   * @param xmlNote
   * @throws Exception when writing to OutputStream fails */
  void intercept(int ticks, XmlNote xmlNote);
}
