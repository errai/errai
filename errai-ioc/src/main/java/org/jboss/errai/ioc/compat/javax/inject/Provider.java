package javax.inject;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface Provider<T> {
  public T get();
}
