package org.jboss.errai.jpa.test.client;

import org.jboss.errai.jpa.client.local.JsonUtil;

import com.google.gwt.json.client.JSONNull;
import com.google.gwt.junit.client.GWTTestCase;

public class JsonUtilTest extends GWTTestCase {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.jpa.test.JpaTest";
  }

  public void testBasicValueNullFromJson() throws Exception {
    assertNull(JsonUtil.basicValueFromJson(null, boolean.class));
  }

  public void testNullToBasicJsonValue() throws Exception {
    assertSame(JSONNull.getInstance(), JsonUtil.basicValueToJson(null));
  }
}
