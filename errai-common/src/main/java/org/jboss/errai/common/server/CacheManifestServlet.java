package org.jboss.errai.common.server;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet provides a cache manifest file specific to the requesting user
 * agent. It responds to .appcache requests and dispatches to user agent
 * specific appcache.manifest files (i.e. safari.appcache.manifest). These files
 * are generated at compile time by a dedicated linker. See the Errai reference
 * guide for details on how to activate this linker.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@WebServlet(urlPatterns = "*.appcache")
public class CacheManifestServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Pattern pattern = Pattern.compile("/([a-zA-Z0-9_]+)/errai.appcache");
    Matcher matcher = pattern.matcher(req.getServletPath());
    if (!matcher.find()) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    String module = matcher.group(1);
    String userAgent = getUserAgent(req);
    String userAgentManifestPath = null;

    String referrer = req.getHeader("referer");
    if (referrer != null && referrer.contains("gwt.codesvr")) {
      // Serve an empty manifest in development mode. This is not reliable as
      // some browser won't send the referer header when requesting the
      // manifest. In that case we simply return a 404 (the manifest is not
      // needed in dev mode anyway but we try to avoid the error, if possible).
      userAgentManifestPath = "/" + module + "/dev.appcache.manifest";
    }
    else {
      userAgentManifestPath = "/" + module + "/" + userAgent + "." + "appcache.manifest";
    }

    resp.setHeader("Cache-Control", "no-cache");
    resp.setHeader("Pragma", "no-cache");
    resp.setContentType("text/cache-manifest");
    req.getRequestDispatcher(userAgentManifestPath).forward(req, resp);
  }

  private String getUserAgent(HttpServletRequest req) {
    String userAgentHeader = req.getHeader("user-agent").toLowerCase();

    // Do not change the order of these checks. To verify this, compile a
    // nocache.js file in pretty mode and compare the corresponding client-side
    // logic provided by GWT.
    if (userAgentHeader.contains("opera")) {
      return "opera";
    }
    else if (userAgentHeader.contains("webkit")) {
      return "safari";
    }
    else if (userAgentHeader.contains("msie 10")) {
      // We don't have a separate permutation for IE 10
      return "ie9";
    }
    else if (userAgentHeader.contains("msie 9")) {
      return "ie9";
    }
    else if (userAgentHeader.contains("msie 8")) {
      return "ie8";
    }
    else if (userAgentHeader.contains("msie")) {
      return "ie6";
    }
    else if (userAgentHeader.contains("gecko")) {
      return "gecko1_8";
    }

    return "";
  }

}