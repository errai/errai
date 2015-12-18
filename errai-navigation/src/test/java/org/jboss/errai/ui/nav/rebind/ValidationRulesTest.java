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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.errai.codegen.exception.GenerationException;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.impl.java.JavaReflectionClass;
import org.jboss.errai.config.util.ClassScanner;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.TransitionToRole;
import org.jboss.errai.ui.nav.client.local.UniquePageRole;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author edewit@redhat.com
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ClassScanner.class)
public class ValidationRulesTest {
  private final NavigationGraphGenerator generator = new NavigationGraphGenerator();


  @Test
  public void shouldThrowExceptionWhenMoreThenOneDefaultPage() {
    // given
    mockClassScanner(StartPage1.class, StartPage2.class);

    // when
    try {
      generator.generate(null, null);
      fail("GenerationException should have been thrown because more then one start page was defined.");
    } catch (GenerationException e) {

      // then
      String message = e.getMessage();
      assertTrue(message.contains(StartPage1.class.getName()));
      assertTrue(message.contains(StartPage2.class.getName()));
    }
    verify(ClassScanner.class);
  }

  private List<MetaClass> createMetaClassList(Class<?>... classes) {
    List<MetaClass> result = new ArrayList<MetaClass>(classes.length);
    for (Class<?> aClass : classes) {
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
    } catch (GenerationException e) {
      String message = e.getMessage();
      assertTrue(message.contains(Page1.class.getName()));
      assertTrue(message.contains(Page2.class.getName()));
    }
    verify(ClassScanner.class);
  }

  @Test
  public void shouldThrowExceptionWhenNoStartPageDefined() {
    // given
    mockClassScanner(Page2.class);

    // when
    try {
      generator.generate(null, null);
      fail("GenerationException should have been thrown because no default start page was defined");
    } catch (GenerationException e) {
      String message = e.getMessage();
      assertTrue(message.contains("DefaultPage"));
    }
    verify(ClassScanner.class);
  }

  @Test(expected = GenerationException.class)
  public void shouldThrowExceptionWhenTransitionToForRoleWithNoPage() throws Exception {
    mockClassScanner(StartPage1.class, PageWithTransitionToMyUniquePageRole.class);
    generator.generate(null, null);
    fail("GenerationException should have been thrown because no PageWithTransitionToMyUniquePageRole was defined.");
  }

  @Test
  public void doNotValidateIfOnlyBlacklistedPages() throws Exception {
    overrideBlacklistedClassNames(BlacklistedPage.class.getCanonicalName());
    mockClassScanner(BlacklistedPage.class);

    try {
      generator.generate(null, null);
    }
    catch (GenerationException e) {
      fail("Validation should not have ocurred.");
    }
  }
  
  @Test(expected = GenerationException.class)
  public void shouldThrowExceptionForDefaultPageWithPathParam() throws Exception {
    mockClassScanner(DefaultPageWithPathParam.class);
    generator.generate(null, null);
  }

  private void mockClassScanner(Class<?>... pages) {
    PowerMockito.mockStatic(ClassScanner.class);
    when(ClassScanner.getTypesAnnotatedWith(Page.class, null)).thenReturn(createMetaClassList(pages));
  }

  private void overrideBlacklistedClassNames(final String... names) throws SecurityException, NoSuchFieldException,
          IllegalArgumentException, IllegalAccessException {
    final Field blacklistedField = NavigationGraphGenerator.class.getDeclaredField("BLACKLISTED_PAGES");

    blacklistedField.setAccessible(true);

    // Change the field to not be final so that we can overwrite it.
    final Field fieldModifiers = Field.class.getDeclaredField("modifiers");
    fieldModifiers.setAccessible(true);
    fieldModifiers.setInt(blacklistedField, fieldModifiers.getInt(blacklistedField) & ~Modifier.FINAL);

    blacklistedField.set(null, Arrays.asList(names));
  }

  @Page(role = DefaultPage.class)
  private static class StartPage1 extends SimplePanel {}

  @Page(role = DefaultPage.class)
  private static class StartPage2 extends SimplePanel {}

  private static class MyUniquePageRole implements UniquePageRole {}

  @Page(role = {ValidationRulesTest.MyUniquePageRole.class, DefaultPage.class})
  private static class Page1 extends SimplePanel {}

  @Page(role = ValidationRulesTest.MyUniquePageRole.class)
  private static class Page2 extends SimplePanel {}

  @Page
  private static class PageWithTransitionToMyUniquePageRole extends SimplePanel {
    private TransitionToRole<MyUniquePageRole> transition;
  }

  @Page
  private static class BlacklistedPage extends SimplePanel {
  }
  
  @Page(role = DefaultPage.class, path = "{var}/text")
  private static class DefaultPageWithPathParam extends SimplePanel {}
  
}
