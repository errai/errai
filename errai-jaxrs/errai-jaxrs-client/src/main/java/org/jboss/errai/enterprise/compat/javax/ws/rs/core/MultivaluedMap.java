/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.php
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

/*
 * MultivaluedMap.java
 *
 * Created on February 13, 2007, 2:30 PM
 *
 */

package javax.ws.rs.core;

import java.util.List;
import java.util.Map;

/**
 * A map of key-values pairs. Each key can have zero or more values.
 * 
 */
public interface MultivaluedMap<K, V> extends Map<K, List<V>> {

    /**
     * Set the key's value to be a one item list consisting of the supplied value.
     * Any existing values will be replaced.
     *
     * @param key   the key
     * @param value the single value of the key
     */
    void putSingle(K key, V value);

    /**
     * Add a value to the current list of values for the supplied key.
     *
     * @param key   the key
     * @param value the value to be added.
     */
    void add(K key, V value);

    /**
     * A shortcut to get the first value of the supplied key.
     *
     * @param key the key
     * @return the first value for the specified key or null if the key is
     *         not in the map.
     */
    V getFirst(K key);

    /**
     * Add multiple values to the current list of values for the supplied key. If
     * the supplied array of new values is empty, method returns immediately.
     * Method throws a {@code NullPointerException} if the supplied array of values
     * is {@code null}.
     *
     * @param key       the key.
     * @param newValues the values to be added.
     * @throws NullPointerException if the supplied array of new values is {@code null}.
     * @since 2.0
     */
    void addAll(K key, V... newValues);

    /**
     * Add all the values from the supplied value list to the current list of
     * values for the supplied key. If the supplied value list is empty, method
     * returns immediately. Method throws a {@code NullPointerException} if the
     * supplied array of values is {@code null}.
     *
     * @param key       the key.
     * @param valueList the list of values to be added.
     * @throws NullPointerException if the supplied value list is {@code null}.
     * @since 2.0
     */
    void addAll(K key, List<V> valueList);

    /**
     * Add a value to the first position in the current list of values for the
     * supplied key.
     *
     * @param key   the key
     * @param value the value to be added.
     * @since 2.0
     */
    void addFirst(K key, V value);

    /**
     * Compare the specified map with this map for equality modulo the order
     * of values for each key. Specifically, the values associated with
     * each key are compared as if they were ordered lists.
     *
     * @param otherMap map to be compared to this one.
     * @return true if the maps are equal modulo value ordering.
     * @since 2.0
     */
    boolean equalsIgnoreValueOrder(MultivaluedMap<K, V> otherMap);
    
}

