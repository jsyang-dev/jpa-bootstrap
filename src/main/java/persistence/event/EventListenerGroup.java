package persistence.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class EventListenerGroup<T> {
    private final EventType<T> eventType;
    private final List<T> listeners = new ArrayList<>();

    public EventListenerGroup(EventType<T> eventType) {
        this.eventType = eventType;
    }

    public void appendListener(T listener) {
        listeners.add(listener);
    }

    public <U> void doEvent(U event, BiConsumer<T, U> action) {
        listeners.forEach(listener -> action.accept(listener, event));
    }

    public EventType<T> getEventType() {
        return eventType;
    }
}
