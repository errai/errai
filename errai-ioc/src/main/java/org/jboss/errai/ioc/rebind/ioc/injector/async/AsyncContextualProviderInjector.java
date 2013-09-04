package org.jboss.errai.ioc.rebind.ioc.injector.async;

import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.meta.MetaParameterizedType;
import org.jboss.errai.codegen.meta.MetaType;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.client.container.async.CreationalCallback;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.Injector;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;
import org.jboss.errai.ioc.rebind.ioc.injector.basic.TypeInjector;
import org.jboss.errai.ioc.rebind.ioc.metadata.QualifyingMetadata;

import java.lang.annotation.Annotation;

/**
 * @author Mike Brock
 */
public class AsyncContextualProviderInjector extends TypeInjector {
  private final Injector providerInjector;

  public AsyncContextualProviderInjector(final MetaClass type,
                                         final MetaClass providerType,
                                         final InjectionContext context) {
    super(type, context);
    this.providerInjector = context.getInjectorFactory().getTypeInjector(providerType, context);
    context.registerInjector(providerInjector);

    this.testMock = context.isElementType(WiringElementType.TestMockBean, providerType);
    this.singleton = context.isElementType(WiringElementType.SingletonBean, providerType);
    this.alternative = context.isElementType(WiringElementType.AlternativeBean, providerType);

    setRendered(true);
  }

  @Override
  public void renderProvider(InjectableInstance injectableInstance) {
    providerInjector.renderProvider(injectableInstance);
  }

  @Override
  public Statement getBeanInstance(final InjectableInstance injectableInstance) {
    final MetaClass type;
    final MetaParameterizedType pType;

    switch (injectableInstance.getTaskType()) {
      case Type:
        return null;
      case PrivateField:
      case Field:
        final MetaField field = injectableInstance.getField();
        type = field.getType();

        pType = type.getParameterizedType();
        break;

      case Parameter:
        final MetaParameter parm = injectableInstance.getParm();
        type = parm.getType();

        pType = type.getParameterizedType();
        break;

      default:
        throw new RuntimeException("illegal task type: " + injectableInstance.getEnclosingType());
    }

    final MetaType[] typeArgs = pType.getTypeParameters();
    final MetaClass[] typeArgsClasses = new MetaClass[typeArgs.length];

    for (int i = 0; i < typeArgs.length; i++) {
      final MetaType argType = typeArgs[i];

      if (argType instanceof MetaClass) {
        typeArgsClasses[i] = (MetaClass) argType;
      }
      else if (argType instanceof MetaParameterizedType) {
        typeArgsClasses[i] = (MetaClass) ((MetaParameterizedType) argType).getRawType();
      }
    }

    final Annotation[] qualifiers = injectableInstance.getQualifiers();

    final BlockBuilder<?> block
        = injectableInstance.getInjectionContext().getProcessingContext().getBlockBuilder();
    final MetaClass providerCreationalCallback
        = MetaClassFactory.parameterizedAs(CreationalCallback.class,
        MetaClassFactory.typeParametersOf(providerInjector.getInjectedType()));

    final String varName = InjectUtil.getVarNameFromType(providerInjector.getConcreteInjectedType(), injectableInstance);

    final Statement valueRef;

    if (providerInjector.isSingleton() && providerInjector.isRendered()) {
      valueRef = Stmt.loadVariable("beanInstance").invoke("provide", typeArgsClasses,
          qualifiers.length != 0 ? qualifiers : null);
    }
    else {

      valueRef = Stmt.load(null);
    }

    block.append(
        Stmt.declareFinalVariable(varName, providerCreationalCallback,
            Stmt.newObject(providerCreationalCallback).extend()
                .publicOverridesMethod("callback", Parameter.of(providerInjector.getInjectedType(), "beanInstance"))
                  .append(Stmt.loadVariable(InjectUtil.getVarNameFromType(type, injectableInstance)).invoke("callback", valueRef))
                  .append(Stmt.loadVariable("async").invoke("finish", Refs.get("this")))
                .finish()
                .publicOverridesMethod("toString")
                  .append(Stmt.load(providerInjector.getInjectedType()).invoke("getName").returnValue())
                .finish()
             .finish())
    );

    block.append(Stmt.loadVariable("async").invoke("wait", Refs.get(varName)));

    block.append(
        Stmt.loadVariable(providerInjector.getCreationalCallbackVarName())
            .invoke("getInstance", Refs.get(varName), Refs.get("context"))
    );

    return null;
  }
  
  @Override
  public boolean matches(MetaParameterizedType parameterizedType, QualifyingMetadata qualifyingMetadata) {
    boolean parmTypesSatisfied = true;
    if (parameterizedType != null) {
      parmTypesSatisfied = parameterizedType.isAssignableFrom(getQualifyingTypeInformation());
    }

    return parmTypesSatisfied;
  }
}