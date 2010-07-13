package org.jboss.errai.bus.server.gae;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SchedulerWebHook extends HttpServlet {

    private Logger log = LoggerFactory.getLogger(GAESchedulerService.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        log.debug("scheduler web hook invocation");
        GAESchedulerService.INSTANCE.runAllDue();
    }

}
