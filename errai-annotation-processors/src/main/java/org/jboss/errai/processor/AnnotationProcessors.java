package org.jboss.errai.processor;

import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

/**
 * An indiscriminate dumping ground for static methods that help paper over the
 * missing functionality in the Java 6 Annotation Processing API.
 * <p>
 * Do your worst! :-)
 *
 * @author jfuerth
 */
public class AnnotationProcessors {

  /** Prevents instantiation. */
  private AnnotationProcessors() {}

  public static boolean hasAnnotation(Element target, CharSequence annotationQualifiedName) {
    return getAnnotation(target, annotationQualifiedName) != null;
  }

  public static AnnotationMirror getAnnotation(Element target, CharSequence annotationQualifiedName) {
    for (AnnotationMirror am : target.getAnnotationMirrors()) {
      Name annotationClassName = ((TypeElement) am.getAnnotationType().asElement()).getQualifiedName();
      if (annotationClassName.contentEquals(annotationQualifiedName)) {
        return am;
      }
    }
    return null;
  }

  /**
   * Retrieves a parameter value from an annotation that targets the given
   * element. The returned value does not take defaults into consideration.
   *
   * @param target
   *          The element targeted by an instance of the given annotation. Could
   *          be a class, field, method, or anything else.
   * @param annotationQualifiedName
   *          The fully-qualified name of the annotation to retrieve the
   *          parameter value from.
   * @param paramName
   *          the name of the annotation parameter to retrieve.
   * @return the String value of the given annotation's parameter, or null if
   *         the parameter is not present on the annotation.
   */
  static AnnotationValue getAnnotationParamValueWithoutDefaults(
          Element target, CharSequence annotationQualifiedName, CharSequence paramName) {
    AnnotationMirror templatedAnnotation = getAnnotation(target, annotationQualifiedName);
    Map<? extends ExecutableElement, ? extends AnnotationValue> annotationParams = templatedAnnotation.getElementValues();
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> param : annotationParams.entrySet()) {
      if (param.getKey().getSimpleName().contentEquals(paramName)) {
        return param.getValue();
      }
    }
    return null;
  }

  static Element getField(TypeElement classElement, CharSequence fieldName) {
    if (fieldName.charAt(0) == '\"') {
      throw new IllegalArgumentException("given field name begins with invalid character: " + fieldName.charAt(0));
    }
    for (Element field : ElementFilter.fieldsIn(classElement.getEnclosedElements())) {
      if (field.getSimpleName().contentEquals(fieldName)) {
        return field;
      }
    }
    return null;
  }

  public static TypeElement getEnclosingTypeElement(final Element element) {
    Element currentElement = element;
    while (currentElement != null && currentElement.getKind() != ElementKind.CLASS) {
      currentElement = currentElement.getEnclosingElement();
    }

    if (currentElement == null) {
      throw new RuntimeException("No enclosing class for " + element);
    }

    return (TypeElement) currentElement;
  }

  /**
   * Returns the JavaBeans property name defined by the given method element, if
   * it does in fact define a property name. The name is calculated by stripping
   * off the prefix "is", "get", or "set" and then converting the new initial
   * character to lowercase.
   *
   * @param el
   *          The method element to extract a property name from.
   * @return the property name defined by the method according to JavaBeans
   *         convention, or null if the method does not define a JavaBeans
   *         property setter/getter.
   */
  static String propertyNameOfMethod(Element el) {
    Name methodName = el.getSimpleName();
    String propertyName = null;
    if (methodName.length() > 3 && "get".contentEquals(methodName.subSequence(0, 3))) {
      StringBuilder sb = new StringBuilder(methodName.length() - 3);
      sb.append(Character.toLowerCase(methodName.charAt(3)));
      sb.append(methodName.subSequence(4, methodName.length()));
      propertyName = sb.toString();
    }
    else if (methodName.length() > 2 && "is".contentEquals(methodName.subSequence(0, 2))) {
      StringBuilder sb = new StringBuilder(methodName.length() - 2);
      sb.append(Character.toLowerCase(methodName.charAt(2)));
      sb.append(methodName.subSequence(3, methodName.length()));
      propertyName = sb.toString();
    }
    else if (methodName.length() > 3 && "set".contentEquals(methodName.subSequence(0, 2))) {
      StringBuilder sb = new StringBuilder(methodName.length() - 3);
      sb.append(Character.toLowerCase(methodName.charAt(3)));
      sb.append(methodName.subSequence(4, methodName.length()));
      propertyName = sb.toString();
    }
    return propertyName;
  }
}
