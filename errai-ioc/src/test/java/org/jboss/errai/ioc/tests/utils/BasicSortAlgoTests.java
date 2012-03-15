/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.ioc.tests.utils;

import junit.framework.TestCase;
import org.jboss.errai.ioc.rebind.SortUnit;
import org.jboss.errai.ioc.rebind.ioc.util.WiringUtil;
import org.jboss.errai.ioc.tests.utils.res.Bar;
import org.jboss.errai.ioc.tests.utils.res.Foo;
import org.jboss.errai.ioc.tests.utils.res.Outer;

import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Arrays.sort;
import static org.jboss.errai.codegen.framework.meta.MetaClassFactory.get;

/**
 * @author Mike Brock
 */
public class BasicSortAlgoTests extends TestCase {

  final SortUnit su1 = sortUnitOf(Foo.class, sortUnitOf(Bar.class));
  final SortUnit su2 = sortUnitOf(Bar.class, hardDep(Foo.class));
  final SortUnit su3 = sortUnitOf(Outer.class, sortUnitOf(Foo.class), sortUnitOf(Bar.class));
  final SortUnit su4 = sortUnitOf(Integer.class, sortUnitOf(Foo.class));

  public void testBasicSort() {

    List<SortUnit> sorted1 = WiringUtil.worstSortAlgorithmEver(asList(su2, su1, su3, su4));
    List<SortUnit> sorted2 = WiringUtil.worstSortAlgorithmEver(asList(su1, su2, su4, su3));
    List<SortUnit> sorted3 = WiringUtil.worstSortAlgorithmEver(asList(su3, su4, su1, su2));
    List<SortUnit> sorted4 = WiringUtil.worstSortAlgorithmEver(asList(su4, su1, su3, su2));

    doAssertionsForScenario1(sorted1);
    doAssertionsForScenario1(sorted2);
    doAssertionsForScenario1(sorted3);
    doAssertionsForScenario1(sorted4);
  }

  private void doAssertionsForScenario1(List<SortUnit> sorted) {
    assertTrue(isInRange(0, 2, su1, sorted));
    assertTrue(isInRange(0, 2, su2, sorted));

    assertTrue(isInRange(2, 4, su4, sorted));
    assertTrue(isInRange(2, 4, su3, sorted));
  }

  private static boolean isInRange(int start, int end, SortUnit su, List<SortUnit> list) {
    for (int i = start; i < list.size() && i < end; i++) {
      if (list.get(i).equals(su)) return true;
    }
    return false;
  }

  private static SortUnit hardDep(Class ref) {
    return new SortUnit(get(ref), true);
  }

  private static SortUnit sortUnitOf(Class ref, SortUnit... deps) {
    return new SortUnit(get(ref), null, new HashSet<SortUnit>(asList(deps)));
  }
}
