package org.jboss.errai.as7support;

import org.jboss.as.naming.ManagedReference;
import org.jboss.as.naming.ManagedReferenceFactory;
import org.jboss.as.naming.NamingStore;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.naming.service.BinderService;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.server.ServerMessageBusImpl;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.jboss.errai.bus.server.service.ErraiServiceConfiguratorImpl;
import org.jboss.errai.bus.server.service.ErraiServiceImpl;
import org.jboss.msc.service.DuplicateServiceException;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ErraiServiceActivator implements ServiceActivator {
  private Logger log = LoggerFactory.getLogger(ErraiServiceActivator.class);

  @Override
  public void activate(ServiceActivatorContext serviceActivatorContext) throws ServiceRegistryException {

    log.info("JBoss AS 7 Service Activator initialized ...");

    final ServiceName bindingServiceName = ContextNames.GLOBAL_CONTEXT_SERVICE_NAME
            .append("ErraiService");

    final BinderService binderService = new BinderService("ErraiService");
    ServiceBuilder<ManagedReferenceFactory> builder = serviceActivatorContext.getServiceTarget()
            .addService(bindingServiceName, binderService);
    builder.addDependency(ContextNames.GLOBAL_CONTEXT_SERVICE_NAME, NamingStore.class,
            binderService.getNamingStoreInjector());
    binderService.getManagedObjectInjector().inject(new ManagedReferenceFactory() {
      private volatile ErraiService service;

      private void init() {
        service = Guice.createInjector(new AbstractModule() {
          public void configure() {
            bind(MessageBus.class).to(ServerMessageBusImpl.class);
            bind(ServerMessageBus.class).to(ServerMessageBusImpl.class);
            bind(ErraiService.class).to(ErraiServiceImpl.class);
            bind(ErraiServiceConfigurator.class).to(ErraiServiceConfiguratorImpl.class);
          }
        }).getInstance(ErraiService.class);
      }

      @Override
      public synchronized ManagedReference getReference() {
        if (service == null) {
          init();
        }
        return new ManagedReference() {
          @Override
          public void release() {
            service.stopService();
          }

          @Override
          public Object getInstance() {
            return service;
          }
        };
      }
    });

    try {
      builder.install();
    }
    catch (DuplicateServiceException dse) {
      log.info("Service already registered.");
    }

    log.info("bound errai service to JNDI context: java:global/ErraiService");
  }
}