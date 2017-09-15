package org.jboss.errai.common.apt.configuration;

import org.jboss.errai.codegen.apt.test.ErraiAptTest;
import org.jboss.errai.common.apt.AnnotatedSourceElementsFinder;
import org.jboss.errai.common.apt.TestAnnotatedSourceElementsFinder;
import org.jboss.errai.common.apt.exportfile.ExportFile;
import org.junit.Assert;
import org.junit.Test;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.singleton;

public class ErraiModuleTest extends ErraiAptTest {

  @Test
  public void testAnnotatedClassesAndInterfacesForAnnotatedField() {
    final Element[] testExportedType = getTypeElement(
            TestExportableTypeWithFieldAnnotations.class).getEnclosedElements().toArray(new Element[0]);

    final TypeElement testEnclosedElementAnnotation = getTypeElement(TestEnclosedElementAnnotation.class);

    final ErraiModule testGenerator = getErraiModuleClass(annotatedElementsFinder(testExportedType));

    final Set<? extends Element> elements = testGenerator.annotatedClassesAndInterfaces(testEnclosedElementAnnotation);
    Assert.assertTrue(elements.isEmpty());
  }

  @Test
  public void testAnnotatedClassesAndInterfacesForAnnotatedClass() {
    final TypeElement testAnnotation = getTypeElement(TestAnnotation.class);
    final TypeElement testExportedType = getTypeElement(TestExportableTypeWithFieldAnnotations.class);

    final ErraiModule testGenerator = getErraiModuleClass(annotatedElementsFinder(testExportedType));

    final Set<? extends Element> elements = testGenerator.annotatedClassesAndInterfaces(testAnnotation);
    Assert.assertEquals(singleton(testExportedType), elements);
  }

  @Test
  public void testNewExportFileWithOneExportedType() {
    final TypeElement testAnnotation = getTypeElement(TestAnnotation.class);
    final TypeElement testExportedType = getTypeElement(TestExportableTypeWithFieldAnnotations.class);
    final TestAnnotatedSourceElementsFinder annotatedElementsFinder = annotatedElementsFinder(testExportedType);

    final ErraiModule testGenerator = getErraiModuleClass(annotatedElementsFinder);
    final Optional<ExportFile> exportFile = testGenerator.newExportFile(testAnnotation);

    Assert.assertTrue(exportFile.isPresent());
    Assert.assertEquals(1, exportFile.get().exportedTypes.size());
    Assert.assertTrue(exportFile.get().exportedTypes.contains(testExportedType));
  }

  @Test
  public void testNewExportFileWithNoExportedTypes() {
    final TypeElement testAnnotation = getTypeElement(TestAnnotation.class);

    final ErraiModule testGenerator = getErraiModuleClass(annotatedElementsFinder());
    final Optional<ExportFile> exportFile = testGenerator.newExportFile(testAnnotation);

    Assert.assertFalse(exportFile.isPresent());
  }

  private TestAnnotatedSourceElementsFinder annotatedElementsFinder(final Element... typeElements) {
    return new TestAnnotatedSourceElementsFinder(typeElements);
  }

  private ErraiModule getErraiModuleClass(final AnnotatedSourceElementsFinder annotatedElementsFinder) {
    return new ErraiModule("test", getTypeElement(ErraiTestDefaultModule.class), annotatedElementsFinder);
  }

}