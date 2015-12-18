package org.jboss.errai.codegen.exception;

/**
 * Thrown when a LiteralValue is requested for an object that cannot be
 * represented as a LiteralValue.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class NotLiteralizableException extends GenerationException {

  private final Object nonLiteralizableObject;

  /**
   * Creates a new NotLiteralizableException that arose as a result of
   * requesting a LiteralValue of the given object.
   *
   * @param nonLiteralizableObject
   *          the object that is not literalizable.
   */
  public NotLiteralizableException(Object nonLiteralizableObject) {
    super("Not literalizable: " +
          (nonLiteralizableObject == null ? "null" : nonLiteralizableObject.toString()) +
          " (of type " + (nonLiteralizableObject == null ? "void" : nonLiteralizableObject.getClass().getName()) + ")");
    this.nonLiteralizableObject = nonLiteralizableObject;
  }

  /**
   * Returns the object that could not be literalized.
   *
   * @return The object that could not be literalized.
   */
  public Object getNonLiteralizableObject() {
    return nonLiteralizableObject;
  }
}
