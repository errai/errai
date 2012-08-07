package java.lang.reflect;

/**
 * @author Mike Brock
 */
public final class Method {
  private final Class<?> declaringClass;
  private final String name;
  private final Class<?> returnType;
  private final int modifiers;
  private final Class<?>[] parameterTypes;


  public Method(final Class<?> declaringClass,
                final String name,
                final Class<?> returnType,
                final int modifiers,
                final Class<?>[] parameterTypes) {

    this.declaringClass = declaringClass;
    this.name = name;
    this.returnType = returnType;
    this.modifiers = modifiers;
    this.parameterTypes = parameterTypes;
  }

  public Class<?> getDeclaringClass() {
    return declaringClass;
  }

  public String getName() {
    return name;
  }

  public Class<?> getReturnType() {
    return returnType;
  }

  public int getModifiers() {
    return modifiers;
  }

  public Class<?>[] getParameterTypes() {
    return parameterTypes;
  }
}
