package org.jboss.errai.codegen.framework.util;

import org.jboss.errai.codegen.framework.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class Implementations {
  public static ClassStructureBuilder<?> implement(Class<?> clazz) {
    return ClassBuilder.define(clazz.getPackage().getName() + "." + clazz.getSimpleName() + "Impl")
                .publicScope()
                .implementsInterface(clazz)
                .body();
  }
  
  public static void autoInitializedField(ClassStructureBuilder<?> builder, MetaClass type,
                                           String name, Class<?> implementation) {

     autoInitializedField(builder, type, name, MetaClassFactory.get(implementation));
  }
  
  public static void autoInitializedField(ClassStructureBuilder<?> builder, MetaClass type,
                                          String name, MetaClass implementation) {

    implementation = MetaClassFactory.parameterizedAs(implementation, type.getParameterizedType());
    
    builder.privateField(name, type)
            .initializesWith(Stmt.newObject(implementation)).finish();
  }
  
}
