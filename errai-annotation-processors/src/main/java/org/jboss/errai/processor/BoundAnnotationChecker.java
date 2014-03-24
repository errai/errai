package org.jboss.errai.processor;

import static org.jboss.errai.processor.AnnotationProcessors.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

/**
 * Evaluates usage of the ErraiUI DataField annotation and emits errors and warnings when
 * the annotation is not being used correctly.
 */
@SupportedAnnotationTypes(TypeNames.BOUND)
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class BoundAnnotationChecker extends AbstractProcessor {

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    final Types types = processingEnv.getTypeUtils();
    final Elements elements = processingEnv.getElementUtils();
    final TypeMirror gwtWidgetType = elements.getTypeElement(TypeNames.GWT_WIDGET).asType();

    Map<TypeElement, List<Element>> classesWithBoundThings = new HashMap<TypeElement, List<Element>>();
    for (TypeElement annotation : annotations) {
      for (Element target : roundEnv.getElementsAnnotatedWith(annotation)) {
        TypeMirror targetType;
        if (target.getKind() == ElementKind.METHOD) {
          targetType = ((ExecutableElement) target).getReturnType();
        }
        else {
          targetType = target.asType();
        }
        if (!types.isAssignable(targetType, gwtWidgetType)) {
          processingEnv.getMessager().printMessage(
                  Kind.ERROR, "@Bound must target a type assignable to Widget", target); // FIXME actually HasText or HasValue
        }

        TypeElement enclosingClass = getEnclosingTypeElement(target);
        List<Element> boundThings = classesWithBoundThings.get(enclosingClass);
        if (boundThings == null) {
          boundThings = new ArrayList<Element>();
          classesWithBoundThings.put(enclosingClass, boundThings);
        }
        boundThings.add(target);
      }
    }

    for (Map.Entry<TypeElement, List<Element>> classWithItsBoundThings : classesWithBoundThings.entrySet()) {
      List<TypeMirror> modelTypes = findAllModelTypes(classWithItsBoundThings.getKey());
      if (modelTypes.size() == 0) {
        for (Element boundElement : classWithItsBoundThings.getValue()) {
          processingEnv.getMessager().printMessage(
                  Kind.ERROR, "@Bound requires that this class also contains a @Model or @AutoBound DataBinder",
                  boundElement, getAnnotation(boundElement, TypeNames.BOUND));
        }
      }
      else if (modelTypes.size() > 1) {
        // TODO mark everything annotated with @AutoBound or @Model with an error
      }
      else {
        TypeMirror modelType = modelTypes.get(0);
        Set<String> modelPropertyNames = getPropertyNames(modelType);
        for (Element boundElement : classWithItsBoundThings.getValue()) {
          switch (boundElement.getKind()) {
          case FIELD:
          case PARAMETER:
            if (!modelPropertyNames.contains(boundElement.getSimpleName().toString())) {
              processingEnv.getMessager().printMessage(
                      Kind.ERROR, "The model type " + ((DeclaredType) modelType).asElement().getSimpleName() + " does not have property \"" + boundElement.getSimpleName() + "\"",
                      boundElement, getAnnotation(boundElement, TypeNames.BOUND));
            }
            break;
          case METHOD:
            String propertyName = propertyNameOfMethod(boundElement);
            if (!modelPropertyNames.contains(propertyName)) {
              processingEnv.getMessager().printMessage(
                      Kind.ERROR, "The model type " + ((DeclaredType) modelType).asElement().getSimpleName() + " does not have property \"" + propertyName + "\"",
                      boundElement, getAnnotation(boundElement, TypeNames.BOUND));
            }
            break;
          default:
            break;
          }
        }
      }
    }

    return false;
  }

  /**
   * Returns the set of all bindable property names in the given model.
   */
  private Set<String> getPropertyNames(TypeMirror modelType) {
    final Elements elements = processingEnv.getElementUtils();
    final Types types = processingEnv.getTypeUtils();

    Set<String> result = new HashSet<String>();

    for (Element el : ElementFilter.methodsIn(elements.getAllMembers((TypeElement) types.asElement(modelType)))) {
      String propertyName = AnnotationProcessors.propertyNameOfMethod(el);
      if (propertyName != null) {
        result.add(propertyName);
      }
      // TODO extract type info from methods
//        for (VariableElement param : ((ExecutableElement) el).getParameters()) {
//          if (hasAnnotation(param, TypeNames.MODEL)) {
//            result.add(param.asType());
//          }
//          else if (hasAnnotation(param, TypeNames.AUTO_BOUND)) {
//            result.add(typeOfDataBinder(param.asType()));
//          }
//        }
    }
    return result;
  }

  /**
   * Returns the bindable model type of all things annotated with {@code @Model}
   * and DataBinders annotated with {@code @AutoBound}. Legally, there should
   * only be one; we return all of them as Elements so the caller can attach
   * error/warning messages to them if we found multiples.
   *
   * @param classContainingBindableThings
   * @return
   */
  private List<TypeMirror> findAllModelTypes(TypeElement classContainingBindableThings) {
    final List<TypeMirror> result = new ArrayList<TypeMirror>();
    final Elements elements = processingEnv.getElementUtils();

    for (Element el : elements.getAllMembers(classContainingBindableThings)) {
      switch (el.getKind()) {
      case METHOD:
      case CONSTRUCTOR:
        if (!hasAnnotation(el, TypeNames.JAVAX_INJECT)) continue;

        for (VariableElement param : ((ExecutableElement) el).getParameters()) {
          if (hasAnnotation(param, TypeNames.MODEL)) {
            result.add(param.asType());
          }
          else if (hasAnnotation(param, TypeNames.AUTO_BOUND)) {
            result.add(typeOfDataBinder(param.asType()));
          }
        }
        break;
      case FIELD:
        if (hasAnnotation(el, TypeNames.MODEL)) {
          result.add(el.asType());
        }
        else if (hasAnnotation(el, TypeNames.AUTO_BOUND)) {
          result.add(typeOfDataBinder(el.asType()));
        }
        break;
      default:
        break;
      }
    }
    return result;
  }

  /**
   * Returns the concrete type, type variable, or wildcard type of the given DataBinder declaration.
   *
   * @param dataBinderDeclaration
   * @return
   */
  private TypeMirror typeOfDataBinder(TypeMirror dataBinderDeclaration) {
    // in a superclass, this could return a type variable or a wildcard
    return ((DeclaredType) dataBinderDeclaration).getTypeArguments().get(0);
  }

}
