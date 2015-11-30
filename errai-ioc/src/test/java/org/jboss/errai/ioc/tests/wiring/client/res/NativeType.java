package org.jboss.errai.ioc.tests.wiring.client.res;

import jsinterop.annotations.JsType;

/**
 * This type tests that we do not proxy abstract JsTypes. Proxying this type
 * will cause a compile error, since you cannot overload public methods on a
 * JsType.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@JsType(isNative = true)
public abstract class NativeType {

  public abstract void overloaded();

  public abstract void overloaded(Object obj);

}
