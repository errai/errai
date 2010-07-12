package org.jboss.errai.ioc.rebind.ioc;


import com.google.gwt.core.ext.typeinfo.JClassType;

import javax.inject.Singleton;

/**
 * User: christopherbrock
 * Date: 7-Jul-2010
 * Time: 1:00:03 PM
 */
public class TypeInjector extends Injector {
    protected final JClassType type;
    protected boolean injected;
    protected boolean singleTon;
    protected String varName;

    public TypeInjector(JClassType type) {
        this.type = type;
        this.singleTon = type.isAnnotationPresent(Singleton.class)
                || type.isAnnotationPresent(com.google.inject.Singleton.class);
        this.varName = InjectUtil.getNewVarName();
    }

    @Override
    public String getType(InjectionContext injectContext) {
        if (isInjected()) {
            if (isSingleton()) {
                return varName;
            } else {
                varName = InjectUtil.getNewVarName();
            }
        }

        ConstructionStrategy cs = InjectUtil.getConstructionStrategy(this, injectContext);

        String generated = cs.generateConstructor();
        injectContext.getProcessingContext().getWriter().print(generated);

        injected = true;

        return varName;
    }

    @Override
    public String instantiateOnly(InjectionContext injectContext) {
        return getType(injectContext);
    }

    @Override
    public boolean isInjected() {
        return injected;
    }

    @Override
    public boolean isSingleton() {
        return singleTon;
    }

    @Override
    public String getVarName() {
        return varName;
    }

    @Override
    public JClassType getInjectedType() {
        return type;
    }


}
