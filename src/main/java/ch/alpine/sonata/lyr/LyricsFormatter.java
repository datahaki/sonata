// code by jph
package ch.alpine.sonata.lyr;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Stream;

import sys.mat.IntRange;

public class LyricsFormatter {
  public int max_pass = 0;
  public final List<LyricString> list = new ArrayList<>();

  public LyricsFormatter(NavigableMap<Integer, String> navigableMap, boolean join) {
    // used when syllables are to be joined to reference previous text piece
    // pass -> lyrics
    Map<Integer, LyricString> map = new HashMap<>();
    for (Entry<Integer, String> myEntry : navigableMap.entrySet()) {
      final int ticks = myEntry.getKey();
      String[] mySplit = myEntry.getValue().split("\\|");
      max_pass = Math.max(mySplit.length, max_pass);
      for (int pass : IntRange.positive(mySplit.length)) {
        String myString = mySplit[pass];
        final boolean append = myString.endsWith("-") && join;
        LyricString myLyricString;
        if (map.containsKey(pass)) {
          myLyricString = map.get(pass);
          if (!append)
            map.remove(pass);
        } else {
          myLyricString = new LyricString(pass);
          myLyricString.ticksRange = new IntRange(ticks, ticks + 1); // max will be updated
          list.add(myLyricString);
          if (append)
            map.put(pass, myLyricString);
        }
        int myInteger = Optional.ofNullable(navigableMap.higherKey(ticks)).orElse(ticks + 1);
        myLyricString.ticksRange = new IntRange(myLyricString.ticksRange.min(), myInteger);
        myLyricString.string += append ? myString.substring(0, myString.length() - 1) : myString;
      }
    }
  }

  public Stream<LyricString> getStream(int pass) {
    return list.stream() //
        .filter(lyricString -> lyricString.pass == pass);
  }

  public NavigableMap<Integer, String> getMap(int pass) {
    NavigableMap<Integer, String> myNavigableMap = new TreeMap<>();
    getStream(pass).forEach(myLyricString -> myNavigableMap.put(myLyricString.ticksRange.min(), myLyricString.string));
    return myNavigableMap;
  }

  public void print(PrintStream myPrintStream) {
    for (int pass : IntRange.positive(max_pass))
      getStream(pass).forEach(myLyricString -> myPrintStream.println(myLyricString.string + " " + myLyricString.ticksRange));
  }
}
