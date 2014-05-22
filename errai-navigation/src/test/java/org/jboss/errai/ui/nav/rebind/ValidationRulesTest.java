package org.jboss.errai.ui.nav.rebind;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.ArrayList;
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

  private void mockClassScanner(Class<?>... pages) {
    mockStatic(ClassScanner.class);
    when(ClassScanner.getTypesAnnotatedWith(Page.class, null)).thenReturn(createMetaClassList(pages));
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
}
