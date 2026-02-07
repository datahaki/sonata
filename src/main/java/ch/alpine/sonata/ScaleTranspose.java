// code by jph
package ch.alpine.sonata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.alpine.tensor.ext.Int;

public class ScaleTranspose {
  private final List<Tone> list;
  private final Map<Integer, Integer> indexOf = new HashMap<>();

  public ScaleTranspose(Scale scale) {
    list = Tone.all().stream().filter(tone -> scale.containsExact(tone.diatoneAlter())).toList();
    Int i = new Int();
    list.stream().forEach(tone -> indexOf.put(tone.pitch(), i.getAndIncrement()));
  }

  /** @param tone
   * @param delta
   * @return
   * @throws Exception if tone is not in scale */
  public Tone transpose(Tone tone, int delta) {
    return list.get(indexOf.get(tone.pitch()) + delta);
  }

  public void transposeInstance(Note note, int delta) {
    note.setTone(transpose(note.tone(), delta));
  }

  public Note transpose(Note note, int delta) {
    return new Note(transpose(note.tone(), delta), note.ticks());
  }

  public void transposeInstance(Torrent torrent, int delta) {
    torrent.list.forEach(note -> transposeInstance(note, delta));
  }

  public Torrent transpose(Torrent torrent, int delta) { // only called once outside from here
    Torrent myReturn = new Torrent(torrent.attributes);
    for (Note note : torrent)
      myReturn.list.add(transpose(note, delta));
    return myReturn;
  }
}
