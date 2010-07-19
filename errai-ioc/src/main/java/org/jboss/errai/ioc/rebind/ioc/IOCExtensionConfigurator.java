package org.jboss.errai.ioc.rebind.ioc;

import org.jboss.errai.bus.rebind.ProcessingContext;
import org.jboss.errai.ioc.rebind.ProcessorFactory;

/**
 * User: christopherbrock
 * Date: 19-Jul-2010
 * Time: 3:44:39 PM
 */
public interface IOCExtensionConfigurator {
    public void configure(ProcessingContext context, InjectorFactory injectorFactory, ProcessorFactory procFactory);
}
