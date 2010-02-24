package org.jboss.errai.bus.server.servlet;

import com.google.inject.Singleton;
import com.sun.grizzly.comet.CometEvent;
import com.sun.grizzly.comet.CometHandler;

import javax.servlet.http.HttpServlet;
import java.io.IOException;

/**
 * The <tt>GrizzlyCometServlet</tt> provides the HTTP-protocol gateway between the server bus and the client buses,
 * using Glassfish v2.
 */
@Singleton
public class GrizzlyCometServlet extends AbstractErraiServlet implements CometHandler<HttpServlet> {

    public GrizzlyCometServlet() {
        System.out.println("Grizzly constructor");
        Thread.dumpStack();
    }

    public void attach(HttpServlet e) {
        System.out.println("attach");
    }

    public void onEvent(CometEvent cometEvent) throws IOException {
        System.out.println("onEvent");
    }

    public void onInitialize(CometEvent cometEvent) throws IOException {
        System.out.println("onInitialize");
    }

    public void onTerminate(CometEvent cometEvent) throws IOException {
        System.out.println("onTerminate");
    }

    public void onInterrupt(CometEvent cometEvent) throws IOException {
        System.out.println("onInterrupt");
    }

    private static final String CONFIG_PROBLEM_TEXT =
            "\n\n*************************************************************************************************\n"
                    + "** PROBLEM!\n"
                    + "** It appears something has been incorrectly configured. In order to use ErraiBus\n"
                    + "** on Glassfish with Grizzly, you must ensure that you are using the NIO or APR connector. Also \n"
                    + "** make sure that you have added these lines to your WEB-INF/web.xml file:\n"
                    + "**                                              ---\n"
                    + "**    <servlet>\n" +
                    "**        <servlet-name>GrizzlyErraiServlet</servlet-name>\n" +
                    "**        <servlet-class>org.jboss.errai.bus.server.servlet.GrizzlyCometServlet</servlet-class>\n" +
                    "**        <load-on-startup>1</load-on-startup>\n" +
                    "**    </servlet>\n" +
                    "**\n" +
                    "**    <servlet-mapping>\n" +
                    "**        <servlet-name>GrizzlyErraiServlet</servlet-name>\n" +
                    "**        <url-pattern>*.erraiBus</url-pattern>\n" +
                    "**    </servlet-mapping>\n"
                    + "**                                              ---\n"
                    + "** If you have the following lines in your WEB-INF/web.xml, you must comment or remove them:\n"
                    + "**                                              ---\n"
                    + "**    <listener>\n" +
                    "**        <listener-class>org.jboss.errai.bus.server.ErraiServletConfig</listener-class>\n" +
                    "**    </listener>\n"
                    + "*************************************************************************************************\n\n";
}
