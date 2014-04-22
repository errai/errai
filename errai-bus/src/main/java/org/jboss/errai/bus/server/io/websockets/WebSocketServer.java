/*
 * Copyright 2012 JBoss, by Red Hat, Inc
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

import static org.slf4j.LoggerFactory.getLogger;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.net.InetSocketAddress;

import org.jboss.errai.bus.server.service.ErraiConfigAttribs;
import org.jboss.errai.bus.server.service.ErraiService;
import org.slf4j.Logger;

/**
 * @author Mike Brock
 */
public class WebSocketServer {
  private ErraiService svc;
  private Logger log = getLogger(getClass());

  public WebSocketServer(ErraiService svc) {
    this.svc = svc;
  }

  public void start() {
    final int port = ErraiConfigAttribs.WEB_SOCKET_PORT.getInt(svc.getConfiguration());
    final ServerBootstrap bootstrap = new ServerBootstrap();
    final WebSocketServerHandler webSocketHandler = new WebSocketServerHandler(svc);

    try {
      final NioEventLoopGroup bossGroup = new NioEventLoopGroup();
      final NioEventLoopGroup workerGroup = new NioEventLoopGroup();
      final ChannelFuture channelFuture = bootstrap
              .group(bossGroup, workerGroup)
              .channel(NioServerSocketChannel.class)
              .childHandler(new ChannelInitializer() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
                  ch.pipeline().addLast("decoder", new HttpRequestDecoder());
                  ch.pipeline().addLast("encoder", new HttpResponseEncoder());
                  ch.pipeline().addLast("handler", webSocketHandler);
                }

              }).bind(new InetSocketAddress(port)).sync();

      svc.addShutdownHook(new Runnable() {
        @Override
        public void run() {
          webSocketHandler.stop();
          channelFuture.channel().close();
          log.info("web socket server stopped.");
        }
      });
      
    } 
    catch (Throwable t) {
      throw new RuntimeException(t);
    }

    log.info("started web socket server on port: " + port);
  }
}
