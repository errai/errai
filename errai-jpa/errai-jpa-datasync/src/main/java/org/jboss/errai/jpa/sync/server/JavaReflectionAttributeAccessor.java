package org.jboss.errai.jpa.sync.server;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import javax.persistence.metamodel.Attribute;

import org.jboss.errai.jpa.sync.client.shared.JpaAttributeAccessor;

/**
 * An implementation of {@link JpaAttributeAccessor} that works on the server by
 * reflectively reading fields and/or invoking getter methods.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public class JavaReflectionAttributeAccessor implements JpaAttributeAccessor {

  @Override
  public <X, Y> Y get(Attribute<X, Y> attr, X entity) {

    Member member = attr.getJavaMember();
    try {
      if (member instanceof Field) {
        Field f = (Field) member;
        f.setAccessible(true);
        try {
          return attr.getJavaType().cast(f.get(entity));
        } catch (ClassCastException e) {
          throw new ClassCastException("Attribute = " + f + " value = " + f.get(entity));
        }
      }
      else if (member instanceof Method) {
        Method m = (Method) member;
        m.setAccessible(true);
        return attr.getJavaType().cast(m.invoke(entity));
      }
      else {
        throw new RuntimeException("Java member " + member + " isn't a field or a method! Eek!");
      }
    }
    catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public <X, Y> void set(Attribute<X, Y> attr, X entity, Y value) {
    Member member = attr.getJavaMember();
    try {
      if (member instanceof Field) {
        Field f = (Field) member;
        f.setAccessible(true);
        f.set(entity, null); // TODO handle primitive values
      }
      else if (member instanceof Method) {
        Method m = (Method) member;
        m.setAccessible(true);
        m.invoke(entity, (Object) null); // TODO handle primitive values
      }
      else {
        throw new RuntimeException("Java member " + member + " isn't a field or a method! Eek!");
      }
    }
    catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

}
