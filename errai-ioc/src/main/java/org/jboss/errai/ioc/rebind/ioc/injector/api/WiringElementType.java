package org.jboss.errai.ioc.rebind.ioc.injector.api;

/**
 * @author Mike Brock
 */
public enum WiringElementType {
  Type,
  QualifiyingType,
  SingletonBean,
  DependentBean,
  ContextualTopLevelProvider,
  TopLevelProvider,
  InjectionPoint,
  ProducerElement,
  AlternativeBean,
  TestMockBean,
  NotSupported
}
