package org.jboss.errai.otec.mutation;

/**
 * @author Mike Brock
 */
public interface Operation<T extends State, P extends Position, D extends Data> {
  public int getRevision();
  public OperationType getType();
  public P getPosition();
  public D getData();
  public void apply(T state);
}
