// code by jph
package ch.alpine.sonata.jnt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.alpine.sonata.Joint;
import ch.alpine.sonata.Relation;
import sys.dat.TwoLists;

public class JointDecomposition {
  public List<Relation> complete = new ArrayList<>();
  public List<Integer> pitchAnte = new ArrayList<>();
  public List<Integer> pitchPost = new ArrayList<>();

  public JointDecomposition(List<Relation> myComplete, List<Integer> myPitchAnte, List<Integer> myPitchPost) {
    // TODO TPF style not ideal
    this.complete = myComplete;
    this.pitchAnte = myPitchAnte;
    this.pitchPost = myPitchPost;
  }

  /** assumes joint is mod12 */
  public JointDecomposition(Joint joint) {
    for (Relation relation : joint)
      if (relation.isComplete())
        complete.add(relation);
      else
        relation.addPitch12(pitchAnte, pitchPost);
    Collections.sort(pitchAnte);
    Collections.sort(pitchPost);
  }

  public JointDecomposition(Joint joint, boolean allPitch) {
    for (Relation relation : joint) {
      if (relation.isComplete())
        complete.add(relation);
      relation.addPitch12(pitchAnte, pitchPost);
    }
    Collections.sort(pitchAnte);
    Collections.sort(pitchPost);
  }

  public boolean isSubsetOf(JointDecomposition jointDecomposition) {
    return TwoLists.areSortedSublists(complete, jointDecomposition.complete) //
        && TwoLists.areSortedSublists(pitchAnte, jointDecomposition.pitchAnte) //
        && TwoLists.areSortedSublists(pitchPost, jointDecomposition.pitchPost);
  }

  public void print() {
    System.out.println(complete);
    System.out.println(pitchAnte);
    System.out.println(pitchPost);
  }
}
