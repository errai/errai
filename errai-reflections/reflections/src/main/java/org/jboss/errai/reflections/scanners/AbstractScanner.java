package org.jboss.errai.reflections.scanners;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Multimap;
import org.jboss.errai.reflections.Configuration;
import org.jboss.errai.reflections.ReflectionsException;
import org.jboss.errai.reflections.adapters.MetadataAdapter;
import org.jboss.errai.reflections.util.Utils;
import org.jboss.errai.reflections.vfs.Vfs;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
@SuppressWarnings({"RawUseOfParameterizedType", "unchecked"})
public abstract class AbstractScanner implements Scanner {

	private Configuration configuration;
	private Multimap<String, String> store;
	private Predicate<String> resultFilter = Predicates.alwaysTrue(); //accept all by default

    public String getName() {
        return getClass().getName();
    }

    public boolean acceptsInput(final String file) {
        return file.endsWith(".class"); //is a class file
    }

    public void scan(final Vfs.File file) {
        InputStream inputStream = null;
        try {
            inputStream = file.openInputStream();
            final Object cls = configuration.getMetadataAdapter().createClassObject(inputStream);
            scan(cls);
        } catch (IOException e) {
            throw new ReflectionsException("could not create class file from " + file.getName(), e);
        } finally {
            Utils.close(inputStream);
        }
    }

    public abstract void scan(Object cls);

    //
    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(final Configuration configuration) {
        this.configuration = configuration;
    }

    public Multimap<String, String> getStore() {
        return store;
    }

    public void setStore(final Multimap<String, String> store) {
        this.store = store;
    }

    public Predicate<String> getResultFilter() {
        return resultFilter;
    }

    public void setResultFilter(final Predicate<String> resultFilter) {
        this.resultFilter = resultFilter;
    }

    public Scanner filterResultsBy(final Predicate<String> filter) {
        this.setResultFilter(filter); return this;
    }

    //
    protected boolean acceptResult(final String fqn) {
		return fqn != null && getResultFilter().apply(fqn);
	}

	protected MetadataAdapter getMetadataAdapter() {
		return configuration.getMetadataAdapter();
	}
}
