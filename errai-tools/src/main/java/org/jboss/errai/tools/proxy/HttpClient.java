/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.tools.proxy;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Security;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Yutaka Yoshida, Greg Murray, Heiko Braun
 *         <p/>
 *         Minimum set of HTTPclient supporting both http and https.
 *         It's aslo capable of POST, but it doesn't provide doGet because
 *         the caller can just read the inputstream.
 */
public class HttpClient {

  private static Logger logger;
  private String proxyHost = null;
  private int proxyPort = -1;
  private boolean isHttps = false;
  private boolean isProxy = false;
  private HttpURLConnection urlConnection = null;
  private Map headers;

  private String setCookieHeader;

  private XmlHttpProxy.CookieCallback callback;

  /**
   * @param phost   PROXY host name
   * @param pport   PROXY port string
   * @param url     URL string
   * @param headers Map
   */
  public HttpClient(
      String phost,
      int pport,
      String url,
      Map headers,
      String method,
      XmlHttpProxy.CookieCallback callback)
      throws MalformedURLException {
    this.callback = callback;

    if (phost != null && pport != -1) {
      this.isProxy = true;
    }

    this.proxyHost = phost;
    this.proxyPort = pport;

    if (url.trim().startsWith("https:")) {
      isHttps = true;
    }

    this.urlConnection = getURLConnection(url);
    try {
      this.urlConnection.setRequestMethod(method);
    }
    catch (java.net.ProtocolException pe) {
      HttpClient.getLogger().severe("Unable protocol method to " + method + " : " + pe);
    }
    this.headers = headers;
    writeHeaders(headers);

  }

  private void writeHeaders(Map headers) {
    if (this.callback != null) {
      Map<String, XmlHttpProxy.Cookie> cookies = callback.getCookies();
      Iterator it = cookies.keySet().iterator();
      while (it.hasNext()) {
        XmlHttpProxy.Cookie c = cookies.get(it.next());
        if (headers == null) headers = new HashMap();
        headers.put(
            "Cookie", c.name + "=" + c.value // + "; Path=" + c.path
        );
      }

    }
    // set headers
    if (headers != null) {
      Iterator it = headers.keySet().iterator();
      if (it != null) {
        while (it.hasNext()) {
          String key = (String) it.next();
          String value = (String) headers.get(key);
          System.out.println("Set Request Header: " + key + "->" + value);
          this.urlConnection.setRequestProperty(key, value);
        }
      }
    }
  }

  /**
   * @param phost    PROXY host name
   * @param pport    PROXY port string
   * @param url      URL string
   * @param headers  Map
   * @param userName string
   * @param password string
   */
  public HttpClient(String phost,
                    int pport,
                    String url,
                    Map headers,
                    String method,
                    String userName,
                    String password,
                    XmlHttpProxy.CookieCallback callback)
      throws MalformedURLException {

    this.callback = callback;

    try {
      if (phost != null && pport != -1) {
        this.isProxy = true;
      }

      this.proxyHost = phost;
      this.proxyPort = pport;
      if (url.trim().startsWith("https:")) {
        isHttps = true;
      }
      this.urlConnection = getURLConnection(url);
      try {
        this.urlConnection.setRequestMethod(method);
      }
      catch (java.net.ProtocolException pe) {
        HttpClient.getLogger().severe("Unable protocol method to " + method + " : " + pe);
      }
      // set basic authentication information
      String auth = userName + ":" + password;
      String encoded = Base64.getEncoder().encodeToString(auth.getBytes());
      // set basic authorization
      this.urlConnection.setRequestProperty("Authorization", "Basic " + encoded);
      this.headers = headers;
      writeHeaders(headers);
    }
    catch (Exception ex) {
      HttpClient.getLogger().severe("Unable to set basic authorization for " + userName + " : " + ex);
    }
  }

