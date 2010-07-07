package org.jboss.errai.ioc.rebind.ioc;

import com.google.gwt.core.ext.typeinfo.JClassType;

public abstract class Injector {
    public abstract String getType(InjectionContext injectContext);
    public abstract boolean isInjected();
    public abstract String getVarName();
    public abstract JClassType getInjectedType();

}

