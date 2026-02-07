// code by jph
package ch.alpine.sonata.mid;

import ch.alpine.midkit.rec.MidiMessageReceiver;
import ch.alpine.sonata.Relation;

/** forwards midi events as relations */
public abstract class RelationReceiver extends MidiMessageReceiver {
  @Override
  public final void noteOn(int key, int velocity, long timestamp_ms) {
    process(Relation.live(key), timestamp_ms);
  }

  @Override
  public final void noteOff(int key, long timestamp_ms) {
    process(Relation.kill(key), timestamp_ms);
  }

  public abstract void process(Relation relation, long timestamp_ms);
}
