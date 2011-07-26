package org.jboss.errai.cdi.server;

import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.servlet.ServiceLocator;

public class CDIServiceLocator implements ServiceLocator {

  public ErraiService locateService() {
    BeanManager beanManager = Util.lookupBeanManager();

    Set<Bean<?>> beans = beanManager.getBeans(ErraiService.class);
    Bean<?> bean = beanManager.resolve(beans);
    CreationalContext<?> context = beanManager.createCreationalContext(bean);

    return (ErraiService) beanManager.getReference(bean, ErraiService.class, context);

  }
}
