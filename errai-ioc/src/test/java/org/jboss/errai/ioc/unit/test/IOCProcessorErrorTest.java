package org.jboss.errai.ioc.unit.test;

import static org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType.AlternativeBean;
import static org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType.DependentBean;
import static org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType.InjectionPoint;
import static org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType.NormalScopedBean;
import static org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType.ProducerElement;
import static org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType.Provider;
import static org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType.PseudoScopedBean;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;
import java.util.Arrays;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.meta.MetaClassCache;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.impl.java.JavaReflectionClass;
import org.jboss.errai.common.client.api.annotations.IOCProducer;
import org.jboss.errai.ioc.client.Bootstrapper;
import org.jboss.errai.ioc.client.api.IOCProvider;
import org.jboss.errai.ioc.client.container.ContextManager;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.FactoryGenerator;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessor;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraph;
import org.jboss.errai.ioc.rebind.ioc.graph.api.QualifierFactory;
import org.jboss.errai.ioc.rebind.ioc.graph.impl.DefaultQualifierFactory;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;
import org.jboss.errai.ioc.unit.res.BeanWithAlternativeDependency;
import org.jboss.errai.ioc.unit.res.DepCycleA;
import org.jboss.errai.ioc.unit.res.DepCycleB;
import org.jboss.errai.ioc.unit.res.DependencyIface;
import org.jboss.errai.ioc.unit.res.DisabledAlternative;
import org.jboss.errai.ioc.unit.res.DisabledAlternativeContextualProvider;
import org.jboss.errai.ioc.unit.res.DisabledAlternativeProducerField;
import org.jboss.errai.ioc.unit.res.DisabledAlternativeProducerMethod;
import org.jboss.errai.ioc.unit.res.DisabledAlternativeProvider;
import org.jboss.errai.ioc.unit.res.PseudoCycleA;
import org.jboss.errai.ioc.unit.res.PseudoCycleB;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.HashMultimap;

