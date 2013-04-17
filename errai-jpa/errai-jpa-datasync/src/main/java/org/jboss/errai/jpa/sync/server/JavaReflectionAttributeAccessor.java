package org.jboss.errai.jpa.sync.server;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import javax.persistence.metamodel.Attribute;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.jpa.sync.client.shared.JpaAttributeAccessor;

/**
 * An implementation of {@link JpaAttributeAccessor} that works on the server by
 * reflectively reading fields and/or invoking getter methods.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public class JavaReflectionAttributeAccessor implements JpaAttributeAccessor {

  @SuppressWarnings("unchecked")
  @Override
  public <X, Y> Y get(Attribute<X, Y> attr, X entity) {

    Member member = attr.getJavaMember();
    Class<Y> attrType = attr.getJavaType();
    try {
      if (member instanceof Field) {
        Field f = (Field) member;
        f.setAccessible(true);
        try {
          if (attrType.isPrimitive()) {
            MetaClass mc = MetaClassFactory.get(attrType);
            attrType = (Class<Y>) mc.asBoxed().asClass();
          }
          return attrType.cast(f.get(entity));
        } catch (ClassCastException e) {
          throw new ClassCastException("Attribute = " + f + " value = " + f.get(entity));
        }
      }
      else if (member instanceof Method) {
        Method m = (Method) member;
        m.setAccessible(true);
        if (attrType.isPrimitive()) {
          MetaClass mc = MetaClassFactory.get(attrType);
          attrType = (Class<Y>) mc.asBoxed().asClass();
        }
        return attrType.cast(m.invoke(entity));
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
        f.set(entity, value);
      }
      else if (member instanceof Method) {
        Method m = (Method) member;
        if (m.getName().startsWith("get")) {
          m = m.getDeclaringClass().getMethod("set" + m.getName().substring(3), attr.getJavaType());
        }
        else if (m.getName().startsWith("is")) {
          m = m.getDeclaringClass().getMethod("set" + m.getName().substring(2), attr.getJavaType());
        }
        m.setAccessible(true);
        try {
          m.invoke(entity, value);
        }
        catch (IllegalArgumentException e) {
          throw new RuntimeException("Failed to invoke method " + m, e);
        }
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
    catch (SecurityException e) {
      throw new RuntimeException(e);
    }
    catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

}
