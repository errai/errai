package org.jboss.errai.marshalling.rebind.util;

import com.google.gwt.json.client.JSONString;
import org.jboss.errai.codegen.framework.Cast;
import org.jboss.errai.codegen.framework.DefParameters;
import org.jboss.errai.codegen.framework.Parameter;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.impl.AnonymousClassStructureBuilderImpl;
import org.jboss.errai.codegen.framework.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.framework.util.Implementations;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingContext;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MarshallingUtil {
  public static Statement marshallerForString(String fieldName) {
    AnonymousClassStructureBuilderImpl classStructureBuilder
            = ObjectBuilder.newInstanceOf(Marshaller.class).extend();

    classStructureBuilder.publicOverridesMethod("getTypeHandled")
            .append(Stmt.load(String.class).returnValue())
            .finish();

    classStructureBuilder.publicOverridesMethod("getEncodingType")
            .append(Stmt.load("json").returnValue())
            .finish();

    classStructureBuilder.publicOverridesMethod("demarshall", Parameter.of(Object.class, "a0"), Parameter.of(MarshallingContext.class, "a1"))
            .append(Stmt.nestedCall(Cast.to(JSONString.class, Stmt.loadVariable("a0"))).invoke("stringValue").returnValue())
            .finish();

    classStructureBuilder.publicOverridesMethod("marshall", Parameter.of(Object.class, "a0"), Parameter.of(MarshallingContext.class, "a1"))
            .append(Stmt.loadVariable("a0").returnValue())
            .finish();

    return classStructureBuilder.finish();
  }

}
