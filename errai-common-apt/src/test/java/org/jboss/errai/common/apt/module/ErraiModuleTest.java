package org.jboss.errai.common.apt.module;

import org.jboss.errai.codegen.apt.test.ErraiAptTest;
import org.jboss.errai.common.apt.AnnotatedSourceElementsFinder;
import org.jboss.errai.common.apt.TestAnnotatedSourceElementsFinder;
import org.jboss.errai.common.apt.configuration.TestAnnotation;
import org.jboss.errai.common.apt.exportfile.ExportFile;
import org.jboss.errai.common.apt.module2.AnnotatedTypeOutOfModule;
import org.jboss.errai.common.apt.strategies.ErraiExportingStrategiesFactory;
import org.jboss.errai.common.apt.strategies.ExportedElement;
import org.junit.Assert;
import org.junit.Test;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class ErraiModuleTest extends ErraiAptTest {

  @Test
  public void testFindAnnotatedClassesAndInterfacesForAnnotatedField() {
    final Element[] testExportedTypes = getTypeElement(
            TestExportableTypeWithFieldAnnotations.class).getEnclosedElements().toArray(new Element[0]);

    final TypeElement testEnclosedElementAnnotation = getTypeElement(TestEnclosedElementAnnotation.class);
    final ErraiModule erraiModule = getErraiModule(getTestAnnotatedElementsFinder(testExportedTypes));

    final List<ExportedElement> elements = new ArrayList<>(
            erraiModule.findExportedElements(testEnclosedElementAnnotation));
    Assert.assertEquals(1, elements.size());
    Assert.assertEquals(getTypeElement(String.class).asType(), elements.get(0).getElement().asType());
  }

  @Test
  public void testFindAnnotatedClassesAndInterfacesForAnnotatedClass() {
    final TypeElement testAnnotation = getTypeElement(TestAnnotation.class);
    final TypeElement testExportedType = getTypeElement(TestExportableTypeWithFieldAnnotations.class);

    final ErraiModule erraiModule = getErraiModule(getTestAnnotatedElementsFinder(testExportedType));

    final List<ExportedElement> elements = new ArrayList<>(erraiModule.findExportedElements(testAnnotation));
    Assert.assertEquals(1, elements.size());
    Assert.assertEquals(testExportedType.asType(), elements.get(0).getElement().asType());
  }

  @Test
  public void testFindAnnotatedClassesAndInterfacesWithInnerClasses() {
    final TypeElement testAnnotation = getTypeElement(TestAnnotation.class);
    final TypeElement type = getTypeElement(AnnotatedTypeWithAnnotatedInnerClasses.class);
    final TypeElement innerStaticType = getTypeElement(AnnotatedTypeWithAnnotatedInnerClasses.InnerAnnotatedStaticType.class);
    final TypeElement innerType = getTypeElement(AnnotatedTypeWithAnnotatedInnerClasses.InnerAnnotatedType.class);

    final ErraiModule erraiModule = getErraiModule(getTestAnnotatedElementsFinder(
            Stream.concat(Stream.of(type), type.getEnclosedElements().stream()).toArray(Element[]::new)));

    final Set<ExportedElement> exportedElements = erraiModule.findExportedElements(testAnnotation);
    Assert.assertEquals(3, exportedElements.size());

    final Set<TypeMirror> exportedTypes = exportedElements.stream().map(s -> s.getElement().asType()).collect(toSet());
    assertContainsOnly(exportedTypes, type.asType(), innerStaticType.asType(), innerType.asType());
  }

  @Test
  public void testFindAnnotatedClassesAndInterfacesWithTypesOutOfModuleScope() {
    final TypeElement testAnnotation = getTypeElement(TestAnnotation.class);
    final TypeElement annotatedTypeInsideModule = getTypeElement(AnnotatedTypeInsideModule.class);

    final ErraiModule erraiModule = getErraiModule(
            getTestAnnotatedElementsFinder(annotatedTypeInsideModule, getTypeElement(AnnotatedTypeOutOfModule.class)));

    final Set<ExportedElement> exportedElements = erraiModule.findExportedElements(testAnnotation);
    final Set<TypeMirror> exportedTypes = exportedElements.stream().map(s -> s.getElement().asType()).collect(toSet());
    Assert.assertEquals(1, exportedElements.size());
    assertContainsOnly(exportedTypes, annotatedTypeInsideModule.asType());
  }

  @Test
  public void testNewExportFileWithOneExportedType() {
    final TypeElement testAnnotation = getTypeElement(TestAnnotation.class);
    final TypeElement testExportedType = getTypeElement(TestExportableTypeWithFieldAnnotations.class);

    final ErraiModule erraiModule = getErraiModule(getTestAnnotatedElementsFinder(testExportedType));
    final List<ExportFile> exportFiles = erraiModule.createExportFiles(singleton(testAnnotation)).collect(toList());

    Assert.assertEquals(1, exportFiles.size());
    Assert.assertEquals(1, exportFiles.get(0).exportedTypes().size());
    Assert.assertTrue(exportFiles.get(0).exportedTypes().contains(testExportedType.asType()));
  }

  @Test
  public void testNewExportFileWithNoExportedTypes() {
    final TypeElement testAnnotation = getTypeElement(TestAnnotation.class);

    final ErraiModule erraiModule = getErraiModule(getTestAnnotatedElementsFinder());
    final List<ExportFile> exportFiles = erraiModule.createExportFiles(singleton(testAnnotation)).collect(toList());

    Assert.assertTrue(exportFiles.isEmpty());
  }

  @Test
  public void testErraiModuleUniqueNamespace() {
    final String moduleNamespace = getErraiModule(getTestAnnotatedElementsFinder()).erraiModuleUniqueNamespace();
    Assert.assertEquals("org_jboss_errai_common_apt_module_ErraiDefaultTestModule__test", moduleNamespace);
  }

  private TestAnnotatedSourceElementsFinder getTestAnnotatedElementsFinder(final Element... typeElements) {
    return new TestAnnotatedSourceElementsFinder(typeElements);
  }

  private ErraiModule getErraiModule(final AnnotatedSourceElementsFinder annotatedElementsFinder) {
    return new ErraiModule("test", aptClass(ErraiDefaultTestModule.class), annotatedElementsFinder,
            new ErraiExportingStrategiesFactory(elements).buildFrom());
  }

  private static void assertContainsOnly(final Set<?> actual, final Object... expected) {
    Assert.assertEquals(expected.length, actual.size());
    Assert.assertTrue(actual.containsAll(Arrays.asList(expected)));
  }

}