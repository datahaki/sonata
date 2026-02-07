package ch.alpine.sonata.enc.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import ch.alpine.sonata.Voice;
import ch.alpine.sonata.enc.ScoreIO;
import ch.alpine.sonata.scr.Score;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.Unprotect;

class StaffPartitionTest {
  @Test
  void test() {
    Path file = Unprotect.path("/io/nvm/bwv0603.nvm");
    Score score = ScoreIO.read(file);
    assertEquals(score.staffPartition, "{2, 1, 1}");
    List<List<Voice>> list = StaffPartition.getStaffList(score);
    assertEquals(list.size(), 3);
    assertEquals(list.get(0).size(), 2);
    assertEquals(list.get(1).size(), 1);
    assertEquals(list.get(2).size(), 1);
    assertEquals(score.getStaffPartition(), Tensors.vector(2, 1, 1));
  }
}
