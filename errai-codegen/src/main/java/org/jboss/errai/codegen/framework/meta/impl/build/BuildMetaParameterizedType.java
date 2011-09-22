package org.jboss.errai.codegen.framework.meta.impl.build;

import org.jboss.errai.codegen.framework.meta.MetaType;
import org.jboss.errai.codegen.framework.meta.impl.AbstractMetaParameterizedType;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class BuildMetaParameterizedType extends AbstractMetaParameterizedType {
  private MetaType[] types;
  private MetaType ownerType;
  private MetaType rawType;

  public BuildMetaParameterizedType(MetaType[] types, MetaType ownerType, MetaType rawType) {
    this.types = types;
    this.ownerType = ownerType;
    this.rawType = rawType;
  }

  @Override
  public MetaType[] getTypeParameters() {
    return types;
  }

  @Override
  public MetaType getOwnerType() {
    return ownerType;
  }

  @Override
  public MetaType getRawType() {
    return rawType;
  }
}
