package org.jboss.errai.ui.client.local.spi;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author edewit@redhat.com
 */
public class DictionaryTest {

  public static final String WELCOME_KEY = "welcome";
  public static final String NL = "nl";
  public static final String CH = "ch";

  @Test
  public void shouldBeAbleToInsertAndRetrieveValuesFromDictionary() {
    // given
    Dictionary dictionary = new Dictionary();

    // when
    dictionary.put(NL, WELCOME_KEY, "Goedendag");
    dictionary.put(CH, WELCOME_KEY, "Grüezi wohl");


    // then
    assertEquals("Goedendag", dictionary.get(NL).get(WELCOME_KEY));
    assertEquals("Grüezi wohl", dictionary.get(CH).get(WELCOME_KEY));

    assertNotNull(dictionary.get("ru"));
    assertTrue(dictionary.get("ru").isEmpty());

    final Set<String> treeSet = new HashSet<String>();
    treeSet.add(NL);
    treeSet.add(CH);
    assertEquals(treeSet, dictionary.getSupportedLocals());
  }

  @Test
  public void shouldReturnReadonlyCollections() {
    // given
    Dictionary dictionary = new Dictionary();
    dictionary.put(NL, WELCOME_KEY, "Goedendag");

    // when
    dictionary.get(NL).put("try to", "break");
    dictionary.getSupportedLocals().add(CH);

    // then
    assertEquals(1, dictionary.get(NL).size());
    assertEquals(1, dictionary.getSupportedLocals().size());
  }
}
