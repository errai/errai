package org.jboss.errai.ioc.tests.qualifiers.client.res;

import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Michel Werren
 * @since 05.07.2012
 */
@Qualifier
@Retention( RetentionPolicy.RUNTIME )
@Target( {ElementType.TYPE, ElementType.FIELD} )
public @interface AQualifier
{
}
