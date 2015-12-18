package org.jboss.errai.cdi.async.test.postconstruct.client.res;

import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.async.AsyncBeanDef;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

/**
 * @author Mike Brock
 */
@ApplicationScoped
public class PostConstrAppBean {
  private boolean finished = false;

  @PostConstruct
  private void onPost() {
    final AsyncBeanDef<DepBeanWithPC> depBeanWithPCAsyncBeanDef = IOC.getAsyncBeanManager().lookupBean(DepBeanWithPC.class);
    depBeanWithPCAsyncBeanDef.getInstance(new CreationalCallback<DepBeanWithPC>() {
          @Override
          public void callback(DepBeanWithPC beanInstance) {
            if (!beanInstance.isPostConstructCalled()) {
              throw new RuntimeException("post construct was NOT called!");
            }
            finished = true;
          }
        });

  }

  public boolean isFinished() {
    return finished;
  }
}
