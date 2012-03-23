package org.jboss.errai.codegen.framework.literal;

import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.BlockBuilder;
import org.jboss.errai.codegen.framework.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.util.Stmt;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class SetValue extends LiteralValue<Set> {
  public SetValue(Set value) {
    super(value);
  }

  @Override
  public String getCanonicalString(Context context) {
    BlockBuilder<AnonymousClassStructureBuilder> initBlock
            = ObjectBuilder.newInstanceOf(HashSet.class, context).extend().initialize();

    for (Object v : getValue()) {
      initBlock.append(Stmt.loadVariable("this").invoke("add", LiteralFactory.getLiteral(v)));
    }

    return initBlock.finish().finish().toJavaString();
  }
}
