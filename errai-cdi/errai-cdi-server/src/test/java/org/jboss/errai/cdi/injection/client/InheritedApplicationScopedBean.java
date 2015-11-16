package org.jboss.errai.cdi.injection.client;

import javax.enterprise.inject.Any;

/**
 * @author Mike Brock
 */
@Any // Ensures this is not @Default, preventing ambiguous resolution.
public class InheritedApplicationScopedBean extends ApplicationScopedBean {
}
