package org.jboss.errai.codegen.gwt.test;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.dev.javac.testing.GeneratorContextBuilder;
import com.google.gwt.dev.javac.testing.Source;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.impl.gwt.GWTClass;
import org.jboss.errai.common.metadata.RebindUtils;
import org.junit.Test;

import java.io.File;

/**
 * @author Mike Brock
 */
public class GWTMetaClassTests {
  private final TypeOracle mockacle;

  public GWTMetaClassTests() {
    final GeneratorContextBuilder contextBuilder = GeneratorContextBuilder.newCoreBasedBuilder();

    final File pathToTestFiles = new File("src/test/resource/");

    contextBuilder.add(new Source() {
      @Override
      public String getPath() {
        return "MyTestClass.java";
      }

      @Override
      public String getSource() {
        return RebindUtils.readFileToString(new File(pathToTestFiles, getPath()));
      }
    });

    final GeneratorContext context = contextBuilder.buildGeneratorContext();

    mockacle = context.getTypeOracle();
  }

  @Test
  public void testSimple() throws NotFoundException {
    final JClassType myTestClass = mockacle.getType("MyTestClass");
    final MetaClass metaClass = GWTClass.newInstance(mockacle, myTestClass);

    System.out.println(metaClass);
  }

}
