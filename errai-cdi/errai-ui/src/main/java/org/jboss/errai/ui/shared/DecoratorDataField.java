package org.jboss.errai.ui.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ui.shared.api.annotations.DataField;

import com.google.common.base.Strings;
import com.google.gwt.dom.client.Element;

/**
 * Store all injected {@link DataField} {@link Statement} instances into the
 * aggregate {@link Map} for this composite {@link Template}.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@CodeDecorator
public class DecoratorDataField extends IOCDecoratorExtension<DataField> {

  public DecoratorDataField(Class<DataField> decoratesWith) {
    super(decoratesWith);
  }

  @Override
  public List<? extends Statement> generateDecorator(InjectableInstance<DataField> ctx) {

    Statement instance = ctx.getValueStatement();
    if(ctx.getType().isAssignableTo(Element.class))
    {
      instance = ObjectBuilder.newInstanceOf(ElementWrapperWidget.class).withParameters(instance);
    }
    String name = getTemplateDataFieldName(ctx.getAnnotation(), ctx.getMemberName());
    saveDataField(ctx, name, instance);

    return new ArrayList<Statement>();

  }

  private void saveDataField(InjectableInstance<DataField> ctx, String name, Statement instance) {
    Map<String, Statement> map = dataFieldMap(ctx, ctx.getInjector().getEnclosingType());
    map.put(name, instance);
    System.out.println("Saving data-field [" + name + "] Statment instance [" + instance + "]");
  }

  private String getTemplateDataFieldName(DataField annotation, String deflt) {
    String value = Strings.nullToEmpty(annotation.value()).trim();
    return value.isEmpty() ? deflt : value;
  }

  /**
   * Get the map of {@link DataField} names and {@link Statement} instances.
   */
  @SuppressWarnings("unchecked")
  public static Map<String, Statement> dataFieldMap(InjectableInstance<?> ctx, MetaClass templateType) {
    String dataFieldMapName = dataFieldMapName(templateType);

    Map<String, Statement> dataFields = (Map<String, Statement>) ctx.getInjectionContext().getAttribute(
            dataFieldMapName);
    if (dataFields == null) {
      dataFields = new HashMap<String, Statement>();
      ctx.getInjectionContext().setAttribute(dataFieldMapName, dataFields);
    }

    return dataFields;
  }

  /**
   * Using the given composite {@link Template} type, return the name of the map
   * of {@link DataField} names and variable {@link Statement} instances for
   * that type.
   */
  public static final String dataFieldMapName(MetaClass composite) {
    return DecoratorDataField.class.getName() + "_DATA_FIELD_MAP_" + composite.getName();
  }

}