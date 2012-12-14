/*
 * Copyright 2012 JBoss, by Red Hat, Inc
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
package org.jboss.errai.ui.rebind;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
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
import org.jboss.errai.codegen.exception.GenerationException;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaConstructor;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.meta.MetaParameterizedType;
import org.jboss.errai.codegen.meta.MetaType;
import org.jboss.errai.codegen.meta.impl.build.BuildMetaClass;
import org.jboss.errai.codegen.meta.impl.java.JavaReflectionClass;
import org.jboss.errai.codegen.util.If;
import org.jboss.errai.codegen.util.PrivateAccessType;
import org.jboss.errai.codegen.util.PrivateAccessUtil;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.rebind.DataBindingValidator;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.client.container.InitializationCallback;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil.BeanMetric;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ui.shared.Template;
import org.jboss.errai.ui.shared.TemplateUtil;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.SinkNative;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.DomEvent.Type;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ClientBundle.Source;
import com.google.gwt.resources.client.TextResource;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Composite;
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
  
  private static final Logger logger = Logger.getLogger(DecoratorTemplated.class.getName());


  public DecoratorTemplated(Class<Templated> decoratesWith) {
    super(decoratesWith);
  }

  @Override
  public List<? extends Statement> generateDecorator(InjectableInstance<Templated> ctx) {

    MetaClass declaringClass = ctx.getEnclosingType();

    if (!declaringClass.isAssignableTo(Composite.class)) {
      throw new GenerationException("@Templated class [" + declaringClass.getFullyQualifiedName()
          + "] must extend base class [" + Composite.class.getName() + "].");
    }

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
        Refs.get(ctx.getInjector().getInstanceVarName()), builder.finish().finish()));
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
      builder
          .append(Stmt
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
      Statement component = Refs.get(ctx.getInjector().getInstanceVarName());

      /*
       * Get all of the data-field Elements from the Template
       */
      String dataFieldElementsVarName = InjectUtil.getUniqueVarName();
      builder.append(Stmt.declareVariable(dataFieldElementsVarName, new TypeLiteral<Map<String, Element>>() {
      },
          Stmt.invokeStatic(TemplateUtil.class, "getDataFieldElements", rootTemplateElement)));

      /*
       * Attach Widget field children Elements to the Template DOM
       */

      String fieldsMapVarName = InjectUtil.getUniqueVarName();

      /*
       * The Map<String, Widget> to store actual component field references.
       */
      builder.append(Stmt.declareVariable(fieldsMapVarName, new TypeLiteral<Map<String, Widget>>() {
      },
          Stmt.newObject(new TypeLiteral<LinkedHashMap<String, Widget>>() {
          })));
      Statement fieldsMap = Stmt.loadVariable(fieldsMapVarName);
      
      generateComponentCompositions(ctx, builder, component, rootTemplateElement,
          Stmt.loadVariable(dataFieldElementsVarName), fieldsMap);

      generateEventHandlerMethodClasses(ctx, builder, component, dataFieldElementsVarName, fieldsMap);
    }
  }

  private void generateEventHandlerMethodClasses(InjectableInstance<Templated> ctx,
                                                 BlockBuilder<AnonymousClassStructureBuilder> builder, Statement component,
                                                 String dataFieldElementsVarName, Statement fieldsMap) {

    Map<String, MetaClass> dataFieldTypes = DecoratorDataField.aggregateDataFieldTypeMap(ctx, ctx.getType());
    dataFieldTypes.put("this", ctx.getType());
    
    MetaClass declaringClass = ctx.getEnclosingType();

    /* Ensure that no @DataFields are handled more than once when used in combination with @SyncNative */
    Set<String> processedNativeHandlers = new HashSet<String>();
    Set<String> processedEventHandlers = new HashSet<String>();

    for (MetaMethod method : declaringClass.getMethodsAnnotatedWith(EventHandler.class)) {

      String[] targetDataFieldNames = method.getAnnotation(EventHandler.class).value();

      if (targetDataFieldNames.length == 0) {
        throw new GenerationException("@EventHandler annotation on method ["
            + declaringClass.getFullyQualifiedName()
            + "." + method.getName() + "] must specify at least one data-field target.");
      }
      
      MetaClass eventType = (method.getParameters().length == 1) ? method.getParameters()[0].getType() : null;
      if (eventType == null || (!eventType.isAssignableTo(Event.class)) && !eventType.isAssignableTo(DomEvent.class)) {
        throw new GenerationException("@EventHandler method [" + method.getName() + "] in class ["
            + declaringClass.getFullyQualifiedName()
            + "] must have exactly one parameter of a type extending either ["
            + DomEvent.class.getName() + "] or [" + NativeEvent.class.getName() + "]." );
      }

      if (eventType.isAssignableTo(Event.class)) {
        /*
         * Generate native DOM event handlers.
         */
        MetaClass handlerType = MetaClassFactory.get(EventListener.class);
        BlockBuilder<AnonymousClassStructureBuilder> listenerBuiler = ObjectBuilder.newInstanceOf(handlerType)
            .extend()
            .publicOverridesMethod(handlerType.getMethods()[0].getName(), Parameter.of(eventType, "event"));
        listenerBuiler.append(InjectUtil.invokePublicOrPrivateMethod(ctx.getInjectionContext(), component,
            method, Stmt.loadVariable("event")));

        ObjectBuilder listenerInstance = listenerBuiler.finish().finish();

        int eventsToSink = Event.FOCUSEVENTS | Event.GESTUREEVENTS | Event.KEYEVENTS | Event.MOUSEEVENTS
            | Event.TOUCHEVENTS;
        if (method.isAnnotationPresent(SinkNative.class)) {
          eventsToSink = method.getAnnotation(SinkNative.class).value();
        }

        for (String name : targetDataFieldNames) {

          if (processedNativeHandlers.contains(name) || processedEventHandlers.contains(name)) {
            throw new GenerationException(
                "Cannot specify more than one @EventHandler method when @SyncNative is used for data-field ["
                    + name + "] in class ["
                    + declaringClass.getFullyQualifiedName()
                    + "].");
          }
          else {
            processedNativeHandlers.add(name);
          }

          if (dataFieldTypes.containsKey(name)) {
            MetaClass dataFieldType = dataFieldTypes.get(name);
            if (!dataFieldType.isAssignableTo(Element.class)) {
              /*
               * We have a GWT or other Widget type.
               */
              throw new GenerationException("@DataField [" + name + "] of type [" + dataFieldType.getName()
                  + "] in class [" + declaringClass.getFullyQualifiedName() + "] is not assignable to ["
                  + Element.class.getName() + "] specified by @EventHandler method " + method.getName()
                  + "("
                  + eventType.getName() + ")]");
            }
            else {
              /*
               * We have a wrapped native Element reference
               */
              throw new GenerationException("Cannot attach native DOM events to @DataField [" + name
                  + "] of type ["
                  + dataFieldType.getName() + "] in class [" + declaringClass.getFullyQualifiedName()
                  + "] specified by @EventHandler method " + method.getName() + "(" + eventType.getName()
                  + ")] - Use the corresponding GWT 'EventHandler' types instead.");
            }
          }
          else {
            /*
             * We are completely native and have no reference to this data-field
             * Element in Java
             */
            builder.append(Stmt.invokeStatic(TemplateUtil.class, "setupNativeEventListener", component,
                Stmt.loadVariable(dataFieldElementsVarName).invoke("get", name), listenerInstance,
                eventsToSink));
          }
        }
      }
      else {
        /*
         * We have a GWT Widget type
         */
        MetaClass handlerType;
        try {
          handlerType = getHandlerForEvent(eventType);
        }
        catch (GenerationException e) {
          /*
           *  see ERRAI-373 for details on this crazy inference (without this message, the cause of the
           *  problem is nearly impossible to diagnose)
           */
          if (declaringClass.getClass() == JavaReflectionClass.class) {
            throw new GenerationException(
                "The type " + declaringClass.getFullyQualifiedName() + " looks like a client-side" +
                    " @Templated class, but it is not known to GWT. This probably means that " +
                    declaringClass.getName() + " or one of its supertypes contains non-translatable code.", e);
          }
          throw e;
        }

        BlockBuilder<AnonymousClassStructureBuilder> listenerBuiler = ObjectBuilder.newInstanceOf(handlerType)
            .extend()
            .publicOverridesMethod(handlerType.getMethods()[0].getName(), Parameter.of(eventType, "event"));


        listenerBuiler.append(InjectUtil.invokePublicOrPrivateMethod(ctx.getInjectionContext(),
            component, method, Stmt.loadVariable("event")));

        ObjectBuilder listenerInstance = listenerBuiler.finish().finish();

        MetaClass hasHandlerType = MetaClassFactory.get("com.google.gwt.event.dom.client.Has"
            + handlerType.getName()
            + "s");

        for (String name : targetDataFieldNames) {
          MetaClass dataFieldType = dataFieldTypes.get(name);
          
          if (dataFieldType == null) {
            throw new GenerationException("@EventHandler method [" + method.getName() + "] in class ["
                + declaringClass.getFullyQualifiedName()
                + "] handles a GWT event type but the specified @DataField [" + name + "] was not found.");
          }

          if (processedNativeHandlers.contains(name)) {
            throw new GenerationException(
                "Cannot specify more than one @EventHandler method when @SyncNative is used for data-field ["
                    + name + "] in class [" + declaringClass.getFullyQualifiedName()
                    + "].");
          }

          processedEventHandlers.add(name);

          // Where will the event come from? It could be a @DataField member, or it could be the templated widget itself!
          Statement eventSource;
          if ("this".equals(name)) {
            eventSource = Stmt.loadVariable("obj");
          }
          else {
            eventSource = Stmt.nestedCall(fieldsMap).invoke("get", name);
          }
          
          if (dataFieldType.isAssignableTo(Element.class)) {
            builder.append(Stmt.invokeStatic(TemplateUtil.class, "setupWrappedElementEventHandler", component,
                eventSource, listenerInstance,
                Stmt.invokeStatic(eventType, "getType")));
          }
          else if (dataFieldType.isAssignableTo(hasHandlerType)) {
            Statement widget = Cast.to(hasHandlerType, eventSource);
            builder.append(Stmt.nestedCall(widget).invoke("add" + handlerType.getName(),
                Cast.to(handlerType, listenerInstance)));
          }
          else if (dataFieldType.isAssignableTo(Widget.class)) {
            Statement widget = Cast.to(Widget.class, eventSource);
            builder.append(Stmt.nestedCall(widget).invoke("addDomHandler",
                listenerInstance, Stmt.invokeStatic(eventType, "getType")));
          }
          else {
            throw new GenerationException("@DataField [" + name + "] of type [" + dataFieldType.getName()
                + "] in class [" + declaringClass.getFullyQualifiedName()
                + "] does not implement required interface [" + hasHandlerType.getName()
                + "] specified by @EventHandler method " + method.getName() + "(" + eventType.getName()
                + ")]");
          }
        }
      }
    }
  }

  private MetaClass getHandlerForEvent(MetaClass eventType) {

    /*
     * All handlers event must have an overrided method getAssociatedType(). We
     * take advantage of this information to get the associated handler. Ex:
     * com.google.gwt.event.dom.client.ClickEvent --->
     * com.google.gwt.event.dom.client.ClickHandler
     *
     * com.google.gwt.event.dom.client.BlurEvent --->
     * com.google.gwt.event.dom.client.BlurHandler
     */

    if (eventType == null) {
      return null;
    }

    MetaMethod method = eventType.getBestMatchingMethod("getAssociatedType", Type.class);

    if (method == null) {
      for (MetaMethod m : eventType.getMethods()) {
        if ("getAssociatedType".equals(m.getName())) {
          method = m;
          break;
        }
      }
    }

    if (method == null) {
      throw new GenerationException("Method 'getAssociatedType()' could not be found in the event ["
          + eventType.getName() + "]");
    }

    MetaClass returnType = method.getReturnType();
    if (returnType == null) {
      throw new GenerationException("The method 'getAssociatedType()' in the event [" + eventType.getName()
          + "] returns void.");
    }

    logger.fine("eventType: " + eventType.getClass() + " -- " + eventType);
    logger.fine("method: " + method.getClass() + " -- " + method);
    logger.fine("returnType: " + returnType.getClass() + " -- " + returnType);

    MetaParameterizedType parameterizedType = returnType.getParameterizedType();
    if (parameterizedType == null) {
      throw new GenerationException("The method 'getAssociatedType()' in the event [" + eventType.getName()
          + "] does not return Type<? extends EventHandler>..");
    }

    logger.fine("parameterizedType: " + parameterizedType.getClass() + " -- " + parameterizedType);

    MetaType[] argTypes = parameterizedType.getTypeParameters();
    if ((argTypes.length != 1) && argTypes[0] instanceof MetaClass
        && !((MetaClass) argTypes[0]).isAssignableTo(EventHandler.class)) {
      throw new GenerationException("The method 'getAssociatedType()' in the event [" + eventType.getName()
          + "] does not return Type<? extends EventHandler>..");
    }

    return (MetaClass) argTypes[0];
  }

  private void generateComponentCompositions(InjectableInstance<Templated> ctx,
                                             BlockBuilder<AnonymousClassStructureBuilder> builder, Statement component, Statement rootTemplateElement,
                                             Statement dataFieldElements, Statement fieldsMap) {

    /*
     * In case of constructor injection, search for the data binder parameter annotated with @AutoBound
     */
    Statement dataBinderRef = null;
    MetaClass dataModelType = null;
    BeanMetric beanMetric = InjectUtil.analyzeBean(ctx.getInjectionContext(), ctx.getEnclosingType());
    MetaConstructor mc = beanMetric.getInjectorConstructor();
    if (mc != null) {
      for (MetaParameter mp : mc.getParameters()) {
        if (mp.getType().getErased().isAssignableTo(MetaClassFactory.get(DataBinder.class))
            && mp.isAnnotationPresent(AutoBound.class)) {
          dataModelType = (MetaClass) mp.getType().getParameterizedType().getTypeParameters()[0];
          if (dataBinderRef != null) {
            throw new GenerationException("Multiple @AutoBound data binders found in constructor of " +
                mc.getDeclaringClass());
          }
          dataBinderRef = ctx.getInjectionContext().getInlineBeanReference(mp);
        }
      }
    }

    /*
     * Search for data binder fields annotated with @AutoBound
     */
    MetaField dataBinderField = null;
    for (MetaField field : ctx.getInjector().getInjectedType().getFields()) {
      if (field.getType().getErased().equals(MetaClassFactory.get(DataBinder.class))
          && field.isAnnotationPresent(AutoBound.class)) {
        if (dataBinderField != null) {
          throw new GenerationException("Multiple @AutoBound data binder fields found in class "
              + ctx.getInjector().getInjectedType());
        }
        if (dataBinderRef != null) {
          throw new GenerationException(
              "Multiple @AutoBound data binders found (check constructors and fields) in class "
                  + ctx.getInjector().getInjectedType());
        }
        dataModelType = (MetaClass) field.getType().getParameterizedType().getTypeParameters()[0];
        dataBinderField = field;
        dataBinderRef = Stmt.invokeStatic(ctx.getInjectionContext().getProcessingContext().getBootstrapClass(),
            PrivateAccessUtil.getPrivateFieldInjectorName(dataBinderField),
            Variable.get(ctx.getInjector().getInstanceVarName()));
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
      builder.append(Stmt.nestedCall(fieldsMap).invoke("put", field.getKey(), field.getValue()));
    }

    /*
     * Bind each widget if data binder is found and has been initialized.
     */
    BlockBuilder<ElseBlockBuilder> binderBlock = If.isNotNull(Variable.get("binder"));
    for (Entry<String, Statement> dataField : dataFields.entrySet()) {
      Bound bound = DecoratorDataField.aggregateDataFieldBoundMap(ctx, ctx.getType()).get(dataField.getKey());
      if (bound != null) {
        if (dataBinderRef != null) {
          String property = bound.property().equals("") ? dataField.getKey() : bound.property();
          // Check if bound property exists in data model type
          if (!DataBindingValidator.isValidPropertyChain(dataModelType, property)) {
            throw new GenerationException("Invalid binding of DataField " + dataField.getKey() + " in class "
                + ctx.getInjector().getInjectedType() + "! Property " + property + " not resolvable from class "
                + dataModelType + ". Hint: All types in a property chain must be @Bindable!");
          }

          Statement converter =
              bound.converter().equals(Bound.NO_CONVERTER.class) ? null : Stmt.newObject(bound.converter());
          binderBlock.append(Stmt.loadVariable("binder").invoke("bind", dataField.getValue(), property, converter));
        }
        else {
          throw new GenerationException("No @AutoBound data binder found for @Bound @DataField " + dataField.getKey()
              + " in class " + ctx.getInjector().getInjectedType());
        }
      }
    }

    if (dataBinderRef != null) {
      builder.append(binderBlock
          .finish()
          .else_()
          .append(Stmt.invokeStatic(GWT.class, "log", "DataBinder in class "
              + ctx.getEnclosingType().getFullyQualifiedName()
              + " has not been initialized - skipping automatic binding!")).finish());
    }

    /*
     * Attach the Template to the Component, and set up the GWT Widget hierarchy
     * to preserve Handlers and DOM events.
     */
    builder.append(Stmt.invokeStatic(TemplateUtil.class, "initWidget", component, rootTemplateElement,
        Stmt.nestedCall(fieldsMap).invoke("values")));

  }

  /**
   * Create an inner interface for the given {@link Template} class and its HTML corresponding resource
   */
  private void generateTemplateResourceInterface(InjectableInstance<Templated> ctx, final MetaClass type) {
    ClassStructureBuilder<?> componentTemplateResource = ClassBuilder.define(getTemplateTypeName(type)).publicScope()
        .interfaceDefinition().implementsInterface(Template.class).implementsInterface(ClientBundle.class)
        .body()
        .publicMethod(TextResource.class, "getContents").annotatedWith(new Source() {

          @Override
          public Class<? extends Annotation> annotationType() {
            return Source.class;
          }

          @Override
          public String[] value() {
            return new String[]{getTemplateFileName(type)};
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
        	if(resource.startsWith("/")) {
        		resource = resource.substring(1,resource.length());
        	}
        	int count = 0;
        	for(int i =0; i < resource.length(); i++) {
        	    if(resource.charAt(i) == '/') {
        	        count++;
        	    }
        	}
        	if(count == 0) {
        		resource = type.getPackageName().replaceAll("\\.", "/") + "/" + resource;
        	}
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