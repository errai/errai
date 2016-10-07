/*
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.ioc.support.tests.factory.client.res.repro1;

import java.util.List;

import javax.enterprise.context.Dependent;

import org.jboss.errai.bus.server.annotations.ShadowService;

/**
 * This exists to test an issue with the ShadowService generator that caused rebind errors.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Dependent
@ShadowService
public class UnderTest implements DRLTextEditorService {

  @Override
  public List<ValidationMessage> validate(final Path path, final String content) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not yet implemented.");
  }

  @Override
  public Path create(final Path context, final String fileName, final String content, final String comment) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not yet implemented.");
  }

  @Override
  public String load(final Path path) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not yet implemented.");
  }

  @Override
  public Path save(final Path path, final String content, final Metadata metadata, final String comment) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not yet implemented.");
  }

  @Override
  public void delete(final Path path, final String comment) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not yet implemented.");
  }

  @Override
  public Path copy(final Path path, final String newName, final String comment) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not yet implemented.");
  }

  @Override
  public Path copy(final Path path, final String newName, final Path targetDirectory, final String comment) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not yet implemented.");
  }

  @Override
  public Path rename(final Path path, final String newName, final String comment) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not yet implemented.");
  }

  @Override
  public DrlModelContent loadContent(final Path path) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not yet implemented.");
  }

  @Override
  public List<String> loadClassFields(final Path path, final String fullyQualifiedClassName) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not yet implemented.");
  }

  @Override
  public String assertPackageName(final String drl, final Path resource) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not yet implemented.");
  }

}
