package org.jboss.errai.codegen.util;

import org.jboss.errai.codegen.Modifier;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaConstructor;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;

/**
 * @author Mike Brock
 */
public interface PrivateMemberAccessor {
  public void createWritableField(MetaClass type,
                                  ClassStructureBuilder<?> classBuilder,
                                  MetaField field,
                                  Modifier[] modifiers);

  public void createReadableField(MetaClass type,
                                  ClassStructureBuilder<?> classBuilder,
                                  MetaField field,
                                  Modifier[] modifiers);

  public void makeMethodAccessible(final ClassStructureBuilder<?> classBuilder,
                                   final MetaMethod field,
                                   Modifier[] modifiers);

  public void makeConstructorAccessible(final ClassStructureBuilder<?> classBuilder,
                                   final MetaConstructor field);
}
