package org.jboss.errai.codegen.gwt.test;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.dev.javac.testing.GeneratorContextBuilder;
import com.google.gwt.dev.javac.testing.Source;
import org.jboss.errai.codegen.util.ClassChangeUtil;
import org.jboss.errai.common.metadata.RebindUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock
 */
public abstract class AbstractGWTMetaClassTest {
  protected final File pathToTestFiles = new File("src/test/resource/");
  protected final List<Source> sourceFilesToAdd = new ArrayList<Source>();

  protected void addTestClass(final String fqcn) {

    sourceFilesToAdd.add(new Source() {
      @Override
      public String getPath() {
        return getRelativePathToClassFromName(fqcn);
      }

      @Override
      public String getSource() {
        return RebindUtils.readFileToString(new File(pathToTestFiles, getPath()));
      }
    });
  }

  protected TypeOracle generateMockacle() {
    final GeneratorContextBuilder contextBuilder = GeneratorContextBuilder.newCoreBasedBuilder();

    for (final Source source : sourceFilesToAdd) {
      contextBuilder.add(source);
    }

    final GeneratorContext context = contextBuilder.buildGeneratorContext();

    return context.getTypeOracle();
  }

  protected Class loadTestClass(final String fqcn) throws Exception {
    return ClassChangeUtil.compileAndLoad(
                 getFullyQualifiedPathToClassFromName(fqcn),
                 getPackageFromFQCN(fqcn),
                 getNameFromFQCN(fqcn));
  }

  private static String getPackageFromFQCN(final String fqcn) {
    final int index = fqcn.lastIndexOf('.');
    if (index == -1) {
      return "";
    }
    else {
      return fqcn.substring(0, index);
    }
  }

  private static String getNameFromFQCN(final String fqcn) {
    final int index = fqcn.lastIndexOf('.');
    if (index == -1) {
      return fqcn;
    }
    else {
      return fqcn.substring(index + 1);
    }
  }

  private static String getRelativePathToClassFromName(final String fqcn) {
    return fqcn.replaceAll("\\.", "/") + ".java";
  }

  private String getFullyQualifiedPathToClassFromName(final String fqcn) {
    return new File(pathToTestFiles, getPackageFromFQCN(fqcn)).getAbsolutePath();
  }
}
