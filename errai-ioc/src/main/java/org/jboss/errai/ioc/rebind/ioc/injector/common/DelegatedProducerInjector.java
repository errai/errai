/*
 * Copyright 2014 JBoss, by Red Hat, Inc
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

package org.jboss.errai.ioc.rebind.ioc.injector.common;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassMember;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.util.GenUtil;
import org.jboss.errai.ioc.rebind.ioc.exception.InjectionFailure;
import org.jboss.errai.ioc.rebind.ioc.injector.Injector;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectorRegistrationListener;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A {@link DelegatedProducerInjector} collects methods that are common for
 * {@link org.jboss.errai.ioc.rebind.ioc.injector.basic.SyncProducerInjector} and
 * {@link org.jboss.errai.ioc.rebind.ioc.injector.async.AsyncProducerInjector}
 *
 * @author Alexander Buyanov
 */
public class DelegatedProducerInjector {
  /**
   * The reference to {@link ProducerInjector}
   */
  private final ProducerInjector injector;


  public DelegatedProducerInjector(ProducerInjector injector) {
    this.injector = injector;
  }

  /**
   *
   * @param context the {@link org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext} reference
   *                to add {@link org.jboss.errai.ioc.rebind.ioc.injector.api.InjectorRegistrationListener}
   */

  public void makeSpecialized(final InjectionContext context) {
    final MetaClass type = injector.getInjectedType();
    final MetaClassMember producerMember = injector.getProducerMember();

    if (!(producerMember instanceof MetaMethod)) {
      throw new InjectionFailure("cannot specialize a field-based producer: " + producerMember);
    }
    final MetaMethod producerMethod = (MetaMethod) producerMember;

    if (producerMethod.isStatic()) {
      throw new InjectionFailure("cannot specialize a static producer method: " + producerMethod);
    }

    if (type.getSuperClass().getFullyQualifiedName().equals(Object.class.getName())) {
      throw new InjectionFailure("the specialized producer " + producerMember + " must override "
          + "another producer");
    }

    context.addInjectorRegistrationListener(injector.getInjectedType(),
        new InjectorRegistrationListener() {
          @Override
          public void onRegister(final MetaClass type, final Injector injector) {
            MetaClass cls = producerMember.getDeclaringClass();
            while ((cls = cls.getSuperClass()) != null && !cls.getFullyQualifiedName().equals(Object.class.getName())) {
              if (!context.hasInjectorForType(cls)) {
                context.addType(cls);
              }

              final MetaMethod declaredMethod
                  = cls.getDeclaredMethod(producerMethod.getName(), GenUtil.fromParameters(producerMethod.getParameters()));

              context.declareOverridden(declaredMethod);

              updateQualifiersAndName(producerMethod, context);
            }
          }
        });
  }

  private void updateQualifiersAndName(final MetaMethod producerMethod, final InjectionContext context) {
    if (!context.hasInjectorForType(injector.getInjectedType())) return;

    final Set<Annotation> qualifiers = new HashSet<Annotation>();
    qualifiers.addAll(Arrays.asList(injector.getQualifyingMetadata().getQualifiers()));

    for (final Injector inj : context.getInjectors(injector.getInjectedType())) {
      if (inj != this
          && inj instanceof ProducerInjector
          && methodSignatureMatches((MetaMethod) ((ProducerInjector) inj).getProducerMember(), producerMethod)) {

        if (injector.getBeanName() == null) {
          injector.setBeanName(inj.getBeanName());
        }

        inj.setEnabled(false);
        qualifiers.addAll(Arrays.asList(inj.getQualifyingMetadata().getQualifiers()));
      }
    }

    injector.setQualifyingMetadata(context.getProcessingContext()
        .getQualifyingMetadataFactory().createFrom(qualifiers.toArray(new Annotation[qualifiers.size()])));
  }

  private static boolean methodSignatureMatches(final MetaMethod a, final MetaMethod b) {
    return a.getName().equals(b.getName())
        && Arrays.equals(GenUtil.fromParameters(a.getParameters()), GenUtil.fromParameters(b.getParameters()));
  }
}
