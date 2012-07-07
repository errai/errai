package org.jboss.errai.ui.shared;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.util.TypeLiteral;

import org.jboss.errai.codegen.Cast;
import org.jboss.errai.codegen.InnerClass;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.Variable;
import org.jboss.errai.codegen.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.ElseBlockBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaConstructor;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.meta.impl.build.BuildMetaClass;
import org.jboss.errai.codegen.util.Bool;
import org.jboss.errai.codegen.util.PrivateAccessType;
import org.jboss.errai.codegen.util.PrivateAccessUtil;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.client.container.InitializationCallback;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil.BeanMetric;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ClientBundle.Source;
import com.google.gwt.resources.client.TextResource;
import com.google.gwt.user.client.ui.Widget;

/**
 * Generates the code required for {@link Templated} classes.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@CodeDecorator
public class DecoratorTemplated extends IOCDecoratorExtension<Templated> {
  private static final String CONSTRUCTED_TEMPLATE_SET_KEY = "constructedTemplate";

  public DecoratorTemplated(Class<Templated> decoratesWith) {
    super(decoratesWith);
  }

  @Override
  public List<? extends Statement> generateDecorator(InjectableInstance<Templated> ctx) {

    MetaClass declaringClass = ctx.getEnclosingType();

    for (MetaField field : declaringClass.getFields()) {
      if (field.isAnnotationPresent(DataField.class)
              || field.getType().getErased().equals(MetaClassFactory.get(DataBinder.class))) {
        ctx.getInjectionContext().addExposedField(field, PrivateAccessType.Both);
      }
    }

    MetaClass callbackMetaClass = MetaClassFactory.parameterizedAs(InitializationCallback.class,
            MetaClassFactory.typeParametersOf(declaringClass));
    BlockBuilder<AnonymousClassStructureBuilder> builder = ObjectBuilder.newInstanceOf(callbackMetaClass).extend()
            .publicOverridesMethod("init", Parameter.of(declaringClass, "obj"));

    /*
     * Do the work
     */
    generateTemplatedInitialization(ctx, builder);

    return Collections.singletonList(Stmt.loadVariable("context").invoke("addInitializationCallback",
            Refs.get(ctx.getInjector().getVarName()), builder.finish().finish()));
  }

  /**
   * Generate the actual construction logic for our {@link Templated} component
   */
  @SuppressWarnings("serial")
  private void generateTemplatedInitialization(InjectableInstance<Templated> ctx,
          BlockBuilder<AnonymousClassStructureBuilder> builder) {

    Map<MetaClass, BuildMetaClass> constructed = getConstructedTemplateTypes(ctx);

    MetaClass declaringClass = ctx.getEnclosingType();
    if (!constructed.containsKey(declaringClass)) {

      /*
       * Generate this component's ClientBundle resource if necessary
       */
      generateTemplateResourceInterface(ctx, declaringClass);

      /*
       * Instantiate the ClientBundle Template resource
       */
      String templateVarName = InjectUtil.getUniqueVarName();
      builder.append(Stmt
              .declareVariable(getConstructedTemplateTypes(ctx).get(declaringClass))
              .named(templateVarName)
              .initializeWith(
                      Stmt.invokeStatic(GWT.class, "create", getConstructedTemplateTypes(ctx).get(declaringClass))));

      /*
       * Get root Template Element
       */
      String rootTemplateElementVarName = InjectUtil.getUniqueVarName();
      builder.append(Stmt
              .declareVariable(Element.class)
              .named(rootTemplateElementVarName)
              .initializeWith(
                      Stmt.invokeStatic(TemplateUtil.class, "getRootTemplateElement", Stmt
                              .loadVariable(templateVarName).invoke("getContents").invoke("getText"),
                              getTemplateFragmentName(declaringClass))));

      Statement rootTemplateElement = Stmt.loadVariable(rootTemplateElementVarName);

      /*
       * Get a reference to the actual Composite component being created
       */
      Statement component = Refs.get(ctx.getInjector().getVarName());

      /*
       * Get all of the data-field Elements from the Template
       */
      String dataFieldElementsVarName = InjectUtil.getUniqueVarName();
      builder.append(Stmt.declareVariable(dataFieldElementsVarName, new TypeLiteral<Map<String, Element>>() {
          }, Stmt.invokeStatic(TemplateUtil.class, "getDataFieldElements", rootTemplateElement)));

      /*
       * Attach Widget field children Elements to the Template DOM
       */
      generateComponentCompositions(ctx, builder, component, rootTemplateElement,
              Stmt.loadVariable(dataFieldElementsVarName));
    }
  }

  @SuppressWarnings("serial")
  private void generateComponentCompositions(InjectableInstance<Templated> ctx,
          BlockBuilder<AnonymousClassStructureBuilder> builder, Statement component, Statement rootTemplateElement,
          Statement dataFieldElements) {

    String fieldsVarName = InjectUtil.getUniqueVarName();
    builder.append(Stmt.declareVariable(fieldsVarName, new TypeLiteral<Collection<Widget>>() {
        }, Stmt.newObject(new TypeLiteral<ArrayList<Widget>>() {
        })));

    /*
     * In case of constructor injection, search for the data binder parameter 
     */
    Statement dataBinderRef = null;
    BeanMetric beanMetric = InjectUtil.analyzeBean(ctx.getInjectionContext(), ctx.getEnclosingType());
    MetaConstructor mc = beanMetric.getInjectorConstructor();
    if (mc != null) {
      for (MetaParameter mp : mc.getParameters()) {
        if (mp.getType().getErased().isAssignableTo(MetaClassFactory.get(DataBinder.class))) {
          dataBinderRef = ctx.getInjectionContext().getInlineBeanReference(mp);  
          break;
        }
      }
    }
    
    /*
     * Search for data binder fields, in case no data binder was injected into the constructor
     */
    if (dataBinderRef == null) {
      MetaField dataBinderField = null;
      for (MetaField field : ctx.getInjector().getInjectedType().getFields()) {
        if (field.getType().getErased().equals(MetaClassFactory.get(DataBinder.class))) {
          if (dataBinderField != null) {
            System.out.println("Multiple data binders found in class:" + ctx.getInjector().getInjectedType()
                    + " - skipping automatic binding generation!");
            dataBinderField = null;
            break;
          }
          dataBinderField = field;
          dataBinderRef = Stmt.invokeStatic(ctx.getInjectionContext().getProcessingContext().getBootstrapClass(),
              PrivateAccessUtil.getPrivateFieldInjectorName(dataBinderField),
              Variable.get(ctx.getInjector().getVarName()));
        }
      }
    }
    
    /*
     * Create a reference to the composite's data binder
     */
    if (dataBinderRef != null) {
      builder.append(Stmt.declareVariable("binder", DataBinder.class, dataBinderRef));
    }

    /*
     * Merge each field's Widget Element into the DOM in place of the
     * corresponding data-field
     */
    Map<String, Statement> dataFields = DecoratorDataField.aggregateDataFieldMap(ctx, ctx.getType());
    for (Entry<String, Statement> field : dataFields.entrySet()) {
      builder.append(Stmt.invokeStatic(TemplateUtil.class, "compositeComponentReplace", ctx.getType()
              .getFullyQualifiedName(), getTemplateFileName(ctx.getType()), Cast.to(Widget.class, field.getValue()),
              dataFieldElements, field.getKey()));
    }

    /*
     * Add each field to the Collection of children of the new Composite
     * Template
     */
    for (Entry<String, Statement> field : dataFields.entrySet()) {
      builder.append(Stmt.loadVariable(fieldsVarName).invoke("add", field.getValue()));
    }

    /*
     * Bind each widget if data-binder is found and has been initialized. 
     * TODO this should really only bind if the developer has have asked it to be bound.
     */
    if (dataBinderRef != null) {
      BlockBuilder<ElseBlockBuilder> binderBlockBuilder = Stmt.if_(Bool.isNotNull(Variable.get("binder")));
      for (Entry<String, Statement> field : dataFields.entrySet()) {
        binderBlockBuilder.append(Stmt.loadVariable("binder").invoke("bind", field.getValue(), field.getKey()));
      }
      builder.append(binderBlockBuilder
          .finish()
          .else_()
          .append(
              Stmt.invokeStatic(GWT.class, "log", "DataBinder in class " 
                  + ctx.getEnclosingType().getFullyQualifiedName()
                  + " has not been initialized - skipping automatic binding!"))
          .finish());
    }

    /*
     * Attach the Template to the Component, and set up the GWT Widget hierarchy
     * to preserve Handlers and DOM events.
     */
    builder.append(Stmt.invokeStatic(TemplateUtil.class, "initWidget", component, rootTemplateElement,
            Stmt.loadVariable(fieldsVarName)));

  }

  /**
   * Create an inner interface for the given {@link Template} class and its HTML corresponding resource
   */
  private void generateTemplateResourceInterface(InjectableInstance<Templated> ctx, final MetaClass type) {
    ClassStructureBuilder<?> componentTemplateResource = ClassBuilder.define(getTemplateTypeName(type)).publicScope()
            .interfaceDefinition().implementsInterface(Template.class).implementsInterface(ClientBundle.class).body()
            .publicMethod(TextResource.class, "getContents").annotatedWith(new Source() {

              @Override
              public Class<? extends Annotation> annotationType() {
                return Source.class;
              }

              @Override
              public String[] value() {
                return new String[] { getTemplateFileName(type) };
              }

            }).finish();

    ctx.getInjectionContext().getProcessingContext().getBootstrapClass()
            .addInnerClass(new InnerClass(componentTemplateResource.getClassDefinition()));

    getConstructedTemplateTypes(ctx).put(type, componentTemplateResource.getClassDefinition());
  }

  /**
   * Get a map of all previously constructed {@link Template} object types
   */
  @SuppressWarnings("unchecked")
  private Map<MetaClass, BuildMetaClass> getConstructedTemplateTypes(InjectableInstance<Templated> ctx) {
    Map<MetaClass, BuildMetaClass> result = (Map<MetaClass, BuildMetaClass>) ctx.getInjectionContext().getAttribute(
            CONSTRUCTED_TEMPLATE_SET_KEY);

    if (result == null) {
      result = new LinkedHashMap<MetaClass, BuildMetaClass>();
      ctx.getInjectionContext().setAttribute(CONSTRUCTED_TEMPLATE_SET_KEY, result);
    }

    return result;
  }

  /*
   * Non-generation utility methods.
   */

  /**
   * Get the name of the {@link Template} class of the given {@link MetaClass} type
   */
  private String getTemplateTypeName(MetaClass type) {
    return type.getFullyQualifiedName().replaceAll("\\.", "_") + "TemplateResource";
  }

  /**
   * Get the name of the {@link Template} HTML file of the given {@link MetaClass} component type
   */
  private String getTemplateFileName(MetaClass type) {
    String resource = type.getFullyQualifiedName().replaceAll("\\.", "/") + ".html";

    if (type.isAnnotationPresent(Templated.class)) {
      String source = canonicalizeTemplateSourceSyntax(type, type.getAnnotation(Templated.class).value());
      Matcher matcher = Pattern.compile("^([^#]+)#?.*$").matcher(source);
      if (matcher.matches()) {
        resource = (matcher.group(1) == null ? resource : matcher.group(1));
        if (resource.matches("\\S+\\.html")) {
          resource = type.getPackageName().replaceAll("\\.", "/") + "/" + resource;
        }
      }
    }

    return resource;
  }

  /**
   * Get the name of the {@link Template} HTML fragment (Element subtree) to be used as the template root of the given
   * {@link MetaClass} component type
   */
  private String getTemplateFragmentName(MetaClass type) {
    String fragment = "";

    if (type.isAnnotationPresent(Templated.class)) {
      String source = canonicalizeTemplateSourceSyntax(type, type.getAnnotation(Templated.class).value());
      Matcher matcher = Pattern.compile("^.*#([^#]+)$").matcher(source);
      if (matcher.matches()) {
        fragment = (matcher.group(1) == null ? fragment : matcher.group(1));
      }
    }

    return fragment;
  }

  /**
   * Throw an exception if the template source syntax is invalid
   */
  private String canonicalizeTemplateSourceSyntax(MetaClass component, String source) {
    String result = Strings.nullToEmpty(source).trim();

    if (result.matches(".*#.*#.*")) {
      throw new IllegalArgumentException("Invalid syntax: @" + Templated.class.getSimpleName() + "(" + source
              + ") on component " + component.getFullyQualifiedName()
              + ". Multiple '#' found, where only one fragment is permitted.");
    }

    return result;
  }

}