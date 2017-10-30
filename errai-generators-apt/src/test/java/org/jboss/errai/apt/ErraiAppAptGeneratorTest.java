package org.jboss.errai.apt;

import org.jboss.errai.codegen.apt.test.ErraiAptTest;
import org.jboss.errai.common.apt.AnnotatedSourceElementsFinder;
import org.jboss.errai.common.apt.ResourceFilesFinder;
import org.junit.Assert;
import org.junit.Test;

import javax.lang.model.element.TypeElement;
import java.util.Set;

import static java.util.Collections.emptySet;

public class ErraiAppAptGeneratorTest extends ErraiAptTest {

  @Test
  public void testExceptionDoesNotBreakIt() {
    Assert.assertFalse(new ErraiAppAptGenerator() {
      @Override
      void generateAndSaveSourceFiles(Set<? extends TypeElement> annotations,
              AnnotatedSourceElementsFinder annotatedElementsFinder) {
        throw new TestException();
      }
    }.process(null, null));
  }

  @Test
  public void testProcessForEmptyAnnotationsSet() {
    Assert.assertFalse(new ErraiAppAptGenerator().process(emptySet(), null));
  }
}