package org.jboss.errai.ui.shared;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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
    ctx.ensureMemberExposed();
    Statement instance = ctx.getValueStatement();
    if (ctx.getType().isAssignableTo(Element.class)) {
      instance = ObjectBuilder.newInstanceOf(ElementWrapperWidget.class).withParameters(instance);
    }
    String name = getTemplateDataFieldName(ctx.getAnnotation(), ctx.getMemberName());
    saveDataField(ctx, ctx.getType(), name, instance);

    return new ArrayList<Statement>();

  }

  private void saveDataField(InjectableInstance<DataField> ctx, MetaClass type, String name, Statement instance) {
    dataFieldMap(ctx, ctx.getEnclosingType()).put(name, instance);
    dataFieldTypeMap(ctx, ctx.getEnclosingType()).put(name, type);
  }

  private String getTemplateDataFieldName(DataField annotation, String deflt) {
    String value = Strings.nullToEmpty(annotation.value()).trim();
    return value.isEmpty() ? deflt : value;
  }

  /**
   * Get the map of {@link DataField} names and {@link Statement} instances.
   */
  @SuppressWarnings("unchecked")
  private static Map<String, Statement> dataFieldMap(InjectableInstance<?> ctx, MetaClass templateType) {
    String dataFieldMapName = dataFieldMapName(templateType);

    Map<String, Statement> dataFields = (Map<String, Statement>) ctx.getInjectionContext().getAttribute(
            dataFieldMapName);
    if (dataFields == null) {
      dataFields = new LinkedHashMap<String, Statement>();
      ctx.getInjectionContext().setAttribute(dataFieldMapName, dataFields);
    }

    return dataFields;
  }

  /**
   * Get the map of {@link DataField} names and {@link MetaClass} types.
   */
  @SuppressWarnings("unchecked")
  private static Map<String, MetaClass> dataFieldTypeMap(InjectableInstance<?> ctx, MetaClass templateType) {
    String dataFieldTypeMapName = dataFieldTypeMapName(templateType);

    Map<String, MetaClass> dataFieldTypes = (Map<String, MetaClass>) ctx.getInjectionContext().getAttribute(
            dataFieldTypeMapName);
    if (dataFieldTypes == null) {
      dataFieldTypes = new LinkedHashMap<String, MetaClass>();
      ctx.getInjectionContext().setAttribute(dataFieldTypeMapName, dataFieldTypes);
    }

    return dataFieldTypes;
  }

  /**
   * Get the aggregate map of {@link DataField} names and {@link Statement}
   * instances for the given {@link MetaClass} type and all ancestors returned
   * by {@link MetaClass#getSuperClass()}.
   */
  @SuppressWarnings("unchecked")
  public static Map<String, Statement> aggregateDataFieldMap(InjectableInstance<?> ctx, MetaClass componentType) {

    Map<String, Statement> result = new LinkedHashMap<String, Statement>();

    if (componentType.getSuperClass() != null) {
      result.putAll(aggregateDataFieldMap(ctx, componentType.getSuperClass()));
    }

    Map<String, Statement> dataFields = (Map<String, Statement>) ctx.getInjectionContext().getAttribute(
            dataFieldMapName(componentType));
    if (dataFields != null) {
      result.putAll(dataFields);
    }

    return result;
  }

  /**
   * Get the aggregate map of {@link DataField} names and {@link MetaClass}
   * types for the given {@link MetaClass} component type and all ancestors
   * returned by {@link MetaClass#getSuperClass()}.
   */
  @SuppressWarnings("unchecked")
  public static Map<String, MetaClass> aggregateDataFieldTypeMap(InjectableInstance<?> ctx, MetaClass componentType) {

    Map<String, MetaClass> result = new LinkedHashMap<String, MetaClass>();

    if (componentType.getSuperClass() != null) {
      result.putAll(aggregateDataFieldTypeMap(ctx, componentType.getSuperClass()));
    }

    Map<String, MetaClass> dataFields = (Map<String, MetaClass>) ctx.getInjectionContext().getAttribute(
            dataFieldTypeMapName(componentType));
    if (dataFields != null) {
      result.putAll(dataFields);
    }

    return result;
  }

  /**
   * Using the given composite {@link Template} type, return the name of the map
   * of {@link DataField} names and variable {@link Statement} instances.
   */
  private static final String dataFieldMapName(MetaClass composite) {
    return DecoratorDataField.class.getName() + "_DATA_FIELD_MAP_" + composite.getName();
  }

  /**
   * Using the given composite {@link Template} type, return the name of the map
   * of {@link DataField} names and variable {@link MetaClass} types.
   */
  private static final String dataFieldTypeMapName(MetaClass composite) {
    return DecoratorDataField.class.getName() + "_DATA_FIELD_TYPE_MAP_" + composite.getName();
  }

}