package org.jboss.errai.demo.busstress.client.shared;

import org.jboss.errai.bus.server.annotations.Remote;

@Remote
public interface RpcService {

  RecursiveObject echo(RecursiveObject in);
}
