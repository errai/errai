/* jboss.org */
package org.jboss.errai.tools.proxy;

import org.jboss.errai.bus.server.io.JSONDecoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Apr 21, 2010
 */
public class ProxyConfig
{
  private Map rootConfig;

  public final static String SERVICES = "services";
  public final static String ID = "id";
  public final static String URL = "url";
  public final static String CONTENT_TYPE = "contentType";
  public final static String PASSTHROUGH = "passthrough";

  protected ProxyConfig(Map rootConfig)
  {
    this.rootConfig = rootConfig;
  }

  public static ProxyConfig parse(String json)
  {
    JSONDecoder decoder = new JSONDecoder(json);
    ProxyConfig config = new ProxyConfig((Map)decoder.parse());
    return config;
  }

  public static ProxyConfig parse(InputStream in)
  {
    return parse(inputStreamToString(in));
  }

  public List<Map<String,String>> getServices()
  {
    Map root = (Map)rootConfig.get("xhp");
    return (List)root.get(SERVICES);
  }

  private static String inputStreamToString(InputStream in)
  {
    try
    {
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
      StringBuilder stringBuilder = new StringBuilder();
      String line = null;

      while ((line = bufferedReader.readLine()) != null) {
        stringBuilder.append(line + "\n");
      }

      bufferedReader.close();
      return stringBuilder.toString();
    }
    catch (IOException e)
    {
      throw new RuntimeException("Failed to parse input stream", e);
    }
  }
}
