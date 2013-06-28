package org.jboss.errai.demo.busstress.server;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.demo.busstress.client.shared.RecursiveObject;
import org.jboss.errai.demo.busstress.client.shared.RpcService;

@Service
public class StressTestRpcService implements RpcService {

  @Override
  public RecursiveObject echo(RecursiveObject in) {
    return in;
  }

}
