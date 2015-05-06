package org.jboss.errai.reflections.scanners;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javassist.bytecode.ClassFile;

import org.jboss.errai.reflections.Configuration;
import org.jboss.errai.reflections.ReflectionsException;
import org.jboss.errai.reflections.adapters.MetadataAdapter;
import org.jboss.errai.reflections.scanners.reg.ScannerRegistry;
import org.jboss.errai.reflections.util.Utils;
import org.jboss.errai.reflections.vfs.Vfs;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Multimap;

public abstract class AbstractScanner implements Scanner {
    private static final Set<String> classesNotInJar = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
  
    private Configuration configuration;
	private Multimap<String, String> store;
	private Predicate<String> resultFilter = Predicates.alwaysTrue(); //accept all by default
	

	
	public AbstractScanner() {
	  /*
	   * Used to register all scanner sub-types names.
	   */
	  ScannerRegistry.getRegistry().setName(this.getClass(), getName());
	}

  /**
   * Get the name of this scanner. This is normally the class name of the
   * scanner. Subclasses designed to override the behaviour of built-in scanners
   * should return the name of the scanner they wish to override.
   */
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
            String fp = file.getFullPath();
            if (fp != null && !fp.contains(".jar")) {
                String className = ((ClassFile) cls).getName();
                if (!classesNotInJar.contains(className)) {
                  classesNotInJar.add(className);
                }
            }
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

    protected boolean acceptResult(final String fqn) {
		return fqn != null && getResultFilter().apply(fqn);
	}

	protected MetadataAdapter getMetadataAdapter() {
		return configuration.getMetadataAdapter();
	}
	
	public static boolean isInJar(String className) {
	  return !(classesNotInJar.contains(className));
	}
}
