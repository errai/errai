package org.jboss.errai.bus.client.tests.support;

import org.jboss.errai.bus.server.annotations.Remote;

@Remote
public interface HibernateRpc {
  
  public HibernateObject getHibernateObject(Integer id);
  
  public void addHibernateObject(HibernateObject entity);
  
  public OtherHibernateObject getOther(Integer idOfParent);

}
