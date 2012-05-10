/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.bus.server.io.websockets;

//import static io.netty.channel.Channels.*;
//
//import io.netty.channel.ChannelPipeline;
//import io.netty.channel.ChannelPipelineFactory;
//import io.netty.handler.codec.http.HttpChunkAggregator;
//import io.netty.handler.codec.http.HttpRequestDecoder;
//import io.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.errai.bus.server.service.ErraiService;

/**
 * @author Mike Brock
 */
public class WebSocketServerPipelineFactory //implements ChannelPipelineFactory 
{
  private ErraiService svc;
  private WebSocketServerHandler webSocketServerHandler;

  public WebSocketServerPipelineFactory(ErraiService service) {
    this.svc = service;
    this.webSocketServerHandler = new WebSocketServerHandler(svc);
  }

//  public ChannelPipeline getPipeline() throws Exception {
//    // Create a default pipeline implementation.
//    ChannelPipeline pipeline = pipeline();
//    pipeline.addLast("decoder", new HttpRequestDecoder());
//    pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
//    pipeline.addLast("encoder", new HttpResponseEncoder());
//    pipeline.addLast("handler", webSocketServerHandler);
//    return pipeline;
//  }

  public WebSocketServerHandler getWebSocketServerHandler() {
    return webSocketServerHandler;
  }
}