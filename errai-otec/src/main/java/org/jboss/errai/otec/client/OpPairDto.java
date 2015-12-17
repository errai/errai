/**
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.otec.client;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.otec.client.operation.OpPair;

/**
 * @author Mike Brock
 */
@Portable
public class OpPairDto {
  private final OpDto remoteOp;
  private final OpDto localOp;

  public OpPairDto(@MapsTo("remoteOp") OpDto remoteOp, @MapsTo("localOp") OpDto localOp) {
    this.remoteOp = remoteOp;
    this.localOp = localOp;
  }

  public OpDto getRemoteOp() {
    return remoteOp;
  }

  public OpDto getLocalOp() {
    return localOp;
  }

  public OpPair toOpPair(OTEngine engine) {
    return OpPair.of(remoteOp.otOperation(engine), localOp.otOperation(engine));
  }
}
