package org.jboss.errai.ui.nav.rebind;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.exception.GenerationException;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.Transition;
import org.jboss.errai.ui.rebind.DecoratorDataField;
import org.jboss.errai.ui.rebind.DecoratorTemplated;
import org.jboss.errai.ui.shared.api.annotations.DataField;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;

/**
 * Handles {@link Transition} decorations.
 *
 * @author eric.wittmann@redhat.com
 */
@CodeDecorator
public class DecoratorTransition extends IOCDecoratorExtension<Transition> {

  private static final Logger logger = Logger.getLogger(DecoratorTransition.class.getName());

  /**
   * Constructor.
   *
   * @param decoratesWith
   */
  public DecoratorTransition(Class<Transition> decoratesWith) {
    super(decoratesWith);
  }

  /**
   * @see org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension#generateDecorator(org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance)
   */
  @Override
  public List<? extends Statement> generateDecorator(
          InjectableInstance<Transition> instance) {
    Class<? extends Widget> transitionTargetClass = instance.getAnnotation()
            .value();
    Page transitionTargetPage = transitionTargetClass.getAnnotation(Page.class);
    DataField dataField = instance.getField().getAnnotation(DataField.class);

    // Can only transition to classes annotated with @Page
    if (transitionTargetPage == null) {
      throw new GenerationException("@Transition ["
              + instance.getEnclosingType().getFullyQualifiedName() + "."
              + instance.getField().getName() + "] targets class ["
              + transitionTargetClass.getSimpleName()
              + "] which is not annotated by @Page");
    }
    // Currently only support Anchors as transitions
    if (!instance.getType().isAssignableTo(Anchor.class)) {
      throw new GenerationException(
              "@Transition ["
                      + transitionTargetClass.getSimpleName()
                      + "] applies to field ["
                      + instance.getField().getName()
                      + "] of class["
                      + instance.getEnclosingType().getFullyQualifiedName()
                      + "] but the field type is *not* com.google.gwt.user.client.ui.Anchor");
    }
    // The field must also be a data field from errai-ui templating
    if (dataField == null) {
      throw new GenerationException("@Transition ["
              + instance.getEnclosingType().getFullyQualifiedName() + "."
              + instance.getField().getName()
              + "] must also be annotated with @DataField");
    }

    String transitionTargetPageName = transitionTargetPage.path().equals("") ? transitionTargetClass
            .getName() : transitionTargetPage.path();
    String historyToken = "#" + transitionTargetPageName;
    logger.info("Setting @Transition annotated Anchor ["
            + instance.getEnclosingType().getFullyQualifiedName() + "."
            + instance.getField().getName() + "]'s href attribute to ["
            + historyToken + "].");

    String dataFieldName = DecoratorDataField.getTemplateDataFieldName(
            dataField, instance.getMemberName());

    DecoratorTemplated.setAttributeOverride(instance, dataFieldName, "href",
            historyToken);
    return Collections.emptyList();
  }

}