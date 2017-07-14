/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.codegen.builder;

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.literal.ByteValue;
import org.jboss.errai.codegen.literal.CharValue;
import org.jboss.errai.codegen.literal.IntValue;
import org.jboss.errai.codegen.literal.LiteralValue;
import org.jboss.errai.codegen.literal.ShortValue;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface CaseBlockBuilder extends Statement, Builder {
  BlockBuilder<CaseBlockBuilder> case_(int value);
  BlockBuilder<CaseBlockBuilder> case_(byte value);
  BlockBuilder<CaseBlockBuilder> case_(short value);
  BlockBuilder<CaseBlockBuilder> case_(char value);
  BlockBuilder<CaseBlockBuilder> case_(String value);
  BlockBuilder<CaseBlockBuilder> case_(Enum<?> value);
  
  BlockBuilder<CaseBlockBuilder> case_(IntValue value);
  BlockBuilder<CaseBlockBuilder> case_(ByteValue value);
  BlockBuilder<CaseBlockBuilder> case_(ShortValue value);
  BlockBuilder<CaseBlockBuilder> case_(CharValue value);
  BlockBuilder<CaseBlockBuilder> case_(LiteralValue<Enum<?>> value);

  BlockBuilder<StatementEnd> default_();
}
