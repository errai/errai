package org.jboss.errai.codegen.framework.builder;

import org.jboss.errai.codegen.framework.Parameter;
import org.jboss.errai.codegen.framework.builder.impl.ObjectBuilder;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface AnonymousClassStructureBuilder
        extends ClassStructureBuilder<AnonymousClassStructureBuilder>, Finishable<ObjectBuilder> {

  public BlockBuilder<AnonymousClassStructureBuilder> initialize();

  public BlockBuilder<AnonymousClassStructureBuilder> publicOverridesMethod(String name, Parameter... args);
}
