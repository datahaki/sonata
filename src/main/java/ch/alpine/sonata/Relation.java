// code by jph
package ch.alpine.sonata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ch.alpine.sonata.utl.TpfStatics;

/** relation encodes the changes within a voice;
 * immutable */
public class Relation implements Comparable<Relation> {
  public static final Relation nada = new Relation(Link.NADA, null, null);
  // ---
  private static final int MAX = 128;
  private static final Relation[] _live = new Relation[MAX];
  private static final Relation[] _hold = new Relation[MAX];
  private static final Relation[] _kill = new Relation[MAX];
  static {
    for (int pitch = 0; pitch < MAX; ++pitch) {
      _live[pitch] = new Relation(Link.LIVE, pitch, null);
      _hold[pitch] = new Relation(Link.HOLD, pitch, null);
      _kill[pitch] = new Relation(Link.KILL, pitch, null);
    }
  }

  public static Relation live(int pitch) {
    return _live[pitch];
  }

  public static Relation hold(int pitch) {
    return _hold[pitch];
  }

  public static Relation kill(int pitch) {
    return _kill[pitch];
  }

  // ---
  private static final Map<Integer, Relation> _jump = new HashMap<>();

  public static Relation jump(int pitch, int delta) {
    final int key = (pitch << 8) + delta; // has to be unique, << 8 mimics *256 even if pitch negative
    if (!_jump.containsKey(key))
      _jump.put(key, new Relation(Link.JUMP, pitch, delta));
    return _jump.get(key);
  }

  // ---
  public final Link link;
  /** possibly null, or in interval [ -64:64 ) */
  public final Integer integer0;
  /** possibly null, or in interval [ -64:64 ) */
  public final Integer integer1;

  /** only called during static initialization
   * 
   * @param link
   * @param integer0
   * @param integer1 */
  private Relation(Link link, Integer integer0, Integer integer1) {
    this.link = link;
    this.integer0 = integer0;
    this.integer1 = integer1;
  }

  // ---
  public Relation mod12() {
    switch (link) {
    case NADA:
      return nada;
    case HOLD:
      return hold(TpfStatics.mod12(integer0));
    case LIVE:
      return live(TpfStatics.mod12(integer0));
    case KILL:
      return kill(TpfStatics.mod12(integer0));
    case JUMP:
      return jump(TpfStatics.mod12(integer0), integer1);
    default:
      throw new RuntimeException();
    }
    // new Relation(myRelation.myLink, myRelation.myInteger0 == null ? null : Natur.mod12(myRelation.myInteger0), myRelation.myInteger1)
  }

  public Relation transpose(int delta) { // not used yet
    switch (link) {
    case NADA:
      return nada;
    case HOLD:
      return hold(integer0 + delta);
    case LIVE:
      return live(integer0 + delta);
    case KILL:
      return kill(integer0 + delta);
    case JUMP:
      return jump(integer0 + delta, integer1);
    default:
      throw new RuntimeException();
    }
    // new Relation(myRelation.myLink, myRelation.myInteger0 == null ? null : Natur.mod12(myRelation.myInteger0), myRelation.myInteger1)
  }

  public Relation transposeMod12(int delta) {
    switch (link) {
    case NADA:
      return nada;
    case HOLD:
      return hold(TpfStatics.mod12(integer0 + delta));
    case LIVE:
      return live(TpfStatics.mod12(integer0 + delta));
    case KILL:
      return kill(TpfStatics.mod12(integer0 + delta));
    case JUMP:
      return jump(TpfStatics.mod12(integer0 + delta), integer1);
    default:
      throw new RuntimeException();
    }
    // new Relation(myRelation.myLink, myRelation.myInteger0 == null ? null : Natur.mod12(myRelation.myInteger0), myRelation.myInteger1)
  }

  public Relation backwards() {
    switch (link) {
    case NADA:
    case HOLD:
      return this;
    case LIVE:
      return Relation.kill(integer0);
    case KILL:
      return Relation.live(integer0);
    case JUMP:
      return Relation.jump(integer0 + integer1, -integer1);
    default:
      throw new RuntimeException();
    }
  }

  public boolean nonNada() {
    return !equals(nada);
  }

  public boolean isLink(Link link) {
    return this.link.equals(link);
  }

  public boolean isComplete() {
    return link.isComplete;
  }

  public Integer pitchAnte() {
    switch (link) {
    case LIVE: // note comes to life only afterwards
    case NADA: // rest
      return null;
    case JUMP: // myInteger0 represents pitchAnte
    case KILL: // myInteger0 represents pitchAnte
    case HOLD: // myInteger0 represents pitchAnte (and after)
      return integer0;
    default:
      throw new RuntimeException("invalid link " + link); // should be null at this point
    }
  }

  public Integer pitchPost() { // applicable for joints of order0
    switch (link) {
    case KILL: // note is gone afterwards
    case NADA: // rest
      return null;
    case LIVE: // myInteger0 represents pitchPost
    case HOLD: // myInteger0 represents pitchPost
      return integer0;
    case JUMP: // myInteger0+myInteger1 represents pitchAfter
      return integer0 + integer1;
    default:
      throw new RuntimeException("invalid link " + link); // should be null at this point
    }
  }

  public void addPitch12(Collection<Integer> ante, Collection<Integer> post) {
    switch (link) {
    case NADA: // rest
      break;
    case HOLD:
      ante.add(integer0); // myInteger0 represents pitchAnte (and after)
      post.add(integer0);
      break;
    case LIVE: // note comes to life only afterwards
      post.add(integer0);
      break;
    case KILL:
      ante.add(integer0); // myInteger0 represents pitchAnte
      break;
    case JUMP:
      ante.add(integer0); // myInteger0 represents pitchAnte
      post.add(TpfStatics.mod12(integer0 + integer1));
      break;
    default:
      throw new RuntimeException("invalid link " + link); // should be null at this point
    }
    // throw new RuntimeException("invalid link " + myLink); // should be null at this point
  }

  // cannot sort:
  // ScoreArray, ticks=2
  // \/ \/
  // v=0 -5. [ ]
  // v=1 -12. -8.
  // v=2 -17. -24.
  // @Override
  // public int compareToNew(Relation myRelation) {
  // Integer p0 = pitchAfter();
  // Integer p1 = myRelation.pitchAfter();
  // if (p0 != null && p1 != null && p0 != p1)
  // return p0 - p1;
  // p0 = pitchBefore();
  // p1 = myRelation.pitchBefore();
  // if (p0 != null && p1 != null && p0 != p1)
  // return p0 - p1;
  // // 6 HOLD and 6 JUMP 0
  // // System.out.println(toString()+" and "+myRelation);
  // // return myLink.compareTo(myRelation.myLink);
  // return compareToOld(myRelation);
  // }
  @Override
  public int compareTo(Relation relation) { // order is native but not musical
    int cmp = link.compareTo(relation.link);
    if (cmp != 0 || integer0 == null)
      return cmp;
    cmp = integer0 - relation.integer0; // canonic, i.e. 0 1 2, a b c
    return (cmp != 0 || integer1 == null) ? cmp : integer1 - relation.integer1; // canonic, i.e. 0 1 2, a b c
  }

  @Override
  public String toString() {
    return (integer0 == null ? "    " : String.format("%3d ", integer0)) + link + (integer1 == null ? "    " : String.format("%4d", integer1));
  }
}
