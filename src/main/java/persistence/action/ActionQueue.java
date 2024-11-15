package persistence.action;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ActionQueue {
    private final Queue<PersistAction<?>> persists = new ConcurrentLinkedQueue<>();
    private final Queue<DeleteAction<?>> deletes = new ConcurrentLinkedQueue<>();
    private final Queue<UpdateAction<?>> updates = new ConcurrentLinkedQueue<>();

    public void addAction(PersistAction<?> persistAction) {
        persists.offer(persistAction);
    }

    public void addAction(DeleteAction<?> deleteAction) {
        deletes.offer(deleteAction);
    }

    public void addAction(UpdateAction<?> updateAction) {
        updates.offer(updateAction);
    }

    public void executeAll() {
        while (!persists.isEmpty()) {
            final PersistAction<?> persistAction = persists.poll();
            persistAction.execute();
        }

        while (!deletes.isEmpty()) {
            final DeleteAction<?> deleteAction = deletes.poll();
            deleteAction.execute();
        }

        while (!updates.isEmpty()) {
            final UpdateAction<?> updateAction = updates.poll();
            updateAction.execute();
        }
    }

    public void clear() {
        persists.clear();
        deletes.clear();
        updates.clear();
    }
}
