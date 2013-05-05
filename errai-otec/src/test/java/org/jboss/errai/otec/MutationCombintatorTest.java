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

import junit.framework.Assert;
import org.jboss.errai.otec.client.Transformer;
import org.jboss.errai.otec.client.mutation.Mutation;
import org.jboss.errai.otec.client.mutation.MutationType;
import org.jboss.errai.otec.client.mutation.StringMutation;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock
 */
public class MutationCombintatorTest {

  @Test
  public void testConsecutiveInsertsCombination() {
    final List<Mutation> mutationList = new ArrayList<Mutation>();

    mutationList.add(StringMutation.of(MutationType.Insert, 0, "foo"));
    mutationList.add(StringMutation.of(MutationType.Insert, 3, "bar"));
    mutationList.add(StringMutation.of(MutationType.Insert, 6, "!"));

    final Mutation mutation = Transformer.mutationCombinitator(mutationList);
    Assert.assertNotNull(mutation);
    Assert.assertTrue(mutation.getType() == MutationType.Insert);
    Assert.assertEquals("foobar!", mutation.getData());
  }

  @Test
  public void testConsecutiveInsertsWithTrailingDeleteCombination() {
    final List<Mutation> mutationList = new ArrayList<Mutation>();

    mutationList.add(StringMutation.of(MutationType.Insert, 10, "foo"));
    mutationList.add(StringMutation.of(MutationType.Insert, 13, "bar"));
    mutationList.add(StringMutation.of(MutationType.Insert, 16, "!"));
    mutationList.add(StringMutation.of(MutationType.Delete, 14, "ar"));

    final Mutation mutation = Transformer.mutationCombinitator(mutationList);
    Assert.assertNotNull(mutation);
    Assert.assertTrue(mutation.getType() == MutationType.Insert);
    Assert.assertEquals("foob!", mutation.getData());
  }


  @Test
  public void testConsecutiveInsertsWithTrailingDeleteCombination2() {
    final List<Mutation> mutationList = new ArrayList<Mutation>();

    mutationList.add(StringMutation.of(MutationType.Insert, 10, "foo"));
    mutationList.add(StringMutation.of(MutationType.Insert, 13, "bar"));
    mutationList.add(StringMutation.of(MutationType.Insert, 16, "!"));
    mutationList.add(StringMutation.of(MutationType.Delete, 13, "bar!"));

    final Mutation mutation = Transformer.mutationCombinitator(mutationList);
    Assert.assertNotNull(mutation);
    Assert.assertTrue(mutation.getType() == MutationType.Insert);
    Assert.assertEquals("foo", mutation.getData());
  }

  @Test
  public void testInsertInsertAppendCombination() {
    final List<Mutation> mutationList = new ArrayList<Mutation>();

    mutationList.add(StringMutation.of(MutationType.Insert, 0, "X"));
    mutationList.add(StringMutation.of(MutationType.Insert, 0, "AB"));
    mutationList.add(StringMutation.of(MutationType.Insert, 3, "YZ"));

    final Mutation mutation = Transformer.mutationCombinitator(mutationList);

    Assert.assertNotNull(mutation);
    Assert.assertTrue(mutation.getType() == MutationType.Insert);
    Assert.assertEquals("ABXYZ", mutation.getData());
  }
}
