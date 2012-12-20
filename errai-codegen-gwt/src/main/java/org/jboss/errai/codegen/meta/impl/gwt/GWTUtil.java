/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.codegen.meta.impl.gwt;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.errai.codegen.literal.LiteralFactory;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaType;
import org.jboss.errai.codegen.meta.MetaTypeVariable;
import org.jboss.errai.codegen.meta.impl.java.JavaReflectionClass;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.config.rebind.EnvUtil;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.typeinfo.JArrayType;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.JTypeParameter;
import com.google.gwt.core.ext.typeinfo.TypeOracle;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GWTUtil {

  public static MetaTypeVariable[] fromTypeVariable(final TypeOracle oracle,
                                                    final JTypeParameter[] typeParameters) {
    final List<MetaTypeVariable> typeVariableList = new ArrayList<MetaTypeVariable>();

    for (final JTypeParameter typeVariable : typeParameters) {
      typeVariableList.add(new GWTTypeVariable(oracle, typeVariable));
    }

    return typeVariableList.toArray(new MetaTypeVariable[typeVariableList.size()]);
  }


  public static MetaType[] fromTypeArray(final TypeOracle oracle, final JType[] types) {
    final List<MetaType> typeList = new ArrayList<MetaType>();

    for (final JType t : types) {
      typeList.add(fromType(oracle, t));
    }

    return typeList.toArray(new MetaType[types.length]);
  }

  private static JType getRootComponentType(JArrayType type) {
    JType root = null;
    while (type.getComponentType() != null) {
      if (type.getComponentType().isArray() != null) {
        type = type.getComponentType().isArray();
      }
      else {
        root = type.getComponentType();
        break;
      }

    }
    return root;
  }

  public static MetaClass eraseOrReturn(final TypeOracle oracle, final JType t) {

    if (t.isArray() != null) {
      final JType root = getRootComponentType(t.isArray());
      if (root.isTypeParameter() != null) {
        return MetaClassFactory.get(Object.class);
      }
    }
    if (t.isTypeParameter() != null) {
      return MetaClassFactory.get(Object.class);
    }
    return GWTClass.newInstance(oracle, t);
  }

  public static MetaType fromType(final TypeOracle oracle, final JType t) {
    if (t.isTypeParameter() != null) {
      return new GWTTypeVariable(oracle, t.isTypeParameter());
    }
    else if (t.isGenericType() != null) {
      if (t.isArray() != null) {
        return new GWTGenericArrayType(oracle, t.isGenericType());
      }
      else {
        return new GWTGenericDeclaration(oracle, t.isGenericType());
      }
    }
    else if (t.isParameterized() != null) {
      return new GWTParameterizedType(oracle, t.isParameterized());
    }
    else if (t.isWildcard() != null) {
      return new GWTWildcardType(oracle, t.isWildcard());
    }
    else if (t.isClassOrInterface() != null) {
      return GWTClass.newInstance(oracle, t.isClassOrInterface());
    }
    else {
      return null;
    }
  }

  private static volatile boolean typeOraclePopulated = false;
  private static volatile SoftReference<GeneratorContext> populatedFrom
          = new SoftReference<GeneratorContext>(null);

  /**
   * Erases the {@link MetaClassFactory} cache, then populates it with types
   * discovered via GWT's TypeOracle. The reason for the initial flush of the
   * MetaClassFactory is to support hot redeploy in Dev Mode. The reason for doing
   * this operation at all is so that the overridden class definitions
   * (super-source classes) are used in preference to the Java reflection based
   * class definitions.
   *
   * @param context The GeneratorContext supplied by the GWT compiler. Not null.
   * @param logger  The TreeLogger supplied by the GWT compiler. Not null.
   */
  public static void populateMetaClassFactoryFromTypeOracle(final GeneratorContext context,
                                                            final TreeLogger logger) {

    // if we're in production mode -- it means we're compiling, and we do not need to accommodate dynamically
    // changing classes. Therefore, do a NOOP after the first successful call.
    if (typeOraclePopulated && (context.equals(populatedFrom.get()) || EnvUtil.isProdMode())) {
      return;
    }

    final TypeOracle typeOracle = context.getTypeOracle();
    MetaClassFactory.emptyCache();
    // Clearing the LiteralFactory cache resolved https://issues.jboss.org/browse/ERRAI-456
    LiteralFactory.emptyCache();
    if (typeOracle != null) {
      final Set<String> translatable = new HashSet<String>(RebindUtils.findTranslatablePackages(context));
      translatable.remove("java.lang");
      translatable.remove("java.lang.annotation");

      for (final JClassType type : typeOracle.getTypes()) {
        if (!translatable.contains(type.getPackage().getName())) {
          logger.log(com.google.gwt.core.ext.TreeLogger.Type.DEBUG, "Skipping non-translatable " + type.getQualifiedSourceName());
          continue;
        }

        if (type.isAnnotation() != null || type.getQualifiedSourceName().equals("java.lang.annotation.Annotation")) {
          logger.log(com.google.gwt.core.ext.TreeLogger.Type.DEBUG, "Caching annotation type " + type.getQualifiedSourceName());

          if (!MetaClassFactory.canLoadClass(type.getQualifiedBinaryName()))  {
             throw new RuntimeException("a new annotation has been introduced (" + type.getQualifiedSourceName() + "); "
             + "you cannot currently introduce new annotations in devmode. Please restart.");
          }

          MetaClassFactory.pushCache(JavaReflectionClass
                  .newUncachedInstance(MetaClassFactory.loadClass(type.getQualifiedBinaryName())));
        }
        else {
          logger.log(com.google.gwt.core.ext.TreeLogger.Type.DEBUG, "Caching translatable type " + type.getQualifiedSourceName());
          MetaClassFactory.pushCache(GWTClass.newInstance(typeOracle, type));
        }
      }
    }
    typeOraclePopulated = true;
    populatedFrom = new SoftReference<GeneratorContext>(context);
  }
}
