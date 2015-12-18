package org.jboss.errai.reflections.scanners;

import com.google.common.base.Predicate;
import com.google.common.collect.Multimap;
import org.jboss.errai.reflections.Configuration;
import org.jboss.errai.reflections.vfs.Vfs;

/**
 * Interface for scanning the class path. It is highly recommended that any
 * custom implementations subclass {@link AbstractScanner}.
 */
public interface Scanner {

  String getName();

  boolean acceptsInput(String file);

  void scan(Vfs.File file);

  Predicate<String> getResultFilter();

  Scanner filterResultsBy(Predicate<String> filter);

  void setConfiguration(Configuration configuration);

  void setStore(Multimap<String, String> store);
}
