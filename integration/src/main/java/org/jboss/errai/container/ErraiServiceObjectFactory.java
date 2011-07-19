package org.jboss.errai.container;

import org.jboss.errai.bus.server.service.ErraiService;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
import java.util.Hashtable;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class ErraiServiceObjectFactory implements ObjectFactory {
  private ErraiService service;

  public ErraiServiceObjectFactory() {
    service = ServiceFactory.create();
  }

  @Override
  public Object getObjectInstance(Object o, Name name, Context context, Hashtable<?, ?> hashtable) throws Exception {
    return service;
  }
}
