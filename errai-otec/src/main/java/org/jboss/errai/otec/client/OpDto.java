/*
 * Copyright 2013 JBoss, by Red Hat, Inc
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

package org.jboss.errai.otec.client;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.otec.client.mutation.Mutation;
import org.jboss.errai.otec.client.operation.OTOperation;
import org.jboss.errai.otec.client.operation.OTOperationImpl;
import org.jboss.errai.otec.client.operation.OpPair;

import java.util.List;

/**
 * @author Mike Brock
 */
@Portable
public class OpDto {
  private int revisionId;
  private int entityId;
  private String hash;
  private List<Mutation> mutations;
  private OpPairDto opPairDto;

  public static OpDto fromOperation(final OTOperation operation) {
    final OpDto dto = new OpDto();
    dto.entityId = operation.getEntityId();
    dto.hash = operation.getRevisionHash();
    dto.revisionId = operation.getRevision();
    dto.mutations = operation.getMutations();

    if (operation.getTransformedFrom() != null) {
      dto.opPairDto = new OpPairDto(fromOperation(operation.getTransformedFrom().getRemoteOp()),
          fromOperation(operation.getTransformedFrom().getLocalOp()));
    }

    return dto;
  }

  public int getRevisionId() {
    return revisionId;
  }

  public void setRevisionId(final int revisionId) {
    this.revisionId = revisionId;
  }

  public int getEntityId() {
    return entityId;
  }

  public void setEntityId(final int entityId) {
    this.entityId = entityId;
  }

  public String getHash() {
    return hash;
  }

  public void setHash(final String hash) {
    this.hash = hash;
  }

  public List<Mutation> getMutations() {
    return mutations;
  }

  public OpPairDto getOpPairDto() {
    return opPairDto;
  }

  public void setMutations(final List<Mutation> mutations) {
    this.mutations = mutations;
  }

  public OTOperation otOperation(final OTEngine engine) {
    OpPair opPair = null;
    if (this.opPairDto != null) {
      opPair = this.opPairDto.toOpPair(engine);
    }

    return OTOperationImpl.createOperation(engine, engine.getId(), mutations, entityId, revisionId, hash, opPair);
  }
}
