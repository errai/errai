package org.jboss.errai.common.apt.generator;

import org.jboss.errai.common.apt.AnnotatedSourceElementsFinder;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.processing.Filer;
import javax.lang.model.element.TypeElement;
import java.util.Set;

import static java.util.Collections.emptySet;

public class AbstractErraiModuleExportFileGeneratorTest {

  @Test
  public void testThrownExceptionDoesNotBreakIt() {
    Assert.assertFalse(new AbstractErraiModuleExportFileGenerator() {
      @Override
      protected String getCamelCaseErraiModuleName() {
        return "test";
      }

      @Override
      void generateAndSaveExportFiles(Set<? extends TypeElement> annotations,
              AnnotatedSourceElementsFinder annotatedSourceElementsFinder,
              Filer filer) {
        throw new TestException();
      }
    }.process(null, null));
  }

  @Test
  public void testProcessForEmptyAnnotationsSet() {
    Assert.assertFalse((new AbstractErraiModuleExportFileGenerator() {
      @Override
      protected String getCamelCaseErraiModuleName() {
        return "test";
      }
    }).process(emptySet(), null));
  }

}