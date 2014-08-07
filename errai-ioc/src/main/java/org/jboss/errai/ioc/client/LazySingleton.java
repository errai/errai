package org.jboss.errai.ioc.client;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
* When used in addition to {@link javax.inject.Singleton}  to force the lazy instantiation of the singleton.
* </br>
* <strong>NOTE:</strong> Requires AsyncBeanManager
*
*/
@Retention(RUNTIME)
@Target({TYPE,ElementType.METHOD})
@Documented
public @interface LazySingleton {
    
}
