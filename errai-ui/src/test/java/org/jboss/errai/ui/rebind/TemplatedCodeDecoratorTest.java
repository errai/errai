/**
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ui.rebind;

import elemental2.dom.Event;
import org.jboss.errai.codegen.exception.GenerationException;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.meta.impl.java.JavaReflectionAnnotation;
import org.jboss.errai.codegen.meta.impl.build.BuildMetaClass;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.Decorable;
import org.jboss.errai.ioc.rebind.ioc.injector.api.FactoryController;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ui.client.local.spi.TemplateProvider;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@RunWith(MockitoJUnitRunner.class)
public class TemplatedCodeDecoratorTest {

  private static final Templated defaultTemplatedAnno = new Templated() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return Templated.class;
    }

    @Override
    public String value() {
      return "";
    }

    @Override
    public String stylesheet() {
      return "";
    }

    @Override
    public Class<? extends TemplateProvider> provider() {
      return Templated.DEFAULT_PROVIDER.class;
    }
  };

  private static final EventHandler defaultHandlerAnno = new EventHandler() {

    @Override
    public Class<? extends Annotation> annotationType() {
      return EventHandler.class;
    }

    @Override
    public String[] value() {
      return new String[] { "this" };
    }
  };

  @Mock
  private Decorable decorable;

  @Mock
  private FactoryController controller;

  @Mock
  private InjectionContext context;

  @Mock
  private MetaClass templatedClass;

  @Mock
  private BuildMetaClass factoryBuildMetaClass;

  @Mock
  private IOCProcessingContext iocProcessingContext;

  private MetaClass elemental2EventClass;

  private TemplatedCodeDecorator decorator;

  @SuppressWarnings("unchecked")
  @Before
  public void setup() {
    decorator = new TemplatedCodeDecorator(Templated.class);

    when(templatedClass.getAnnotation(Templated.class)).thenReturn(
            Optional.of(new JavaReflectionAnnotation(defaultTemplatedAnno)));
    when(templatedClass.getFullyQualifiedName()).thenReturn("org.foo.TestTemplated");
    when(templatedClass.getPackageName()).thenReturn("org.foo");
    when(templatedClass.getName()).thenReturn("TestTemplated");
    when(templatedClass.getMethodsAnnotatedWith(EventHandler.class)).thenReturn(Collections.emptyList());

    when(iocProcessingContext.resourceFilesFinder()).thenReturn(
            Thread.currentThread().getContextClassLoader()::getResource);

    when(context.getProcessingContext()).thenReturn(iocProcessingContext);

    when(decorable.getAnnotation()).thenReturn(new JavaReflectionAnnotation(defaultTemplatedAnno));
    when(decorable.getDecorableDeclaringType()).thenReturn(templatedClass);
    when(decorable.getType()).thenReturn(templatedClass);
    when(decorable.getInjectionContext()).thenReturn(context);
    when(decorable.getFactoryMetaClass()).thenReturn(factoryBuildMetaClass);

    this.elemental2EventClass = MetaClassFactory.getUncached(Event.class);
  }

  @Test
  public void nativeQuickHandlerWithNoEventTypeThrowsGenerationException() throws Exception {
    final MetaMethod handlerMethod = mock(MetaMethod.class);
    final MetaParameter eventParam = mock(MetaParameter.class);

    when(templatedClass.getMethodsAnnotatedWith(EventHandler.class)).thenReturn(singletonList(handlerMethod));
    when(handlerMethod.getAnnotation(EventHandler.class)).thenReturn(
            Optional.of(new JavaReflectionAnnotation(defaultHandlerAnno)));
    when(handlerMethod.getParameters()).thenReturn(new MetaParameter[] { eventParam });
    when(eventParam.getType()).thenReturn(elemental2EventClass);
    when(eventParam.getAnnotation(any(Class.class))).thenReturn(Optional.empty());

    try {
      decorator.generateDecorator(decorable, controller);
      fail("No error was observed.");
    } catch (final GenerationException observed) {
      try {
        assertTrue("Error must mention @ForEvent",
                ofNullable(observed.getMessage()).filter(m -> m.contains("@ForEvent")).isPresent());
        assertTrue("Error must mention @BrowserEvent",
                ofNullable(observed.getMessage()).filter(m -> m.contains("@BrowserEvent")).isPresent());
      } catch (final AssertionError ae) {
        ae.initCause(observed);
        throw ae;
      }
    } catch (final AssertionError ae) {
      throw ae;
    } catch (final Throwable t) {
      throw new AssertionError("Unexpected error: " + t.getMessage(), t);
    }
  }

}