/**
 * Unit tests failing dependency graphs for proper error messages.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@RunWith(MockitoJUnitRunner.class)
public class IOCProcessorErrorTest {

  private IOCProcessor processor;

  @Mock
  private InjectionContext injContext;

  @Mock
  private IOCProcessingContext procContext;

  @Mock
  private MetaClassCache cache;

  @Before
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void setup() {
    MetaClassFactory.getMetaClassCache().clear();
    FactoryGenerator.setDependencyGraph(null);

    final QualifierFactory qualFactory = new DefaultQualifierFactory();
    when(injContext.getQualifierFactory()).thenReturn(qualFactory);
    when(injContext.getInjectableProviders()).thenReturn(HashMultimap.create());
    when(injContext.getExactTypeInjectableProviders()).thenReturn(HashMultimap.create());
    when(injContext.getAnnotationsForElementType(DependentBean)).thenReturn(Arrays.asList(Dependent.class));
    when(injContext.getAnnotationsForElementType(NormalScopedBean)).thenReturn(Arrays.asList(ApplicationScoped.class));
    when(injContext.getAnnotationsForElementType(PseudoScopedBean)).thenReturn(Arrays.asList(Singleton.class, Dependent.class));
    when(injContext.getAnnotationsForElementType(AlternativeBean)).thenReturn(Arrays.asList(Alternative.class));
    when(injContext.getAnnotationsForElementType(InjectionPoint)).thenReturn(Arrays.asList(Inject.class));
    when(injContext.getAnnotationsForElementType(ProducerElement)).thenReturn(Arrays.asList(IOCProducer.class));
    when(injContext.getAnnotationsForElementType(Provider)).thenReturn(Arrays.asList(IOCProvider.class));
    when(injContext.isWhitelisted(any())).thenReturn(true);
    when(injContext.isBlacklisted(any())).thenReturn(false);
    when(injContext.isElementType(any(), (Class<? extends Annotation>) any()))
      .then(inv -> injContext.getAnnotationsForElementType((WiringElementType) inv.getArguments()[0]).contains(inv.getArguments()[1]));

    final ClassStructureBuilder<?> classBuilder = ClassBuilder
            .define("org.jboss.errai.ioc.FakeBootstrapperImpl")
            .publicScope()
            .implementsInterface(Bootstrapper.class)
            .body();
    final BlockBuilder blockBuilder = classBuilder.publicMethod(ContextManager.class, "bootstrap").body();

    when(procContext.getBlockBuilder()).thenReturn(blockBuilder);
    when(procContext.getBootstrapBuilder()).thenReturn(classBuilder);
    when(procContext.getBootstrapClass()).thenReturn(classBuilder.getClassDefinition());

    processor = new IOCProcessor(injContext);
  }

  @Test
  public void hintWhenUnsatisfiedTypeHasMissingAlternative() throws Exception {
    addToMetaClassCache(
            Object.class,
            DependencyIface.class,
            BeanWithAlternativeDependency.class,
            DisabledAlternative.class);

    final String injSiteTypeName = DependencyIface.class.getName();
    final String typeWithDepName = BeanWithAlternativeDependency.class.getName();
    final String disabledTypeName = DisabledAlternative.class.getName();

    assertDisabledTypeReported(injSiteTypeName, typeWithDepName, disabledTypeName);
  }

  @Test
  public void hintWhenUnsatisfiedTypeHasMissingAlternativeProducerMethod() throws Exception {
    addToMetaClassCache(
            Object.class,
            DependencyIface.class,
            BeanWithAlternativeDependency.class,
            DisabledAlternativeProducerMethod.class);

    final String injSiteTypeName = DependencyIface.class.getName();
    final String typeWithDepName = BeanWithAlternativeDependency.class.getName();
    final String disabledTypeName = DisabledAlternativeProducerMethod.class.getName();

    assertDisabledTypeReported(injSiteTypeName, typeWithDepName, disabledTypeName);
  }

  @Test
  public void hintWhenUnsatisfiedTypeHasMissingAlternativeProducerField() throws Exception {
    addToMetaClassCache(
            Object.class,
            DependencyIface.class,
            BeanWithAlternativeDependency.class,
            DisabledAlternativeProducerField.class);

    final String injSiteTypeName = DependencyIface.class.getName();
    final String typeWithDepName = BeanWithAlternativeDependency.class.getName();
    final String disabledTypeName = DisabledAlternativeProducerField.class.getName();

    assertDisabledTypeReported(injSiteTypeName, typeWithDepName, disabledTypeName);
  }

  @Test
  public void hintWhenUnsatisfiedTypeHasMissingAlternativeProvider() throws Exception {
    addToMetaClassCache(
            Object.class,
            DependencyIface.class,
            BeanWithAlternativeDependency.class,
            DisabledAlternativeProvider.class);

    final String injSiteTypeName = DependencyIface.class.getName();
    final String typeWithDepName = BeanWithAlternativeDependency.class.getName();
    final String disabledTypeName = DisabledAlternativeProvider.class.getName();

    assertDisabledTypeReported(injSiteTypeName, typeWithDepName, disabledTypeName);
  }

  @Test
  public void hintWhenUnsatisfiedTypeHasMissingAlternativeContextualProvider() throws Exception {
    addToMetaClassCache(
            Object.class,
            DependencyIface.class,
            BeanWithAlternativeDependency.class,
            DisabledAlternativeContextualProvider.class);

    final String injSiteTypeName = DependencyIface.class.getName();
    final String typeWithDepName = BeanWithAlternativeDependency.class.getName();
    final String disabledTypeName = DisabledAlternativeContextualProvider.class.getName();

    assertDisabledTypeReported(injSiteTypeName, typeWithDepName, disabledTypeName);
  }

  @Test
  public void doNotMakeDisabledAlternativesAvailableForLookup() throws Exception {
    addToMetaClassCache(
            Object.class,
            DependencyIface.class,
            DisabledAlternative.class);

    processor.process(procContext);
    final DependencyGraph graph = FactoryGenerator.getDependencyGraph();
    assertNotNull("The dependency graph was not set.", graph);
    assertEquals("The dependency graph should not have any injectables.", 0, graph.getNumberOfInjectables());
  }

  @Test
  public void dependentCycleCausesError() throws Exception {
    addToMetaClassCache(
            Object.class,
            DepCycleA.class,
            DepCycleB.class);

    try {
      processor.process(procContext);
      fail("Did not produce error for @Depenent scope cycle.");
    } catch (RuntimeException e) {
      final String message = e.getMessage();
      assertTrue(
              "Message did not reference types in dependent scoped cycle.\n\tMessage: " + message,
              message.contains(DepCycleA.class.getSimpleName()) && message.contains(DepCycleB.class.getSimpleName()));
    }
  }

  @Test
  public void pseudoScopeCycleCausesError() throws Exception {
    addToMetaClassCache(
            Object.class,
            PseudoCycleA.class,
            PseudoCycleB.class);

    try {
      processor.process(procContext);
      fail("Did not produce error for pseudo scope cycle.");
    } catch (RuntimeException e) {
      final String message = e.getMessage();
      assertTrue(
              "Message did not reference types in pseudo scoped cycle.\n\tMessage: " + message,
              message.contains(PseudoCycleA.class.getSimpleName()) && message.contains(PseudoCycleB.class.getSimpleName()));
    }
  }

  private void assertDisabledTypeReported(final String injSiteTypeName, final String typeWithDepName, final String disabledTypeName)
          throws AssertionError {
    try {
      processor.process(procContext);
      fail("Calling process should have caused an error from an unsatisfied dependency.");
    } catch (NullPointerException npe) {
      throw npe;
    } catch (RuntimeException ex) {
      // rethrow exception if preconditions not met
      try {
        assertNotNull("Message of " + ex.getClass().getSimpleName() + " should not have been null.", ex.getMessage());
        assertTrue("IOC error did not mention unsatisfied type: " + ex.getMessage(), ex.getMessage().contains(injSiteTypeName));
        assertTrue("IOC error did not mention the type with the unsatisfied injection site: " + ex.getMessage(),
                ex.getMessage().contains(typeWithDepName));
        assertTrue("IOC error contains two unsatisfied dependencies. Should only containe one.",
                ex.getMessage().indexOf("Unsatisfied") == ex.getMessage().lastIndexOf("Unsatisfied"));
      } catch (AssertionError ae) {
        throw new AssertionError(ae.getMessage(), ex);
      }

      assertTrue("IOC error did not mention the disabled alternative that satisfies the injection site: " + ex.getMessage(),
              ex.getMessage().contains(disabledTypeName));
    }
  }

  private void addToMetaClassCache(final Class<?>... metaClasses) {
    Arrays.stream(metaClasses)
          .map(type -> JavaReflectionClass.newInstance(type))
          .forEach(mc -> MetaClassFactory.getMetaClassCache().pushCache(mc));
  }

}
