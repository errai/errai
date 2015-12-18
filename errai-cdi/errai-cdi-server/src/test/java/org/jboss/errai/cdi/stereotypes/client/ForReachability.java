package org.jboss.errai.cdi.stereotypes.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * This class injects non-explicitly scoped beans so that they are available for
 * lookup by the bean manager. (Otherwise they would be removed for being
 * unreachable.)
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@ApplicationScoped
public class ForReachability {

  @Inject
  Reindeer reindeer;

}