  /**
   * private method to get the URLConnection
   *
   * @param str URL string
   */
  private HttpURLConnection getURLConnection(String str)
      throws MalformedURLException {
    try {

/*       if (isHttps) {
         when communicating with the server which has unsigned or invalid
        * certificate (https), SSLException or IOException is thrown.
        * the following line is a hack to avoid that
        */
        
        /*Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
        if (isProxy) {
          System.setProperty("https.proxyHost", proxyHost);
          System.setProperty("https.proxyPort", proxyPort + "");
        }
      }
      else { */
        if (isProxy) {
          System.setProperty("http.proxyHost", proxyHost);
          System.setProperty("http.proxyPort", proxyPort + "");
        }
//      }

      URL url = new URL(str);
      HttpURLConnection uc = (HttpURLConnection) url.openConnection();

      // if this header has not been set by a request set the user agent.
      if (headers == null ||
          (headers != null && headers.get("user-agent") == null)) {
        // set user agent to mimic a common browser
        String ua = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.1.4322)";
        uc.setRequestProperty("user-agent", ua);
      }

      uc.setInstanceFollowRedirects(false);

      return uc;
    }
    catch (MalformedURLException me) {
      throw new MalformedURLException(str + " is not a valid URL");
    }
    catch (Exception e) {
      throw new RuntimeException("Unknown error creating UrlConnection: " + e);
    }
  }

  public String getSetCookieHeader() {
    return setCookieHeader;
  }

  /**
   * returns the inputstream from URLConnection
   *
   * @return InputStream
   */
  public InputStream getInputStream() {
    try {
      // logger doesnt work, because it writes to stderr,
      // which causes GwtTest to interpret it as failure
      System.out.println(
          this.urlConnection.getRequestMethod() + " " +
              this.urlConnection.getURL() + ": " +
              this.urlConnection.getResponseCode()
      );

      try {
        // HACK: manually follow redirects, for the login to work
        // HTTPUrlConnection auto redirect doesn't respect the provided headers
        if (this.urlConnection.getResponseCode() == 302) {
          HttpClient redirectClient =
              new HttpClient(proxyHost, proxyPort, urlConnection.getHeaderField("Location"),
                  headers, urlConnection.getRequestMethod(), callback);
          redirectClient.getInputStream().close();
        }
      }
      catch (Throwable e) {
        System.out.println("Following redirect failed");
      }

      setCookieHeader = this.urlConnection.getHeaderField("Set-Cookie");

      return (this.urlConnection.getInputStream());
    }
    catch (Exception e) {
      System.out.println("Failed to open " + this.urlConnection.getURL());
      e.printStackTrace();
      return null;
    }
  }

  /**
   * return the OutputStream from URLConnection
   *
   * @return OutputStream
   */
  public OutputStream getOutputStream() {

    try {
      return (this.urlConnection.getOutputStream());
    }
    catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * posts data to the inputstream and returns the InputStream.
   *
   * @param postData    data to be posted. must be url-encoded already.
   * @param contentType allows you to set the contentType of the request.
   * @return InputStream input stream from URLConnection
   */
  public InputStream doPost(String postData, String contentType) {
    this.urlConnection.setDoOutput(true);
    if (contentType != null) this.urlConnection.setRequestProperty("Content-type", contentType);

    OutputStream os = this.getOutputStream();
    PrintStream ps = new PrintStream(os);
    ps.print(postData);
    ps.close();
    return (this.getInputStream());
  }

  public String getContentEncoding() {
    if (this.urlConnection == null) return null;
    return (this.urlConnection.getContentEncoding());
  }

  public int getContentLength() {
    if (this.urlConnection == null) return -1;
    return (this.urlConnection.getContentLength());
  }

  public String getContentType() {
    if (this.urlConnection == null) return null;
    return (this.urlConnection.getContentType());
  }

  public long getDate() {
    if (this.urlConnection == null) return -1;
    return (this.urlConnection.getDate());
  }

  public String getHeader(String name) {
    if (this.urlConnection == null) return null;
    return (this.urlConnection.getHeaderField(name));
  }

  public long getIfModifiedSince() {
    if (this.urlConnection == null) return -1;
    return (this.urlConnection.getIfModifiedSince());
  }

  public static Logger getLogger() {
    if (logger == null) {
      logger = Logger.getLogger("jmaki.xhp.Log");
    }
    return logger;
  }
}
