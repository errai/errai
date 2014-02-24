package org.jboss.errai.ioc.client.lifecycle.api;

/**
 * An event representing the end of an IOC bean instances lifecycle.
 * 
 * This event is special in that, if it is successful (i.e. no listeners
 * {@linkplain LifecycleEvent#veto() veto} it) then all references to
 * {@link LifecycleListener LifecycleListeners} for this instance will released.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public interface Destruction<T> extends LifecycleEvent<T> {

}
