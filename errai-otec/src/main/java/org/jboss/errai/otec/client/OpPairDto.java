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
