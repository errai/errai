package org.jboss.errai.common.metadata;

public interface MetaDataProcessor<T> {
  void process(T context, MetaDataScanner reflections);
}