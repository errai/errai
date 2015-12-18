package org.jboss.errai.ioc.tests.qualifiers.client.res;

import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;

/**
 * @author Michel Werren
 * @since 05.07.2012
 */
@EntryPoint
public class InjectionPoint
{
	@AQualifier
	@Inject
	private A a;
	
	public A getInjected() {
	  return a;
	}
}
