package persistence.event.update;

public interface UpdateEventListener {
    <T> void onUpdate(UpdateEvent<T> updateEvent);
}
