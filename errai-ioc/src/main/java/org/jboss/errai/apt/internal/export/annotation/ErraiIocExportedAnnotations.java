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

package org.jboss.errai.apt.internal.export.annotation;

import jsinterop.annotations.JsType;
import org.jboss.errai.common.client.api.annotations.IOCProducer;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.api.IOCBootstrapTask;
import org.jboss.errai.ioc.client.api.IOCExtension;
import org.jboss.errai.ioc.client.api.IOCProvider;
import org.jboss.errai.ioc.client.api.ScopeContext;
import org.jboss.errai.ioc.client.api.SharedSingleton;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Qualifier;
import javax.inject.Singleton;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public final class ErraiIocExportedAnnotations {

  private ErraiIocExportedAnnotations() {
  }

  private IOCBootstrapTask iocBootstrapTask;
  private IOCExtension iocExtension;
  private CodeDecorator codeDecorator;
  private ScopeContext scopeContext;

  private Inject javaxInject;
  private com.google.inject.Inject googleInject;
  private IOCProvider iocProvider;
  private Dependent dependent;
  private ApplicationScoped applicationScoped;
  private Alternative alternative;
  private Singleton singleton;
  private EntryPoint entryPoint;
  private SharedSingleton sharedSingleton;
  private IOCProducer iocProducer;
  private Qualifier qualifier;
  private JsType jsType;
  private Produces produces;


}
