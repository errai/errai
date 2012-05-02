package org.jboss.errai.codegen.literal;

import org.jboss.errai.codegen.Context;
import org.jboss.errai.codegen.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.util.Stmt;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock
 */
public class ListValue extends LiteralValue<List> {
  public ListValue(List value) {
    super(value);
  }

  @Override
  public String getCanonicalString(Context context) {
    BlockBuilder<AnonymousClassStructureBuilder> initBlock
            = ObjectBuilder.newInstanceOf(ArrayList.class, context).extend().initialize();

    for (Object v : getValue()) {
      initBlock.append(Stmt.loadVariable("this").invoke("add", LiteralFactory.getLiteral(context, v)));
    }

    return initBlock.finish().finish().toJavaString();
  }
}
