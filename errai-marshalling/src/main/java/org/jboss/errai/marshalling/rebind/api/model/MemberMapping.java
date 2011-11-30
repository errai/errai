package org.jboss.errai.marshalling.rebind.api.model;

import org.jboss.errai.codegen.framework.meta.MetaClassMember;

/**
 * @author Mike Brock
 */
public interface MemberMapping extends Mapping {
  public MetaClassMember getMember();
}
