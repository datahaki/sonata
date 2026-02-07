// code by jph
package ch.alpine.sonata.enc.nvm;

import java.nio.file.Path;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.StringTokenizer;

import ch.alpine.bridge.lang.EnumValue;
import ch.alpine.bridge.lang.SI;
import ch.alpine.midkit.MidiInstrument;
import ch.alpine.sonata.Clef;
import ch.alpine.sonata.Division;
import ch.alpine.sonata.Dynamic;
import ch.alpine.sonata.FiguredBass;
import ch.alpine.sonata.KeyMode;
import ch.alpine.sonata.KeySignature;
import ch.alpine.sonata.Ornament;
import ch.alpine.sonata.ScoreEntry;
import ch.alpine.sonata.Torrent;
import ch.alpine.sonata.Voice;
import ch.alpine.sonata.io.NativeNoteFormat;
import ch.alpine.sonata.scr.Score;
import ch.alpine.sonata.scr.UniformFlatten;
import ch.alpine.sonata.tri.Triad;
import ch.alpine.tensor.Scalars;
import sys.dat.Manager;
import sys.dat.ManagerOp;

public class NativeReader {
  public Score get(Path file) {
    Score score = new Score();
    Manager manager = new Manager(file);
    List<String> manager_execute = List.of();
    try {
      manager_execute = ManagerOp.lines(file);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
    score.title = manager.getString("Score.Title", score.title);
    score.comment = manager.getString("Score.Comment", score.comment);
    score.keySignature = KeySignature.fromType(manager.getInteger("Score.KeySignature", score.keySignature.type()));
    score.setKeyMode(EnumValue.match(KeyMode.class, manager.getString("Score.KeyMode", score.keyMode().toString())));
    score.staffPartition = manager.getString("Score.StaffPartition", "");
    score.quarter = manager.getInteger("Score.Quarter", score.quarter);
    try {
      score.atom = Scalars.fromString(manager.getString("Score.Atom", score.atom.toString()));
    } catch (Exception exception) {
      exception.printStackTrace();
    }
    score.division = new Division(manager.getListInteger("Score.Division", Division.FALLBACK.getList()));
    score.period = manager.getInteger("Score.Period", score.period);
    try {
      score.bpm = SI.PER_MINUTE.quantity(Scalars.fromString(manager.getString("Score.Bpm", SI.PER_MINUTE.magnitude(score.bpm).toString())));
    } catch (Exception exception) {
      System.err.println("tempo bpm ignored");
    }
    {
      int count = 0;
      while (manager.containsKey("Voice[" + count + "].Clef")) {
        String key = "Voice[" + count + "].";
        Voice voice = new Voice();
        voice.clef = EnumValue.match(Clef.class, manager.getString(key + "Clef", "TREBLE"));
        voice.midiInstrument = EnumValue.match(MidiInstrument.class, manager.getString(key + "MidiInstrument", "GRAND_PIANO"));
        // voice.channel = manager.getInteger(key + "Channel", voice.channel);
        score.voices.add(voice);
        ++count;
      }
    }
    {
      Torrent torrent = null;
      for (String myLine : manager_execute)
        if (myLine.startsWith("VoiceInstance ")) {
          StringTokenizer stringTokenizer = new StringTokenizer(myLine);
          stringTokenizer.nextToken(); // drop first
          int voice = Integer.parseInt(stringTokenizer.nextToken());
          int ticks = Integer.parseInt(stringTokenizer.nextToken());
          torrent = new Torrent();
          if (0 < score.voices.get(voice).freeAt(ticks))
            score.voices.get(voice).navigableMap.put(ticks, torrent);
        } else //
        if (Objects.nonNull(torrent))
          torrent.list.add(NativeNoteFormat.INSTANCE.parseNote(myLine));
    }
    for (Entry<String, String> entry : manager.entrySet())
      try {
        String key = entry.getKey();
        String value = entry.getValue();
        if (key.startsWith("Shake(")) {
          ScoreEntry scoreEntry = ScoreEntry.fromString(key.substring(5));
          Ornament ornament = EnumValue.match(Ornament.class, value);
          score.voices.get(scoreEntry.voice()).shake.put(scoreEntry.ticks(), ornament);
        } else //
        if (key.startsWith("Press(")) {
          ScoreEntry scoreEntry = ScoreEntry.fromString(key.substring(5));
          Dynamic dynamics = EnumValue.match(Dynamic.class, value);
          score.voices.get(scoreEntry.voice()).press.put(scoreEntry.ticks(), dynamics);
        } else //
        if (key.startsWith("Lyric(")) {
          ScoreEntry scoreEntry = ScoreEntry.fromString(key.substring(5));
          score.voices.get(scoreEntry.voice()).lyric.put(scoreEntry.ticks(), value);
        } else //
        if (key.startsWith("Motif(")) {
          ScoreEntry scoreEntry = ScoreEntry.fromString(key.substring(5));
          int numel = Integer.parseInt(value);
          score.voices.get(scoreEntry.voice()).motif.put(scoreEntry.ticks(), numel);
        } else //
        if (key.startsWith("Fbass(")) {
          ScoreEntry scoreEntry = ScoreEntry.fromString(key.substring(5));
          FiguredBass figuredBass = FiguredBass.fromString(value);
          score.voices.get(scoreEntry.voice()).fbass.put(scoreEntry.ticks(), figuredBass);
        } else //
        if (key.startsWith("Chord@")) {
          Triad triad = Triad.fromString(value);
          if (Objects.isNull(triad))
            System.err.println("cannot parse to triad: " + value);
          else
            score.triad.put(Integer.parseInt(key.substring(6)), triad);
        } else //
        if (key.startsWith("Hepta@")) {
          // Hepta myHepta = Hepta.from(value);
          // if (myHepta == null)
          // System.out.println("cannot parse to hepta: " + value);
          // else
          // score.hepta.put(Integer.parseInt(key.substring(6)), myHepta);
        } else //
        if (key.startsWith("Text@"))
          score.text.put(Integer.parseInt(key.substring(5)), value);
      } catch (Exception exception) {
        exception.printStackTrace();
      }
    // like all formats: voices are guaranteed to be flattened (assumed by Conditioner)
    for (Voice voice : score.voices)
      UniformFlatten.applyTo(voice);
    return score;
  }
}
