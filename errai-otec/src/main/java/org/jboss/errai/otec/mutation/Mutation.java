package org.jboss.errai.otec.mutation;

/**
 * @author Mike Brock
 */
public interface Mutation<T extends State, P extends Position, D extends Data> {
  public MutationType getType();
  public P getPosition();
  public D getData();
  public void apply(T state);
}
