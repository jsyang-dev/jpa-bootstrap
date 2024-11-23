package persistence.event;

import persistence.action.ActionQueue;
import persistence.bootstrap.Metamodel;
import persistence.entity.manager.factory.PersistenceContext;
import persistence.event.delete.DefaultDeleteEventListener;
import persistence.event.load.DefaultLoadEventListener;
import persistence.event.persist.DefaultPersistEventListener;
import persistence.event.update.DefaultUpdateEventListener;

import java.util.HashMap;
import java.util.Map;

public class EventListenerRegistry {
    final Map<EventType<?>, EventListenerGroup<EventListener>> eventListenerGroupRegistry = new HashMap<>();

    public EventListenerRegistry(Metamodel metamodel, PersistenceContext persistenceContext, ActionQueue actionQueue) {
        registerLoadEventListener(metamodel, persistenceContext);
        registerPersistEventListener(metamodel, persistenceContext, actionQueue);
        registerDeleteEventListener(metamodel, persistenceContext, actionQueue);
        registerUpdateEventListener(metamodel, persistenceContext, actionQueue);
    }

    public void registerEventListener(EventType<? extends EventListener> eventType, EventListener... eventListeners) {
        final EventListenerGroup<EventListener> eventListenerGroup =
                eventListenerGroupRegistry.computeIfAbsent(eventType, e -> new EventListenerGroup<>(eventType));
        for (EventListener eventListener : eventListeners) {
            eventListenerGroup.appendListener(eventListener);
        }
        eventListenerGroupRegistry.put(eventType, eventListenerGroup);
    }

    public void clear() {
        eventListenerGroupRegistry.clear();
    }

    private void registerLoadEventListener(Metamodel metamodel, PersistenceContext persistenceContext) {
        final DefaultLoadEventListener eventListener = new DefaultLoadEventListener(metamodel, persistenceContext);
        registerEventListener(EventType.LOAD, eventListener);
    }

    private void registerPersistEventListener(Metamodel metamodel, PersistenceContext persistenceContext, ActionQueue actionQueue) {
        final DefaultPersistEventListener eventListener = new DefaultPersistEventListener(metamodel, persistenceContext, actionQueue);
        registerEventListener(EventType.PERSIST, eventListener);
    }

    private void registerDeleteEventListener(Metamodel metamodel, PersistenceContext persistenceContext, ActionQueue actionQueue) {
        final DefaultDeleteEventListener eventListener = new DefaultDeleteEventListener(metamodel, persistenceContext, actionQueue);
        registerEventListener(EventType.DELETE, eventListener);
    }

    private void registerUpdateEventListener(Metamodel metamodel, PersistenceContext persistenceContext, ActionQueue actionQueue) {
        final DefaultUpdateEventListener eventListener = new DefaultUpdateEventListener(metamodel, persistenceContext, actionQueue);
        registerEventListener(EventType.UPDATE, eventListener);
    }
}
