/*
 * Copyright 2013 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.otec;


import static junit.framework.Assert.assertEquals;

import org.jboss.errai.otec.client.mutation.MutationType;
import org.jboss.errai.otec.client.util.DiffUtil;
import org.jboss.errai.otec.client.util.DiffPatchMerge;
import org.junit.Test;

import java.util.LinkedList;

/**
 * @author Mike Brock
 */
public class DiffUtilTests {

  @Test
  public void testInsertDiff() {
    final DiffUtil.Delta diff = DiffUtil.diff("bob the cat", "bobby the cat");

    assertEquals(MutationType.Insert, diff.getType());
    assertEquals("by", diff.getDeltaText());
    assertEquals(3, diff.getCursor());
  }

  @Test
  public void testInsertDiff2() {
    final DiffUtil.Delta diff = DiffUtil.diff("bob the cat", "bob the cat!!!");

    assertEquals(MutationType.Insert, diff.getType());
    assertEquals("!!!", diff.getDeltaText());
    assertEquals(11, diff.getCursor());
  }

  @Test
  public void testInsertDiff3() {
    final DiffUtil.Delta diff = DiffUtil.diff("bob the cat", ">>>bob the cat");

    assertEquals(MutationType.Insert, diff.getType());
    assertEquals(">>>", diff.getDeltaText());
    assertEquals(0, diff.getCursor());
  }

  @Test
  public void testDeleteDiff() {
    final DiffUtil.Delta diff = DiffUtil.diff("bob the cat", "bobcat");

    assertEquals(MutationType.Delete, diff.getType());
    assertEquals(" the ", diff.getDeltaText());
    assertEquals(3, diff.getCursor());
  }

  @Test
  public void testDeleteDiff2() {
    final DiffUtil.Delta diff = DiffUtil.diff("bob the cat", "cat");

    assertEquals(MutationType.Delete, diff.getType());
    assertEquals("bob the ", diff.getDeltaText());
    assertEquals(0, diff.getCursor());
  }

  @Test
  public void testDeleteDiff3() {
    final DiffUtil.Delta diff = DiffUtil.diff("bob the cat", "");

    assertEquals(MutationType.Delete, diff.getType());
    assertEquals("bob the cat", diff.getDeltaText());
    assertEquals(0, diff.getCursor());
  }

  @Test
  public void testMeyerDiff() {
    final LinkedList<DiffPatchMerge.Diff> diffs = new DiffPatchMerge().diff_main("bob the cat", "bob a the cat!!!");

    System.out.println(diffs);

  }
}
