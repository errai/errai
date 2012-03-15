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
import org.jboss.errai.ioc.client.container.IOCBeanManager;
import org.jboss.errai.ioc.rebind.SortUnit;
import org.jboss.errai.ioc.rebind.ioc.util.WiringUtil;
import org.jboss.errai.ioc.tests.utils.res.AppController;
import org.jboss.errai.ioc.tests.utils.res.Contacts;
import org.jboss.errai.ioc.tests.utils.res.ContactsPresenter;
import org.jboss.errai.ioc.tests.utils.res.EditContactsPresenter;
import org.jboss.errai.ioc.tests.utils.res.HandlerManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;
import static org.jboss.errai.codegen.framework.meta.MetaClassFactory.get;

/**
 * @author Mike Brock
 */
public class ComplexScenarioAlgoTests extends TestCase {

  final SortUnit contactsSU = sortUnitOf(Contacts.class, sortUnitOf(AppController.class));
  final SortUnit handlerMgrSU = sortUnitOf(HandlerManager.class, hardDep(Contacts.class));
  final SortUnit contactsPresSU = sortUnitOf(ContactsPresenter.class, sortUnitOf(HandlerManager.class));
  final SortUnit editContactsSU = sortUnitOf(EditContactsPresenter.class, sortUnitOf(HandlerManager.class));
  final SortUnit appControllerSU = sortUnitOf(AppController.class, sortUnitOf(HandlerManager.class), sortUnitOf(IOCBeanManager.class));
  final SortUnit iocBeanMgrSU = sortUnitOf(IOCBeanManager.class);

  public void testBasicSort() {
    List<SortUnit> toSort = asList(contactsSU, handlerMgrSU, contactsPresSU, editContactsSU, appControllerSU, iocBeanMgrSU);
    List<SortUnit> sorted;

    for (int i = 0; i < 10000; i++) {
      Collections.shuffle(toSort);
      sorted = WiringUtil.worstSortAlgorithmEver(toSort);
      assertTrue(comesBefore(sorted, contactsSU, handlerMgrSU));
    }
  }


  public static boolean comesBefore(List<SortUnit> list, SortUnit compareFrom, SortUnit compareTo) {
    return list.indexOf(compareFrom) < list.indexOf(compareTo);
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
