package org.jboss.errai.otec.tests;

import org.jboss.errai.otec.mutation.GUIDUtil;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class GUIDTests {
  @Test
  public void testGUIDUtilEntropy() {
    Set<String> set = new HashSet<String>();
    for (int i = 0; i < Integer.MAX_VALUE; i++) {
      if (i % 100000 == 0) {

        System.out.println(i);
        System.out.println("mem usage: " + Runtime.getRuntime().totalMemory() / (1024 * 1024) + "MB");
      }

      if (!set.add(GUIDUtil.createGUID())) {
        throw new RuntimeException("collision: " + i);
      }
    }
    System.out.println("okay.");
  }
}
