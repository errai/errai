package org.reflections.scanners;

import org.reflections.util.FilterBuilder;

import java.util.List;

/** scans for superclass and interfaces of a class, allowing a reverse lookup for subtypes */
public class SubTypesScanner extends AbstractScanner {

    {
        filterResultsBy(new FilterBuilder().exclude(Object.class.getName())); //exclude direct Object subtypes by default
    }

    @SuppressWarnings({"unchecked"})
    public void scan(final Object cls) {
		String className = getMetadataAdapter().getClassName(cls);
		String superclass = getMetadataAdapter().getSuperclassName(cls);

        if (acceptResult(superclass)) {
            getStore().put(superclass, className);
        }

		for (String anInterface : (List<String>) getMetadataAdapter().getInterfacesNames(cls)) {
			if (acceptResult(anInterface)) {
                getStore().put(anInterface, className);
            }
        }
    }
}
