package org.jboss.errai.ioc.rebind.ioc;


import com.google.gwt.core.ext.typeinfo.JClassType;

/**
 * User: christopherbrock
 * Date: 7-Jul-2010
 * Time: 1:00:03 PM
 */
public class TypeInjector extends Injector {
    protected final JClassType type;
    protected boolean injected;
    protected final String varName = InjectUtil.getNewVarName();

    public TypeInjector(JClassType type) {
        this.type = type;
    }

    @Override
    public String getType(InjectionContext injectContext) {
        if (isInjected()) return varName;

        ConstructionStrategy cs = InjectUtil.getConstructionStrategy(this, injectContext);

        String generated = cs.generateConstructor();
        injectContext.getProcessingContext().getWriter().print(generated);

        injected = true;

        return varName;
    }

    @Override
    public boolean isInjected() {
        return injected;
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
