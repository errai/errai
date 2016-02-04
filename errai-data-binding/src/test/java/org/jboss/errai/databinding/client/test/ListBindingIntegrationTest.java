/*
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.databinding.client.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jboss.errai.databinding.client.BindableListChangeHandlerComponent;
import org.jboss.errai.databinding.client.TestModel;
import org.jboss.errai.databinding.client.TestModelWithList;
import org.jboss.errai.databinding.client.TestModelWithListOfTestModels;
import org.jboss.errai.databinding.client.TestModelWithListWidget;
import org.jboss.errai.databinding.client.api.Convert;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.StateSync;
import org.jboss.errai.ioc.client.test.AbstractErraiIOCTest;
import org.jboss.errai.marshalling.client.api.MarshallerFramework;

/**
 * Tests binding to lists with the {@link DataBinder}.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class ListBindingIntegrationTest extends AbstractErraiIOCTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.databinding.DataBindingTestModule";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    Convert.deregisterDefaultConverters();
    MarshallerFramework.initializeDefaultSessionProvider();
  }

  public void testListHandlerBindingToListProperty() throws Exception {
    final BindableListChangeHandlerComponent component = new BindableListChangeHandlerComponent();
    final TestModelWithListOfTestModels model = DataBinder.forType(TestModelWithListOfTestModels.class)
            .bind(component, "list", Convert.identityConverter(List.class)).getModel();
    model.setList(new ArrayList<>());
    final List<TestModel> list = model.getList();
    final List<TestModel> copy = component.getValue();
    final TestModel one = new TestModel("one");
    final TestModel two = new TestModel("two");
    final TestModel three = new TestModel("three");

    runTestModelListAssertions(list, copy, one, two, three);
  }

  public void testListHandlerBindingToListDirectly() throws Exception {
    final BindableListChangeHandlerComponent component = new BindableListChangeHandlerComponent();
    final List<TestModel> list = DataBinder.forListOfType(TestModel.class)
            .bind(component, "this", Convert.identityConverter(List.class)).getModel();
    final List<TestModel> copy = component.getValue();
    final TestModel one = new TestModel("one");
    final TestModel two = new TestModel("two");
    final TestModel three = new TestModel("three");

    runTestModelListAssertions(list, copy, one, two, three);
  }

  public void testListHandlerPauseAndResume() throws Exception {
    final BindableListChangeHandlerComponent component = new BindableListChangeHandlerComponent();
    final DataBinder<List<TestModel>> binder = DataBinder.forListOfType(TestModel.class)
            .bind(component, "this", Convert.identityConverter(List.class));
    final TestModel one = new TestModel("one");
    final TestModel two = new TestModel("two");

    binder.getModel().add(one);
    assertEquals("Binding failed before pause called.", binder.getModel(), component.getValue());

    binder.pause();
    binder.getModel().add(two);
    assertEquals("Component was updated after binding paused.", Collections.singletonList(one), component.getValue());

    binder.resume(StateSync.FROM_MODEL);
    assertEquals(Arrays.asList(one, two), binder.getModel());
    assertEquals("Component not updated after resume.", binder.getModel(), component.getValue());

    binder.pause();
    binder.getModel().clear();
    assertEquals("Component was updated after binding paused.", Arrays.asList(one, two), component.getValue());

    binder.resume(StateSync.FROM_UI);
    assertEquals(Arrays.asList(one, two), component.getValue());
    assertEquals(component.getValue(), binder.getModel());
  }

  public void testListHasValueBindingToListProperty() throws Exception {
    final TestModelWithListWidget component = new TestModelWithListWidget();
    final TestModelWithList model = DataBinder.forType(TestModelWithList.class)
            .bind(component, "list", Convert.identityConverter(List.class)).getModel();
    model.setList(new ArrayList<>());
    final List<String> list = model.getList();
    runStringListAssertions(component, list, "one", "two", "three");
  }

  public void testListHasValueBindingToListDirectly() throws Exception {
    final TestModelWithListWidget component = new TestModelWithListWidget();
    final List<String> list = DataBinder.forListOfType(String.class)
            .bind(component, "this", Convert.identityConverter(String.class)).getModel();
    runStringListAssertions(component, list, "one", "two", "three");
  }

  private void runStringListAssertions(final TestModelWithListWidget component, final List<String> list, final String one,
          final String two, final String three) {
    list.add(one);
    assertEquals(list, component.getValue());

    list.add(three);
    assertEquals(list, component.getValue());

    list.add(1, two);
    assertEquals(list, component.getValue());

    list.remove(2);
    assertEquals(list, component.getValue());

    list.removeAll(Collections.singleton(two));
    assertEquals(list, component.getValue());

    list.addAll(Collections.singleton(three));
    assertEquals(list, component.getValue());

    list.addAll(1, Collections.singleton(two));
    assertEquals(list, component.getValue());

    list.clear();
    assertEquals(list, component.getValue());
  }

  private void runTestModelListAssertions(final List<TestModel> boundList, final List<TestModel> copyList, final TestModel one,
          final TestModel two, final TestModel three) {
    boundList.add(one);
    assertEquals(boundList, copyList);

    boundList.add(three);
    assertEquals(boundList, copyList);

    boundList.add(1, two);
    assertEquals(boundList, copyList);

    boundList.remove(2);
    assertEquals(boundList, copyList);

    boundList.removeAll(Collections.singleton(two));
    assertEquals(boundList, copyList);

    boundList.addAll(Collections.singleton(three));
    assertEquals(boundList, copyList);

    boundList.addAll(1, Collections.singleton(two));
    assertEquals(boundList, copyList);

    boundList.get(0).setValue("one!");
    assertEquals(boundList, copyList);

    boundList.clear();
    assertEquals(boundList, copyList);
  }

}
