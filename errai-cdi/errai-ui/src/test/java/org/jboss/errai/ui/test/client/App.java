package org.jboss.errai.ui.test.client;

import java.util.ArrayList;
import java.util.List;

import java_cup.parser;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.shared.Insert;
import org.jboss.errai.ui.shared.Visit;
import org.jboss.errai.ui.shared.VisitContext;
import org.jboss.errai.ui.shared.Visitor;

import com.google.gwt.dom.client.Element;
import com.google.gwt.resources.client.TextResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

@EntryPoint
public class App {

  @Insert
  private PageX page;

  @Inject
  private RootPanel root;

  private TextResource template;

  @PostConstruct
  public void setup() {
    page = new PageX();
    template = TemplateResource.INSTANCE.getTemplate();
    final TemplateComponent component = new TemplateComponent();
    component.setContent(new Label());
    component.init();

    final List<Element> dataFields = new ArrayList<Element>();

    System.out.println(template.getText());
    final Element parserDiv = DOM.createDiv();
    parserDiv.setInnerHTML(template.getText());
    Element templateRoot = parserDiv.getFirstChildElement();

    // TODO do this as browser split deferred binding using
    // Document.querySelectorAll() -
    // https://developer.mozilla.org/En/DOM/Element.querySelectorAll

    Visit.accept(templateRoot, new Visitor() {
      @Override
      public void visit(VisitContext context, Element element) {
        if (element.hasAttribute("data-field")) {
          dataFields.add(element);
        }
      }
    });

    for (Element element : dataFields) {
      Element parentElement = element.getParentElement();
      System.out.println("Binding [data-field="
              + element.getAttribute("data-field") + "]");

      Label label = component.getContent();
      parentElement.replaceChild(label.getElement(), element);
      System.out.println(templateRoot.getInnerHTML());
    }

    root.getElement().appendChild(templateRoot);
    System.out.println(root.getElement().getInnerHTML());
  }

  public TextResource getTemplate() {
    return template;
  }
}
