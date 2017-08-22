package org.jboss.errai.apt;

import org.jboss.errai.apt.internal.generator.TestExportedGenerator;
import org.jboss.errai.codegen.apt.test.ErraiAptTest;
import org.jboss.errai.common.apt.AnnotatedElementsFinder;
import org.jboss.errai.common.apt.ErraiAptGenerator;
import org.junit.Assert;
import org.junit.Test;

import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptySet;

public class ErraiAppAptGeneratorTest extends ErraiAptTest {

  @Test
  public void testExceptionDoesNotBreakIt() {
    Assert.assertFalse(new ErraiAppAptGenerator() {
      @Override
      void generateAndSaveSourceFiles(Set<? extends TypeElement> annotations,
              AnnotatedElementsFinder annotatedElementsFinder) {
        throw new TestException();
      }
    }.process(null, null));
  }

  @Test
  public void testProcessForEmptyAnnotationsSet() {
    Assert.assertFalse(new ErraiAppAptGenerator().process(emptySet(), null));
  }

  @Test
  public void testFindGenerators() {
    final List<ErraiAptGenerator> generators = new ErraiAppAptGenerator().findGenerators(elements);

    Assert.assertEquals(1, generators.size());
    Assert.assertEquals(TestExportedGenerator.class, generators.get(0).getClass());
  }
}