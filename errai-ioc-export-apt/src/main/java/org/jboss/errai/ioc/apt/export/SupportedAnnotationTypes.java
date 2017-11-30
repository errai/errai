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

package org.jboss.errai.ioc.apt.export;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
interface SupportedAnnotationTypes {

  String IOC_BOOTSTRAP_TASK = "org.jboss.errai.ioc.client.api.IOCBootstrapTask";
  String IOC_EXTENSION = "org.jboss.errai.ioc.client.api.IOCExtension";
  String CODE_DECORATOR = "org.jboss.errai.ioc.client.api.CodeDecorator";
  String SCOPE_CONTEXT = "org.jboss.errai.ioc.client.api.ScopeContext";
  String JAVAX_INJECT = "javax.inject.Inject";
  String GOOGLE_INJECT = "com.google.inject.Inject";
  String IOC_PROVIDER = "org.jboss.errai.ioc.client.api.IOCProvider";
  String DEPENDENT = "javax.enterprise.context.Dependent";
  String APPLICATION_SCOPED = "javax.enterprise.context.ApplicationScoped";
  String ALTERNATIVE = "javax.enterprise.inject.Alternative";
  String SINGLETON = "javax.inject.Singleton";
  String IOC_PRODUCER = "org.jboss.errai.common.client.api.annotations.IOCProducer";
  String ENTRY_POINT = "org.jboss.errai.ioc.client.api.EntryPoint";
  String SHARED_SINGLETON = "org.jboss.errai.ioc.client.api.SharedSingleton";
  String QUALIFIER = "javax.inject.Qualifier";
  String JS_TYPE = "jsinterop.annotations.JsType";
  String PRODUCES = "javax.enterprise.inject.Produces";
}
