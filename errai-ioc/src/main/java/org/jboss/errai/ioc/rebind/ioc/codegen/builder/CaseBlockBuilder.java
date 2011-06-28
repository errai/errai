/*
 * Copyright 2011 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.BlockBuilderImpl;
import org.jboss.errai.ioc.rebind.ioc.codegen.literal.ByteValue;
import org.jboss.errai.ioc.rebind.ioc.codegen.literal.CharValue;
import org.jboss.errai.ioc.rebind.ioc.codegen.literal.IntValue;
import org.jboss.errai.ioc.rebind.ioc.codegen.literal.LiteralValue;
import org.jboss.errai.ioc.rebind.ioc.codegen.literal.ShortValue;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface CaseBlockBuilder extends Statement, Builder {
  BlockBuilderImpl<CaseBlockBuilder> case_(int value);
  BlockBuilderImpl<CaseBlockBuilder> case_(byte value);
  BlockBuilderImpl<CaseBlockBuilder> case_(short value);
  BlockBuilderImpl<CaseBlockBuilder> case_(char value);
  BlockBuilderImpl<CaseBlockBuilder> case_(Enum<?> value);
  
  BlockBuilderImpl<CaseBlockBuilder> case_(IntValue value);
  BlockBuilderImpl<CaseBlockBuilder> case_(ByteValue value);
  BlockBuilderImpl<CaseBlockBuilder> case_(ShortValue value);
  BlockBuilderImpl<CaseBlockBuilder> case_(CharValue value);
  BlockBuilderImpl<CaseBlockBuilder> case_(LiteralValue<Enum<?>> value);

  BlockBuilderImpl<StatementEnd> default_();
}