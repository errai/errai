/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ui.nav.rebind;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.errai.codegen.exception.GenerationException;
import org.jboss.errai.codegen.meta.HasAnnotations;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.impl.java.JavaReflectionClass;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.config.util.ClassScanner;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.graph.api.InjectionSite;
import org.jboss.errai.ioc.rebind.ioc.graph.api.Qualifier;
import org.jboss.errai.ioc.rebind.ioc.graph.api.QualifierFactory;
import org.jboss.errai.ioc.rebind.ioc.graph.impl.FactoryNameGenerator;
import org.jboss.errai.ioc.rebind.ioc.graph.impl.InjectableHandle;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableProvider;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.TransitionToRole;
import org.jboss.errai.ui.nav.client.local.UniquePageRole;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.gwt.core.ext.GeneratorContext;

/**
 * @author edewit@redhat.com
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ClassScanner.class)
public class ValidationRulesTest {
  private final NavigationGraphGenerator generator = new NavigationGraphGenerator();
  private TransitionProviderIOCExtension transitionProvider;

  @Mock
  private InjectionContext injContext;

  @Mock
  private GeneratorContext genContext;

  @Mock
  private IOCProcessingContext procContext;

  @Mock
  private InjectionSite injSite;

  @Mock
  private FactoryNameGenerator nameGenerator;

  @Mock
  private Qualifier transitionToRole;

  @Mock
  private Qualifier transitionTo;

  @Mock
  private org.jboss.errai.ui.nav.client.local.api.TransitionToRole toRoleAnno;

  @Captor
  private ArgumentCaptor<InjectableHandle> handleCaptor;

  @Captor
  private ArgumentCaptor<InjectableProvider> providerCaptor;

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Before
  public void setup() {
    transitionProvider = new TransitionProviderIOCExtension();
    final QualifierFactory qualFactory = mock(QualifierFactory.class);

    when(nameGenerator.generateFor(Mockito.any(), Mockito.any())).thenReturn("FactoryName");
    when(procContext.getGeneratorContext()).thenReturn(genContext);
    when(injContext.getProcessingContext()).thenReturn(procContext);
    when(injContext.getQualifierFactory()).thenReturn(qualFactory);
    when(toRoleAnno.annotationType()).thenReturn((Class) org.jboss.errai.ui.nav.client.local.api.TransitionToRole.class);
    when(qualFactory.forSource(Mockito.any())).then(invocation -> {
      final HasAnnotations hasAnno = (HasAnnotations) invocation.getArguments()[0];
      if (hasAnno.isAnnotationPresent(org.jboss.errai.ui.nav.client.local.api.TransitionToRole.class)) {
        return transitionToRole;
      }
      else if (hasAnno.isAnnotationPresent(org.jboss.errai.ui.nav.client.local.api.TransitionTo.class)) {
        return transitionTo;
      }
      else {
        throw new IllegalStateException();
      }
    });
    doNothing().when(injContext).registerExactTypeInjectableProvider(handleCaptor.capture(), providerCaptor.capture());
  }


  @Test
  public void shouldThrowExceptionWhenMoreThenOneDefaultPage() {
    // given
    mockClassScanner(StartPage1.class, StartPage2.class);

    // when
    try {
      generator.generate(null, null);
      fail("GenerationException should have been thrown because more then one start page was defined.");
    } catch (final GenerationException e) {

      // then
      final String message = e.getMessage();
      assertTrue(message.contains(StartPage1.class.getName()));
      assertTrue(message.contains(StartPage2.class.getName()));
    }
  }

  private List<MetaClass> createMetaClassList(final Class<?>... classes) {
    final List<MetaClass> result = new ArrayList<MetaClass>(classes.length);
    for (final Class<?> aClass : classes) {
      result.add(JavaReflectionClass.newInstance(aClass));
    }
    return result;
  }

  @Test
  public void shouldThrowExceptionMoreUniquePages() {
    // given
    mockClassScanner(Page1.class, Page2.class);

    // when
    try {
      generator.generate(null, null);
      fail("GenerationException should have been thrown because more then one unique page was defined");
    } catch (final GenerationException e) {
      final String message = e.getMessage();
      assertTrue(message.contains(Page1.class.getName()));
      assertTrue(message.contains(Page2.class.getName()));
    }
  }

  @Test
  public void shouldThrowExceptionWhenNoStartPageDefined() {
    // given
    mockClassScanner(Page2.class);

    // when
    try {
      generator.generate(null, null);
      fail("GenerationException should have been thrown because no default start page was defined");
    } catch (final GenerationException e) {
      final String message = e.getMessage();
      assertTrue(message.contains("DefaultPage"));
    }
  }

  @Test(expected = GenerationException.class)
  public void shouldThrowExceptionWhenTransitionToForRoleWithNoPage() throws Exception {
    mockClassScanner(StartPage1.class, PageWithTransitionToMyUniquePageRole.class);
    generator.generate(null, null);
    fail("GenerationException should have been thrown because no PageWithTransitionToMyUniquePageRole was defined.");
  }

  @Test
  public void doNotValidateIfOnlyDenylistedPages() throws Exception {
    overrideDenylistedClassNames(DenylistedPage.class.getCanonicalName());
    mockClassScanner(DenylistedPage.class);

    try {
      generator.generate(null, null);
    }
    catch (final GenerationException e) {
      fail("Validation should not have ocurred.");
    }
  }

  @Test(expected = GenerationException.class)
  public void shouldThrowExceptionForDefaultPageWithPathParam() throws Exception {
    mockClassScanner(DefaultPageWithPathParam.class);
    generator.generate(null, null);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test(expected = GenerationException.class)
  public void shouldThrowExceptionWithTransitionToMissingRole() throws Exception {
    mockClassScanner();
    transitionProvider.afterInitialization(procContext, injContext);

    final InjectableProvider transitionToRoleProvider = getTransitionToRoleProvider();
    when(injSite.isAnnotationPresent(org.jboss.errai.ui.nav.client.local.api.TransitionToRole.class)).thenReturn(true);
    when(injSite.getAnnotation(org.jboss.errai.ui.nav.client.local.api.TransitionToRole.class)).thenReturn(toRoleAnno);
    when(toRoleAnno.value()).thenReturn((Class) MyUniquePageRole.class);

    transitionToRoleProvider.getInjectable(injSite, nameGenerator);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test(expected = GenerationException.class)
  public void shouldThrowExceptionWithTransitionToUniqueRoleWithMultiplePages() throws Exception {
    mockClassScanner(Page1.class, Page2.class);
    transitionProvider.afterInitialization(procContext, injContext);

    final InjectableProvider transitionToRoleProvider = getTransitionToRoleProvider();
    when(injSite.isAnnotationPresent(org.jboss.errai.ui.nav.client.local.api.TransitionToRole.class)).thenReturn(true);
    when(injSite.getAnnotation(org.jboss.errai.ui.nav.client.local.api.TransitionToRole.class)).thenReturn(toRoleAnno);
    when(toRoleAnno.value()).thenReturn((Class) MyUniquePageRole.class);

    transitionToRoleProvider.getInjectable(injSite, nameGenerator);
  }

  private InjectableProvider getTransitionToRoleProvider() {
    final List<InjectableHandle> handles = handleCaptor.getAllValues();
    for (int i = 0; i < handles.size(); i++) {
      final InjectableHandle handle = handles.get(i);
      if (handle.getQualifier() == transitionToRole) {
        return providerCaptor.getAllValues().get(i);
      }
    }

    throw new IllegalStateException();
  }


  private void mockClassScanner(final Class<?>... pages) {
    PowerMockito.mockStatic(ClassScanner.class);
    when(ClassScanner.getTypesAnnotatedWith(Page.class, null)).thenReturn(createMetaClassList(pages));
  }

  private void overrideDenylistedClassNames(final String... names) throws SecurityException, NoSuchFieldException,
          IllegalArgumentException, IllegalAccessException {
    final Field denylistedField = NavigationGraphGenerator.class.getDeclaredField("DENYLISTED_PAGES");

    denylistedField.setAccessible(true);

    // Change the field to not be final so that we can overwrite it.
    final Field fieldModifiers = Field.class.getDeclaredField("modifiers");
    fieldModifiers.setAccessible(true);
    fieldModifiers.setInt(denylistedField, fieldModifiers.getInt(denylistedField) & ~Modifier.FINAL);

    denylistedField.set(null, Arrays.asList(names));
  }

  @Page(role = DefaultPage.class)
  private static class StartPage1 implements IsElement {
    @Override
    public HTMLElement getElement() {
      throw new RuntimeException("Not yet implemented.");
    }
  }

  @Page(role = DefaultPage.class)
  private static class StartPage2 implements IsElement {

    @Override
    public HTMLElement getElement() {
      throw new RuntimeException("Not yet implemented.");
    }}

  private static class MyUniquePageRole implements UniquePageRole {}

  @Page(role = {ValidationRulesTest.MyUniquePageRole.class, DefaultPage.class})
  private static class Page1 implements IsElement {

    @Override
    public HTMLElement getElement() {
      throw new RuntimeException("Not yet implemented.");
    }}

  @Page(role = ValidationRulesTest.MyUniquePageRole.class)
  private static class Page2 implements IsElement {

    @Override
    public HTMLElement getElement() {
      throw new RuntimeException("Not yet implemented.");
    }}

  @Page
  private static class PageWithTransitionToMyUniquePageRole implements IsElement {
    @SuppressWarnings("unused")
    private TransitionToRole<MyUniquePageRole> transition;

    @Override
    public HTMLElement getElement() {
      throw new RuntimeException("Not yet implemented.");
    }
  }

  @Page
  private static class DenylistedPage implements IsElement {

    @Override
    public HTMLElement getElement() {
      throw new RuntimeException("Not yet implemented.");
    }
  }

  @Page(role = DefaultPage.class, path = "{var}/text")
  private static class DefaultPageWithPathParam implements IsElement {

    @Override
    public HTMLElement getElement() {
      throw new RuntimeException("Not yet implemented.");
    }}

}
