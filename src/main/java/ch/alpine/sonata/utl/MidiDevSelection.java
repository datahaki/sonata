package ch.alpine.sonata.utl;

import java.util.List;
import java.util.function.Predicate;

import javax.sound.midi.MidiDevice.Info;

import ch.alpine.bridge.ref.ann.FieldSelectionCallback;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.midkit.MidiDevices;
import ch.alpine.midkit.get.MidiGetPredicate;
import ch.alpine.midkit.put.MidiPutPredicate;

@ReflectionMarker
public class MidiDevSelection {
  private final boolean isPut;
  @FieldSelectionCallback("names")
  public String name = "";
  public Boolean regex = false;
  /** purpose of maxMidi is to limit matches of type
   * VirMIDI [hw:0,0,0], VirMIDI [hw:0,0,1], ... */
  public static final int DEVICES_LIMIT = 5; // magic const

  public MidiDevSelection(boolean isMidiInput) {
    this.isPut = isMidiInput;
  }

  public Predicate<Info> getPredicate() {
    return regex //
        ? info -> info.getName().matches(name)
        : info -> info.getName().equals(name);
  }

  public List<String> names() {
    return MidiDevices.getList(isPut //
        ? MidiPutPredicate.INSTANCE
        : MidiGetPredicate.INSTANCE, MidiDevSelection.DEVICES_LIMIT).stream().map(Info::getName).toList();
  }
}
