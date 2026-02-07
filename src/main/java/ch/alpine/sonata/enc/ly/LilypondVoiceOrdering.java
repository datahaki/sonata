// code by jph
package ch.alpine.sonata.enc.ly;

import java.util.ArrayList;
import java.util.List;

import sys.mat.IntRange;

/** recommended indexing of multiple voices in same staff
 * 
 * for 1 voice : [0]
 * for 2 voices: [0, 1]
 * for 3 voices: [0, 2, 1]
 * for 4 voices: [0, 3, 1, 2]
 * for 5 voices: [0, 4, 1, 3, 2] */
class LilypondVoiceOrdering {
  static List<Integer> get(int size) {
    List<Integer> list = new ArrayList<>();
    for (int index : IntRange.positive(size))
      list.add(index % 2 == 0 ? index / 2 : size - 1 - index / 2);
    return list;
  }
}
