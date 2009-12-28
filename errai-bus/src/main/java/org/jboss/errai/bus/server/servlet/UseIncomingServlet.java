package org.jboss.errai.bus.server.servlet;

import com.sun.net.httpserver.HttpServer;

import javax.servlet.http.HttpServlet;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UseIncomingServlet {
    Class<? extends HttpServlet> value();
}
