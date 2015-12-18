package org.jboss.errai.ui.test.form.client;

import static org.jboss.errai.ioc.client.container.IOC.*;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ui.client.widget.AbstractForm;
import org.jboss.errai.ui.test.form.client.res.TestFormWidget;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;

public class AbstractFormTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ui.test.form.Test";
  }

  public void testFormAddsHiddenIFrame() throws Exception {
    createTestFormWidget();
    assertNotNull(Document.get().getElementById(AbstractForm.ERRAI_FORM_FRAME_ID));
  }

  public void testMultipleFormsOnlyAddOneHiddenIFrame() throws Exception {
    // Should create one iframe
    createTestFormWidget();
    
    NodeList<Element> iFrames = Document.get().getElementsByTagName("iframe");
    final int initialNumberOfIFrames = iFrames.getLength();

    // Should not create another iframe
    createTestFormWidget();

    assertEquals(initialNumberOfIFrames, iFrames.getLength());
  }

  private TestFormWidget createTestFormWidget() {
    return getBeanManager().lookupBean(TestFormWidget.class).getInstance();
  }
}
