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

package org.jboss.errai.ui.test.binding.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.jboss.errai.databinding.client.BindableListWrapper;
import org.jboss.errai.databinding.client.BindableProxy;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ui.client.local.spi.InvalidBeanScopeException;
import org.jboss.errai.ui.client.widget.ListWidget;
import org.jboss.errai.ui.test.binding.client.res.AppScopedBindingListWidget;
import org.jboss.errai.ui.test.binding.client.res.BindingItemWidget;
import org.jboss.errai.ui.test.binding.client.res.BindingListWidget;
import org.jboss.errai.ui.test.binding.client.res.BindingTemplate;
import org.jboss.errai.ui.test.binding.client.res.SingletonBindingListWidget;
import org.jboss.errai.ui.test.common.client.TestModel;
import org.junit.Test;

import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Tests data binding with {@link ListWidget}.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ListWidgetBindingTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return getClass().getName().replaceAll("client.*$", "Test");
  }

  @Test
  public void testAutomaticListBinding() {
    BindingTemplateTestApp app = IOC.getBeanManager().lookupBean(BindingTemplateTestApp.class).getInstance();
    BindingTemplate template = app.getTemplate();
    assertNotNull("Template instance was not injected!", template);
    assertEquals("Expected zero widgets", 0, template.getListWidget().getWidgetCount());

    TestModel model = template.getModel();
    assertEquals("Expected zero widgets", 0, template.getListWidget().getWidgetCount());
    model.getChildren().add(new TestModel(3, "c3"));
    assertEquals("Expected one widget", 1, template.getListWidget().getWidgetCount());
    model.getChildren().remove(0);
    assertEquals("Expected zero widgets", 0, template.getListWidget().getWidgetCount());

    List<TestModel> children = new ArrayList<TestModel>();
    children.add(new TestModel(1, "c1"));
    children.add(new TestModel(2, "c2"));
    model.setChildren(children);
    assertEquals("Expected two widgets", 2, template.getListWidget().getWidgetCount());
    model.getChildren().add(new TestModel(3, "c3"));
    assertEquals("Expected three widgets", 3, template.getListWidget().getWidgetCount());
  }

  @Test
  public void testManualListBinding() {
    List<TestModel> modelList = new ArrayList<TestModel>();
    TestModel model0 = new TestModel();
    TestModel model1 = new TestModel();
    modelList.add(model0);
    modelList.add(model1);

    BindingTemplateTestApp app = IOC.getBeanManager().lookupBean(BindingTemplateTestApp.class).getInstance();
    BindingListWidget listWidget = app.getListWidget();
    // binding a list of model objects
    listWidget.setItems(modelList);

    assertEquals("Expected two widgets", 2, listWidget.getWidgetCount());
    assertEquals("", listWidget.getWidget(0).getTextBox().getText());
    assertEquals("", listWidget.getWidget(1).getTextBox().getText());
    assertEquals("onItemsRendered should be called exactly one time", 1, listWidget.getItemsRenderedCalled());

    BindingItemWidget itemWidget0 = listWidget.getWidget(0);
    assertEquals(itemWidget0, listWidget.getWidget(model0));
    BindingItemWidget itemWidget1 = listWidget.getWidget(1);
    assertEquals(itemWidget1, listWidget.getWidget(model1));

    itemWidget0.getModel().setName("0");
    itemWidget1.getModel().setName("1");
    assertEquals("First item widget was not updated!", "0", itemWidget0.getTextBox().getText());
    assertEquals("Second item widget was not updated!", "1", itemWidget1.getTextBox().getText());

    itemWidget0.getTextBox().setValue("0-updated", true);
    itemWidget1.getTextBox().setValue("1-updated", true);
    assertEquals("First model object was not updated!", "0-updated", itemWidget0.getModel().getName());
    assertEquals("Second model object was not updated!", "1-updated", itemWidget1.getModel().getName());
  }

  @Test
  public void testListBindingOfNullList() {
    BindingTemplateTestApp app = IOC.getBeanManager().lookupBean(BindingTemplateTestApp.class).getInstance();
    BindingListWidget listWidget = app.getListWidget();
    listWidget.setValue(null);
    List<TestModel> proxiedList = listWidget.getValue();
    
    assertNotNull(proxiedList);
    assertTrue(proxiedList instanceof BindableListWrapper);
    assertTrue(proxiedList.isEmpty());
  }
  
  @Test
  public void testUnorderedListWidget() {
    List<TestModel> modelList = new ArrayList<TestModel>();
    modelList.add(new TestModel());

    BindingTemplateTestApp app = IOC.getBeanManager().lookupBean(BindingTemplateTestApp.class).getInstance();
    ListWidget<TestModel, BindingItemWidget> listWidget = app.getUlListWidget();
    assertNotNull(listWidget);
    listWidget.setItems(modelList);

    
    Widget item = listWidget.getWidget(0);
    assertNotNull(item);
    Widget panel = item.getParent();
    assertTrue(panel instanceof HTMLPanel);
    assertEquals(panel.getElement().getNodeName(), "UL");
  }
  
  @Test
  public void testTableListWidget() {
    List<TestModel> modelList = new ArrayList<TestModel>();
    modelList.add(new TestModel());

    BindingTemplateTestApp app = IOC.getBeanManager().lookupBean(BindingTemplateTestApp.class).getInstance();
    ListWidget<TestModel, BindingItemWidget> listWidget = app.getTableListWidget();
    assertNotNull(listWidget);
    listWidget.setItems(modelList);
    
    Widget item = listWidget.getWidget(0);
    assertNotNull(item);
    Widget panel = item.getParent();
    assertTrue(panel instanceof HTMLPanel);
    assertEquals(panel.getElement().getNodeName(), "TABLE");
  }

  @Test
  public void testAppScopedWidgetInListCausesError() {
    List<TestModel> list = new ArrayList<TestModel>();
    AppScopedBindingListWidget listWidget = new AppScopedBindingListWidget();
    try {
      listWidget.setItems(list);
      fail("Did not throw InvalidBeanScopeException");
    } catch (InvalidBeanScopeException e) {}
  }
  
  @Test
  public void testSingletonWidgetInListCausesError() {
    List<TestModel> list = new ArrayList<TestModel>();
    SingletonBindingListWidget listWidget = new SingletonBindingListWidget();
    try {
      listWidget.setItems(list);
      fail("Did not throw InvalidBeanScopeException");
    } catch (InvalidBeanScopeException e) {}
  }
  
  @Test
  public void testListBindingAndAddItem() {
    List<TestModel> modelList = new ArrayList<TestModel>();
    modelList.add(new TestModel(0, "0"));

    BindingTemplateTestApp app = IOC.getBeanManager().lookupBean(BindingTemplateTestApp.class).getInstance();
    BindingListWidget listWidget = app.getListWidget();
    listWidget.setItems(modelList);
    assertEquals("Expected one widget", 1, listWidget.getWidgetCount());

    // Add a widget to the UI by adding a model instance to the model list
    listWidget.getItems().add(new TestModel(1, "1"));
    assertEquals("Expected two widgets", 2, listWidget.getWidgetCount());

    BindingItemWidget itemWidget1 = listWidget.getWidget(1);
    assertEquals("Second item widget was not rendered correctly!", "1", itemWidget1.getTextBox().getText());
  }
  
  @Test
  public void testListBindingAndAddManyItems() {
    List<TestModel> modelList = new ArrayList<TestModel>();
    modelList.add(new TestModel(1, "0"));

    BindingTemplateTestApp app = IOC.getBeanManager().lookupBean(BindingTemplateTestApp.class).getInstance();
    BindingListWidget listWidget = app.getListWidget();
    listWidget.setItems(modelList);
    assertEquals("Expected one widgets", 1, listWidget.getWidgetCount());

    List<TestModel> addModelList = new ArrayList<TestModel>();
    for (int i = 1; i < 500; i++) {
      addModelList.add(new TestModel(i, ""+i));
    }

    // Add widgets to the UI by adding model instances to the model list
    listWidget.getItems().addAll(addModelList);
    assertEquals("Expected exactly 100 widgets", 500, listWidget.getWidgetCount());
  }

  @Test
  public void testListBindingAndAddItemByIndex() {
    List<TestModel> modelList = new ArrayList<TestModel>();
    modelList.add(new TestModel(0, "0"));
    modelList.add(new TestModel(2, "2"));
    modelList.add(new TestModel(3, "3"));

    BindingTemplateTestApp app = IOC.getBeanManager().lookupBean(BindingTemplateTestApp.class).getInstance();
    BindingListWidget listWidget = app.getListWidget();
    listWidget.setItems(modelList);
    assertEquals("Expected three widgets", 3, listWidget.getWidgetCount());

    // Add a widget to the UI by adding a model instance to the model list
    listWidget.getItems().add(1, new TestModel(1, "1"));
    assertItemsRendered(listWidget);
  }
  
  @Test
  public void testListBindingAndAddItemByIndexUsingHtmlPanel() {
    List<TestModel> modelList = new ArrayList<TestModel>();
    modelList.add(new TestModel(0, "0"));
    modelList.add(new TestModel(2, "2"));
    modelList.add(new TestModel(3, "3"));

    BindingTemplateTestApp app = IOC.getBeanManager().lookupBean(BindingTemplateTestApp.class).getInstance();
    ListWidget<TestModel, BindingItemWidget> listWidget = app.getTableListWidget();
    listWidget.setItems(modelList);
    assertEquals("Expected three widgets", 3, listWidget.getWidgetCount());

    // Add a widget to the UI by adding a model instance to the model list
    listWidget.getValue().add(1, new TestModel(1, "1"));
    assertItemsRendered(listWidget);
  }
  
  @Test
  public void testListBindingAndAddItemByIndexAtHead() {
    List<TestModel> modelList = new ArrayList<TestModel>();
    modelList.add(new TestModel(1, "1"));
    modelList.add(new TestModel(2, "2"));
    modelList.add(new TestModel(3, "3"));

    BindingTemplateTestApp app = IOC.getBeanManager().lookupBean(BindingTemplateTestApp.class).getInstance();
    BindingListWidget listWidget = app.getListWidget();
    listWidget.setItems(modelList);
    assertEquals("Expected three widgets", 3, listWidget.getWidgetCount());

    // Add a widget to the UI by adding a model instance to the model list
    listWidget.getItems().add(0, new TestModel(0, "0"));
    assertItemsRendered(listWidget);
  }

  @Test
  public void testListBindingAndAddItemByIndexAtTail() {
    List<TestModel> modelList = new ArrayList<TestModel>();
    modelList.add(new TestModel(0, "0"));
    modelList.add(new TestModel(1, "1"));
    modelList.add(new TestModel(2, "2"));

    BindingTemplateTestApp app = IOC.getBeanManager().lookupBean(BindingTemplateTestApp.class).getInstance();
    BindingListWidget listWidget = app.getListWidget();
    listWidget.setItems(modelList);
    assertEquals("Expected three widgets", 3, listWidget.getWidgetCount());

    // Add a widget to the UI by adding a model instance to the model list
    listWidget.getItems().add(3, new TestModel(3, "3"));
    assertItemsRendered(listWidget);
  }

  @Test
  public void testListBindingAndAddItems() {
    List<TestModel> modelList = new ArrayList<TestModel>();
    modelList.add(new TestModel(0, "0"));
    modelList.add(new TestModel(1, "1"));

    BindingTemplateTestApp app = IOC.getBeanManager().lookupBean(BindingTemplateTestApp.class).getInstance();
    BindingListWidget listWidget = app.getListWidget();
    listWidget.setItems(modelList);
    assertEquals("Expected two widgets", 2, listWidget.getWidgetCount());

    List<TestModel> addModelList = new ArrayList<TestModel>();
    addModelList.add(new TestModel(2, "2"));
    addModelList.add(new TestModel(3, "3"));

    // Add widgets to the UI by adding model instances to the model list
    listWidget.getItems().addAll(addModelList);
    assertItemsRendered(listWidget);
  }
  
  @Test
  public void testListBindingAndAddItemsByIndex() {
    List<TestModel> modelList = new ArrayList<TestModel>();
    modelList.add(new TestModel(0, "0"));
    modelList.add(new TestModel(3, "3"));

    BindingTemplateTestApp app = IOC.getBeanManager().lookupBean(BindingTemplateTestApp.class).getInstance();
    BindingListWidget listWidget = app.getListWidget();
    listWidget.setItems(modelList);
    assertEquals("Expected two widgets", 2, listWidget.getWidgetCount());

    List<TestModel> addModelList = new ArrayList<TestModel>();
    addModelList.add(new TestModel(1, "1"));
    addModelList.add(new TestModel(2, "2"));

    // Add widgets to the UI by adding model instances to the model list
    listWidget.getItems().addAll(1, addModelList);
    assertItemsRendered(listWidget);
  }
  
  @Test
  public void testListBindingAndAddItemsByIndexUsingHtmlPanel() {
    List<TestModel> modelList = new ArrayList<TestModel>();
    modelList.add(new TestModel(0, "0"));
    modelList.add(new TestModel(3, "3"));

    BindingTemplateTestApp app = IOC.getBeanManager().lookupBean(BindingTemplateTestApp.class).getInstance();
    ListWidget<TestModel, BindingItemWidget> listWidget = app.getUlListWidget();
    listWidget.setItems(modelList);
    assertEquals("Expected two widgets", 2, listWidget.getWidgetCount());

    List<TestModel> addModelList = new ArrayList<TestModel>();
    addModelList.add(new TestModel(1, "1"));
    addModelList.add(new TestModel(2, "2"));

    // Add widgets to the UI by adding model instances to the model list
    listWidget.getValue().addAll(1, addModelList);
    assertItemsRendered(listWidget);
  }

  @Test
  public void testListBindingAndAddItemsByIndexAtHead() {
    List<TestModel> modelList = new ArrayList<TestModel>();
    modelList.add(new TestModel(2, "2"));
    modelList.add(new TestModel(3, "3"));

    BindingTemplateTestApp app = IOC.getBeanManager().lookupBean(BindingTemplateTestApp.class).getInstance();
    BindingListWidget listWidget = app.getListWidget();
    listWidget.setItems(modelList);
    assertEquals("Expected two widgets", 2, listWidget.getWidgetCount());

    List<TestModel> addModelList = new ArrayList<TestModel>();
    addModelList.add(new TestModel(0, "0"));
    addModelList.add(new TestModel(1, "1"));

    // Add widgets to the UI by adding model instances to the model list
    listWidget.getItems().addAll(0, addModelList);
    assertItemsRendered(listWidget);
  }

  @Test
  public void testListBindingAndAddItemsByIndexAtTail() {
    List<TestModel> modelList = new ArrayList<TestModel>();
    modelList.add(new TestModel(0, "0"));
    modelList.add(new TestModel(1, "1"));

    BindingTemplateTestApp app = IOC.getBeanManager().lookupBean(BindingTemplateTestApp.class).getInstance();
    BindingListWidget listWidget = app.getListWidget();
    listWidget.setItems(modelList);
    assertEquals("Expected two widgets", 2, listWidget.getWidgetCount());

    List<TestModel> addModelList = new ArrayList<TestModel>();
    addModelList.add(new TestModel(2, "2"));
    addModelList.add(new TestModel(3, "3"));

    // Add a widgets to the UI by adding model instances to the model list
    listWidget.getItems().addAll(2, addModelList);
    assertItemsRendered(listWidget);
  }

  @Test
  public void testListBindingAndClearItems() {
    List<TestModel> modelList = new ArrayList<TestModel>();
    modelList.add(new TestModel(0, "0"));
    modelList.add(new TestModel(1, "1"));

    BindingTemplateTestApp app = IOC.getBeanManager().lookupBean(BindingTemplateTestApp.class).getInstance();
    BindingListWidget listWidget = app.getListWidget();
    listWidget.setItems(modelList);
    assertEquals("Expected two widgets", 2, listWidget.getWidgetCount());
    
    List <BindingItemWidget> testWidgetsCleared = new ArrayList<BindingItemWidget>();
    for (int i = 0; i < listWidget.getWidgetCount(); i++) {
      testWidgetsCleared.add(listWidget.getWidget(i));
    }
    listWidget.getItems().clear();
    assertEquals("Expected zero widgets", 0, listWidget.getWidgetCount());
    
    Iterator<BindingItemWidget> itr = testWidgetsCleared.iterator();
    while(itr.hasNext()) {
      //BindingItemWidget.getNum() is incremented in a pre-destroy method
      assertEquals(1, itr.next().getNum());
    }
  }

  @Test
  public void testListBindingAndSetItems() {
    List<TestModel> modelList = new ArrayList<TestModel>();
    modelList.add(new TestModel(0, "0"));
    modelList.add(new TestModel(2, "2"));
    modelList.add(new TestModel(1, "1"));
    modelList.add(new TestModel(3, "3"));

    BindingTemplateTestApp app = IOC.getBeanManager().lookupBean(BindingTemplateTestApp.class).getInstance();
    BindingListWidget listWidget = app.getListWidget();
    listWidget.setItems(modelList);
    assertEquals("Expected two widgets", 4, listWidget.getWidgetCount());

    listWidget.getItems().set(1, new TestModel(1, "1"));
    listWidget.getItems().set(2, new TestModel(2, "2"));
    assertItemsRendered(listWidget);
  }

  @Test
  public void testListBindingAndRemoveItem() {
    TestModel itemToRemove = new TestModel(4, "4");
    List<TestModel> modelList = new ArrayList<TestModel>();
    modelList.add(new TestModel(0, "0"));
    modelList.add(new TestModel(1, "1"));
    modelList.add(itemToRemove);
    modelList.add(new TestModel(2, "2"));
    modelList.add(new TestModel(3, "3"));

    BindingTemplateTestApp app = IOC.getBeanManager().lookupBean(BindingTemplateTestApp.class).getInstance();
    BindingListWidget listWidget = app.getListWidget();
    listWidget.setItems(modelList);
    assertEquals("Expected five widgets", 5, listWidget.getWidgetCount());

    BindingItemWidget widget = listWidget.getWidget(2);
    listWidget.getItems().remove(itemToRemove);
    assertEquals(1, widget.getNum());
    assertItemsRendered(listWidget);
  }

  @Test
  public void testListBindingAndRemoveItems() {
    List<TestModel> removeList = new ArrayList<TestModel>();
    TestModel removeModel1 = (new TestModel(4, "4"));
    TestModel removeModel2 = (new TestModel(5, "5"));
    removeList.add(removeModel1);
    removeList.add(removeModel2);

    List<TestModel> modelList = new ArrayList<TestModel>();
    modelList.add(new TestModel(0, "0"));
    modelList.add(removeModel1);
    modelList.add(new TestModel(1, "1"));
    modelList.add(new TestModel(2, "2"));
    modelList.add(removeModel2);
    modelList.add(new TestModel(3, "3"));

    BindingTemplateTestApp app = IOC.getBeanManager().lookupBean(BindingTemplateTestApp.class).getInstance();
    BindingListWidget listWidget = app.getListWidget();
    listWidget.setItems(modelList);
    assertEquals("Expected six widgets", 6, listWidget.getWidgetCount());
    
    List<BindingItemWidget> testWidgetsRemoved = new ArrayList<BindingItemWidget>();
    
    for(int i = 0; i < listWidget.getWidgetCount(); i++) {
      testWidgetsRemoved.add(listWidget.getWidget(i));
    }

    listWidget.getItems().removeAll(removeList);
    
    for(int i = 0; i < listWidget.getWidgetCount(); i++) {
      if ((i == 1) || (i == 4))
        assertEquals("Widget " + i + " failed", 1, testWidgetsRemoved.get(i).getNum());
      else
        assertEquals("Widget " + i + " failed", 0, testWidgetsRemoved.get(i).getNum());
    }
    assertItemsRendered(listWidget);
  }

  @Test
  public void testListBindingAndRemoveItemByIndex() {
    List<TestModel> modelList = new ArrayList<TestModel>();
    modelList.add(new TestModel(0, "0"));
    modelList.add(new TestModel(1, "1"));
    modelList.add(new TestModel(4, "4"));
    modelList.add(new TestModel(2, "2"));
    modelList.add(new TestModel(3, "3"));

    BindingTemplateTestApp app = IOC.getBeanManager().lookupBean(BindingTemplateTestApp.class).getInstance();
    BindingListWidget listWidget = app.getListWidget();
    listWidget.setItems(modelList);
    assertEquals("Expected five widgets", 5, listWidget.getWidgetCount());

    BindingItemWidget widget = listWidget.getWidget(2);
    listWidget.getItems().remove(2);
    assertEquals(1, widget.getNum());
    assertItemsRendered(listWidget);
  }
  
  @Test
  public void testListBindingAndCollectionsSort() {
    List<TestModel> modelList = new ArrayList<TestModel>();
    modelList.add(new TestModel(3, "3"));
    modelList.add(new TestModel(2, "2"));
    modelList.add(new TestModel(1, "1"));
    modelList.add(new TestModel(0, "0"));
    
    BindingTemplateTestApp app = IOC.getBeanManager().lookupBean(BindingTemplateTestApp.class).getInstance();
    BindingListWidget listWidget = app.getListWidget();
    listWidget.setItems(modelList);
    assertEquals("Expected four widgets", 4, listWidget.getWidgetCount());
    
    Collections.sort(listWidget.getItems(), new Comparator<TestModel>() {
      @Override
      public int compare(TestModel o1, TestModel o2) {
        return o1.getId() - o2.getId();
      }
    });
    
    assertItemsRendered(listWidget);
  }
  
  @Test
  public void testBindableListIteratorAdd() {
    List<TestModel> modelList = new ArrayList<TestModel>();
    modelList.add(new TestModel(0, "0"));
    modelList.add(new TestModel(1, "1"));
    modelList.add(new TestModel(3, "3"));
    
    BindingTemplateTestApp app = IOC.getBeanManager().lookupBean(BindingTemplateTestApp.class).getInstance();
    BindingListWidget listWidget = app.getListWidget();
    listWidget.setItems(modelList);
    assertEquals("Expected three widgets", 3, listWidget.getWidgetCount());
    
    ListIterator<TestModel> iter = listWidget.getValue().listIterator();
    iter.next();
    iter.next();
    iter.add(new TestModel(2, "2"));
    
    assertItemsRendered(listWidget);
  }
  
  @Test
  public void testBindableListIteratorRemove() {
    List<TestModel> modelList = new ArrayList<TestModel>();
    modelList.add(new TestModel(0, "0"));
    modelList.add(new TestModel(1, "1"));
    modelList.add(new TestModel(5, "5"));
    modelList.add(new TestModel(2, "2"));
    modelList.add(new TestModel(3, "3"));
    
    BindingTemplateTestApp app = IOC.getBeanManager().lookupBean(BindingTemplateTestApp.class).getInstance();
    BindingListWidget listWidget = app.getListWidget();
    listWidget.setItems(modelList);
    assertEquals("Expected three widgets", 5, listWidget.getWidgetCount());
    
    ListIterator<TestModel> iter = listWidget.getValue().listIterator();
    for (int i = 0; i < 3; i++) {
      iter.next();
    }
    iter.remove();
    
    assertItemsRendered(listWidget);
  }

  private void assertItemsRendered(ListWidget<TestModel, BindingItemWidget> listWidget) {
    assertEquals("Expected exactly four widgets", 4, listWidget.getWidgetCount());

    BindingItemWidget itemWidget0 = listWidget.getWidget(0);
    BindingItemWidget itemWidget1 = listWidget.getWidget(1);
    BindingItemWidget itemWidget2 = listWidget.getWidget(2);
    BindingItemWidget itemWidget3 = listWidget.getWidget(3);
    assertEquals("First item widget was not rendered correctly!", "0", itemWidget0.getTextBox().getText());
    assertEquals("Second item widget was not rendered correctly!", "1", itemWidget1.getTextBox().getText());
    assertEquals("Third item widget was not rendered correctly!", "2", itemWidget2.getTextBox().getText());
    assertEquals("Fourth item widget was not rendered correctly!", "3", itemWidget3.getTextBox().getText());

    TestModel model0 = listWidget.getValue().get(0);
    TestModel model1 = listWidget.getValue().get(1);
    TestModel model2 = listWidget.getValue().get(2);
    TestModel model3 = listWidget.getValue().get(3);
    assertTrue("First item was not proxied!", model0 instanceof BindableProxy);
    assertTrue("Second item was not proxied!", model1 instanceof BindableProxy);
    assertTrue("Third item was not proxied!", model2 instanceof BindableProxy);
    assertTrue("Fourth item was not proxied!", model3 instanceof BindableProxy);

    assertSame("First item and widget model are not same proxy object!", itemWidget0.getModel(), model0);
    assertSame("Second item and widget model are not same proxy object!", itemWidget1.getModel(), model1);
    assertSame("Third item and widget model are not same proxy object!", itemWidget2.getModel(), model2);
    assertSame("Fourth item and widget model are not same proxy object!", itemWidget3.getModel(), model3);
  }

}
