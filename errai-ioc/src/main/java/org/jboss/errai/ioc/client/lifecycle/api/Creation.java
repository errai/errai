package org.jboss.errai.ioc.client.lifecycle.api;

/**
 * An event for the creation of a new bean instance.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public interface Creation<T> extends LifecycleEvent<T> {

}
