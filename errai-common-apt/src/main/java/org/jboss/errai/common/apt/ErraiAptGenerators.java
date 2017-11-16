/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.common.apt;

import org.jboss.errai.common.apt.configuration.AptErraiConfiguration;
import org.jboss.errai.common.apt.generator.ErraiAptGeneratedSourceFile;
import org.jboss.errai.config.ErraiConfiguration;
import org.jboss.errai.config.MetaClassFinder;

import java.util.Collection;
import java.util.Collections;

import static org.jboss.errai.common.apt.generator.ErraiAptGeneratedSourceFile.Type.CLIENT;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class ErraiAptGenerators {

  public static abstract class Any {

    private final ErraiAptExportedTypes exportedTypes;
    private final ErraiConfiguration erraiConfiguration;

    public Any(final ErraiAptExportedTypes exportedTypes) {
      this.exportedTypes = exportedTypes;
      this.erraiConfiguration = new AptErraiConfiguration(exportedTypes.erraiAppConfiguration(), metaClassFinder());
    }

    public abstract Collection<ErraiAptGeneratedSourceFile> files();

    public MetaClassFinder metaClassFinder() {
      return exportedTypes::findAnnotatedMetaClasses;
    }

    public ErraiConfiguration erraiConfiguration() {
      return erraiConfiguration;
    }

    public int priority() {
      return 0;
    }
  }

  public static abstract class SingleFile extends Any {

    public SingleFile(final ErraiAptExportedTypes exportedTypes) {
      super(exportedTypes);
    }

    public abstract String generate();

    public abstract String getPackageName();

    public abstract String getClassSimpleName();

    public abstract ErraiAptGeneratedSourceFile.Type getType();

    protected String getResolvedClassSimpleName() {
      return erraiConfiguration().app().namespace() + getClassSimpleName();
    }

    protected String getResolvedFullyQualifiedClassName() {
      return getPackageName() + "." + getResolvedClassSimpleName();
    }

    @Override
    public final Collection<ErraiAptGeneratedSourceFile> files() {
      return Collections.singleton(
              new ErraiAptGeneratedSourceFile(getPackageName(), getResolvedClassSimpleName(), generate(), getType()));
    }

  }

  public static abstract class MultipleFiles extends Any {
    public MultipleFiles(final ErraiAptExportedTypes exportedTypes) {
      super(exportedTypes);
    }
  }

}
