package org.jboss.errai.common.apt.configuration;

import org.jboss.errai.codegen.apt.test.ErraiAptTest;
import org.jboss.errai.common.apt.AnnotatedSourceElementsFinder;
import org.jboss.errai.common.apt.TestAnnotatedSourceElementsFinder;
import org.jboss.errai.common.apt.configuration2.AnnotatedTypeOutOfModule;
import org.jboss.errai.common.apt.exportfile.ExportFile;
import org.junit.Assert;
import org.junit.Test;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.singleton;

public class ErraiModuleTest extends ErraiAptTest {

  @Test
  public void testFindAnnotatedClassesAndInterfacesForAnnotatedField() {
    final Element[] testExportedType = getTypeElement(
            TestExportableTypeWithFieldAnnotations.class).getEnclosedElements().toArray(new Element[0]);

    final TypeElement testEnclosedElementAnnotation = getTypeElement(TestEnclosedElementAnnotation.class);
    final ErraiModule erraiModule = getErraiModule(getTestAnnotatedElementsFinder(testExportedType));

    final Set<? extends Element> elements = erraiModule.findAnnotatedClassesAndInterfaces(
            testEnclosedElementAnnotation);
    Assert.assertTrue(elements.isEmpty());
  }

  @Test
  public void testFindAnnotatedClassesAndInterfacesForAnnotatedClass() {
    final TypeElement testAnnotation = getTypeElement(TestAnnotation.class);
    final TypeElement testExportedType = getTypeElement(TestExportableTypeWithFieldAnnotations.class);

    final ErraiModule testGenerator = getErraiModule(getTestAnnotatedElementsFinder(testExportedType));

    final Set<? extends Element> elements = testGenerator.findAnnotatedClassesAndInterfaces(testAnnotation);
    Assert.assertEquals(singleton(testExportedType), elements);
  }

  @Test
  public void testFindAnnotatedClassesAndInterfacesWithInnerClasses() {
    final TypeElement testAnnotation = getTypeElement(TestAnnotation.class);

    final TypeElement type = getTypeElement(AnnotatedTypeWithAnnotatedInnerClasses.class);
    final TypeElement innerType = getTypeElement(AnnotatedTypeWithAnnotatedInnerClasses.InnerAnnotatedType.class);
    final ErraiModule erraiModule = getErraiModule(getTestAnnotatedElementsFinder(type, innerType));

    final Set<Element> exportedTypes = erraiModule.findAnnotatedClassesAndInterfaces(testAnnotation);
    assertContainsOnly(exportedTypes, type, innerType);
  }

  @Test
  public void testFindAnnotatedClassesAndInterfacesWithTypesOutOfModuleScope() {
    final TypeElement testAnnotation = getTypeElement(TestAnnotation.class);

    final TypeElement annotatedTypeInsideModule = getTypeElement(AnnotatedTypeInsideModule.class);
    final ErraiModule erraiModule = getErraiModule(
            getTestAnnotatedElementsFinder(annotatedTypeInsideModule, getTypeElement(AnnotatedTypeOutOfModule.class)));

    final Set<Element> exportedTypes = erraiModule.findAnnotatedClassesAndInterfaces(testAnnotation);
    assertContainsOnly(exportedTypes, annotatedTypeInsideModule);
  }

  @Test
  public void testNewExportFileWithOneExportedType() {
    final TypeElement testAnnotation = getTypeElement(TestAnnotation.class);
    final TypeElement testExportedType = getTypeElement(TestExportableTypeWithFieldAnnotations.class);
    final TestAnnotatedSourceElementsFinder annotatedElementsFinder = getTestAnnotatedElementsFinder(testExportedType);

    final ErraiModule erraiModule = getErraiModule(annotatedElementsFinder);
    final Optional<ExportFile> exportFile = erraiModule.newExportFile(testAnnotation);

    Assert.assertTrue(exportFile.isPresent());
    Assert.assertEquals(1, exportFile.get().exportedTypes.size());
    Assert.assertTrue(exportFile.get().exportedTypes.contains(testExportedType));
  }

  @Test
  public void testNewExportFileWithNoExportedTypes() {
    final TypeElement testAnnotation = getTypeElement(TestAnnotation.class);

    final ErraiModule erraiModule = getErraiModule(getTestAnnotatedElementsFinder());
    final Optional<ExportFile> exportFile = erraiModule.newExportFile(testAnnotation);

    Assert.assertFalse(exportFile.isPresent());
  }

  @Test
  public void testErraiModuleUniqueNamespace() {
    final String moduleNamespace = getErraiModule(getTestAnnotatedElementsFinder()).erraiModuleUniqueNamespace();
    Assert.assertEquals("org_jboss_errai_common_apt_configuration_ErraiDefaultTestModule__test", moduleNamespace);
  }

  private TestAnnotatedSourceElementsFinder getTestAnnotatedElementsFinder(final Element... typeElements) {
    return new TestAnnotatedSourceElementsFinder(typeElements);
  }

  private ErraiModule getErraiModule(final AnnotatedSourceElementsFinder annotatedElementsFinder) {
    return new ErraiModule("test", getTypeElement(ErraiDefaultTestModule.class), annotatedElementsFinder);
  }

  private static void assertContainsOnly(final Set<?> actual, final Object... expected) {
    Assert.assertEquals(expected.length, actual.size());
    Assert.assertTrue(actual.containsAll(Arrays.asList(expected)));
  }

}