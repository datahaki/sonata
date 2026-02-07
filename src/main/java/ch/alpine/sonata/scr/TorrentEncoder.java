// code by jph
package ch.alpine.sonata.scr;

import ch.alpine.sonata.Note;
import ch.alpine.sonata.Tone;
import ch.alpine.sonata.Torrent;
import ch.alpine.sonata.utl.TpfStatics;
import sys.mat.IntRange;

public enum TorrentEncoder {
  ;
  /** @param torrent
   * @param swap45
   * @param duration
   * @return */
  public static String encodeIvoryDelta(Torrent torrent, boolean swap45, final boolean duration) {
    char[] myChar = new char[torrent.list.size() - 1]; // last byte is always zero
    int count = -1;
    Note myPrev = null;
    int encode = 0; // char 0 is not the most elegant if duration==false, but acceptable
    for (Note note : torrent) {
      if (0 <= count) {
        int jump = swap45 ? IvoryDeltaEncoder.encodeJump(myPrev, note) : IvoryDeltaEncoder.encodeJumpExact(myPrev, note); // encode pitch
        myChar[count] = (char) (encode + jump);
      }
      myPrev = note;
      // the bit shift by 5 means * 32 where 32 is larger than Flow.values().length
      if (duration)
        encode = note.ticks() << 5; // encode ticks, zero length as '0' should never happen
      ++count;
    }
    return new String(myChar);
  }

  public static String encodeNatur_PitchDelta(final int tonic, Torrent torrent) {
    char[] myChar = new char[torrent.list.size() - 1]; // last byte is always zero
    int count = -1;
    Note myPrev = null;
    for (Note note : torrent) {
      if (0 <= count) {
        int natur = TpfStatics.mod12(myPrev.tone().pitch() - tonic);
        int jump = Math.min(Math.max(-24, myPrev.tone().pitch() - note.tone().pitch()), 24); // encode pitch
        myChar[count] = (char) (natur + ((24 + jump) << 4));
      }
      myPrev = note;
      ++count;
    }
    return new String(myChar);
  }

  public static String encodeToneMod12(Torrent torrent) {
    char[] myChar = new char[torrent.list.size()]; // last byte is always zero
    int count = 0;
    for (Note note : torrent) {
      Tone myTone = note.tone();
      int hash = 8 + myTone.ivory().ivory() * 5 + myTone.diatoneAlter().alter().ordinal();
      if (hash <= 0)
        throw new RuntimeException("change hash formula");
      myChar[count] = (char) hash;
      ++count;
    }
    return new String(myChar);
  }

  public static IntRange locate(int ticks, Torrent torrent, int index, int length) {
    return new IntRange( //
        ticks + torrent.ticksBeforeNote(index), //
        ticks + torrent.ticksBeforeNote(index + length));
  }
  //
  // public static String encodeExact(Torrent myTorrent) {
  // char[] myChar = new char[myTorrent.myList.size() - 1]; // last byte is always zero
  // int count = -1;
  // Note myPrev = null;
  // int encode = 0;
  // for (Note myNote : myTorrent) {
  // if (0 <= count) {
  // int jump = IvoryEncoder.encodeJumpExact(myPrev, myNote); // encode pitch
  // myChar[count] = (char) (encode + jump);
  // }
  // myPrev = myNote;
  // // the bit shift by 5 means * 32 where 32 is larger than Flow.values().length
  // encode = myNote.ticks << 5; // encode ticks, zero length as '0' should never happen
  // ++count;
  // }
  // return new String(myChar);
  // }
}
