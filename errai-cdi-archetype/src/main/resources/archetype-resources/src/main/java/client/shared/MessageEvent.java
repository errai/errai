package ${package}.client.shared;

import org.jboss.errai.bus.server.annotations.ExposeEntity;

/**
 * Generated.
 */
@ExposeEntity
public class MessageEvent {
    private int id;
    private String message;

    public MessageEvent() {
    }

    public MessageEvent(String message) {
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}