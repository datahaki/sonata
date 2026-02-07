// code by jph
package ch.alpine.sonata.enc.mxl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

import ch.alpine.sonata.Voice;
import ch.alpine.sonata.enc.utl.MeasureChop;
import ch.alpine.sonata.xml.XmlNote;

class MusicXmlPart {
  final MeasureChop myMeasureChop;
  final List<Voice> myStaff;
  final int total;
  // staff -> measure -> notes
  final List<List<List<XmlNote>>> myList = new ArrayList<>();
  final boolean[] tie_stop;

  public MusicXmlPart(MeasureChop myMeasureChop, List<Voice> myStaff, int total) {
    this.myMeasureChop = myMeasureChop;
    this.myStaff = myStaff;
    this.total = total;
    for (Voice myVoice : myStaff)
      myList.add(myMeasureChop.fromVoice(myVoice, 0, total));
    tie_stop = new boolean[myStaff.size()];
  }

  public Collection<Integer> measureIndices() {
    return IntStream.range(0, total / myMeasureChop.measure).boxed().toList();
  }

  public Collection<Integer> voiceIndices() {
    return IntStream.range(0, myStaff.size()).boxed().toList();
  }

  public List<XmlNote> getChopAt(int voiceCount, int measureCount) {
    return myList.get(voiceCount).get(measureCount);
  }

  public Voice getVoice(int voice) {
    return myStaff.get(voice);
  }
}
