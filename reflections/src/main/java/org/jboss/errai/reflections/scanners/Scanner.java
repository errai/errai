package org.jboss.errai.reflections.scanners;

import com.google.common.base.Predicate;
import com.google.common.collect.Multimap;
import org.jboss.errai.reflections.Configuration;
import org.jboss.errai.reflections.vfs.Vfs;

/**
 *
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
