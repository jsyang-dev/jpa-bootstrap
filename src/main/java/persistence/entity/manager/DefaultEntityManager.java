package persistence.entity.manager;

import persistence.bootstrap.Metamodel;
import persistence.entity.manager.factory.PersistenceContext;
import persistence.entity.persister.CollectionPersister;
import persistence.entity.persister.EntityPersister;
import persistence.event.LoadEvent;
import persistence.event.LoadEventListener;
import persistence.event.PersistEvent;
import persistence.event.PersistEventListener;
import persistence.meta.EntityColumn;
import persistence.meta.EntityTable;

import java.util.List;
import java.util.Objects;
import java.util.Queue;

public class DefaultEntityManager implements EntityManager {
    public static final String NOT_PERSISTABLE_STATUS_FAILED_MESSAGE = "엔티티가 영속화 가능한 상태가 아닙니다.";
    public static final String NOT_REMOVABLE_STATUS_FAILED_MESSAGE = "엔티티가 제거 가능한 상태가 아닙니다.";

    private final Metamodel metamodel;
    private final PersistenceContext persistenceContext;

    public DefaultEntityManager(Metamodel metamodel) {
        this.metamodel = metamodel;
        this.persistenceContext = new PersistenceContext();
    }

    @Override
    public <T> T find(Class<T> entityType, Object id) {
        final LoadEvent<T> loadEvent = new LoadEvent<>(metamodel, persistenceContext, entityType, id);
        metamodel.getLoadEventListenerGroup().doEvent(loadEvent, LoadEventListener::onLoad);
        return loadEvent.getResult();
    }

    @Override
    public <T> void persist(T entity) {
        validatePersist(entity);

        final EntityTable entityTable = metamodel.getEntityTable(entity.getClass());
        if (entityTable.isIdGenerationFromDatabase()) {
            final PersistEvent<T> persistEvent = new PersistEvent<>(metamodel, persistenceContext, entity);
            metamodel.getPersistEventListenerGroup().doEvent(persistEvent, PersistEventListener::onPersist);
            return;
        }

        persistenceContext.addEntity(entity, entityTable.getIdValue(entity));
        persistenceContext.createOrUpdateStatus(entity, EntityStatus.MANAGED);
        persistenceContext.addToPersistQueue(entity);
    }

    @Override
    public void remove(Object entity) {
        final EntityEntry entityEntry = persistenceContext.getEntityEntry(entity);
        if (!entityEntry.isRemovable()) {
            throw new IllegalStateException(NOT_REMOVABLE_STATUS_FAILED_MESSAGE);
        }

        final EntityTable entityTable = metamodel.getEntityTable(entity.getClass());
        persistenceContext.removeEntity(entity, entityTable.getIdValue(entity));
        persistenceContext.addToRemoveQueue(entity);
    }

    @Override
    public void flush() {
        persistAll();
        deleteAll();
        updateAll();
    }

    @Override
    public void clear() {
        persistenceContext.clear();
    }

    private void validatePersist(Object entity) {
        final EntityEntry entityEntry = persistenceContext.getEntityEntry(entity);
        if (entityEntry != null && !entityEntry.isPersistable()) {
            throw new IllegalStateException(NOT_PERSISTABLE_STATUS_FAILED_MESSAGE);
        }
    }

    private void persistImmediately(Object entity, EntityTable entityTable) {
        persist(entity, entityTable);
        persistenceContext.addEntity(entity, entityTable.getIdValue(entity));
        persistenceContext.createOrUpdateStatus(entity, EntityStatus.MANAGED);
    }

    private void persistAll() {
        final Queue<Object> persistQueue = persistenceContext.getPersistQueue();
        while (!persistQueue.isEmpty()) {
            final Object entity = persistQueue.poll();
            final EntityTable entityTable = metamodel.getEntityTable(entity.getClass());

            persist(entity, entityTable);
            persistenceContext.createOrUpdateStatus(entity, EntityStatus.MANAGED);
        }
    }

    private void persist(Object entity, EntityTable entityTable) {
        final EntityPersister entityPersister = metamodel.getEntityPersister(entity.getClass());
        entityPersister.insert(entity);
        if (entityTable.isOneToMany()) {
            final CollectionPersister collectionPersister = metamodel.getCollectionPersister(
                    entity.getClass(), entityTable.getAssociationColumnName());
            collectionPersister.insert(entityTable.getAssociationColumnValue(entity), entity);
        }
    }

    private void deleteAll() {
        final Queue<Object> removeQueue = persistenceContext.getRemoveQueue();
        while (!removeQueue.isEmpty()) {
            final Object entity = removeQueue.poll();
            final EntityPersister entityPersister = metamodel.getEntityPersister(entity.getClass());
            entityPersister.delete(entity);
        }
    }

    private void updateAll() {
        persistenceContext.getAllEntity()
                .forEach(this::update);
    }

    private void update(Object entity) {
        final EntityTable entityTable = metamodel.getEntityTable(entity.getClass());
        final Object snapshot = persistenceContext.getSnapshot(entity.getClass(), entityTable.getIdValue(entity));
        if (snapshot == null) {
            return;
        }

        final List<EntityColumn> dirtiedEntityColumns = findDirtiedEntityColumns(entity, snapshot);
        if (dirtiedEntityColumns.isEmpty()) {
            return;
        }

        final EntityPersister entityPersister = metamodel.getEntityPersister(entity.getClass());
        entityPersister.update(entity, dirtiedEntityColumns);
        persistenceContext.addEntity(entity, entityTable);
    }

    private List<EntityColumn> findDirtiedEntityColumns(Object entity, Object snapshot) {
        final EntityTable entityTable = metamodel.getEntityTable(entity.getClass());
        return entityTable.getEntityColumns()
                .stream()
                .filter(entityColumn -> isDirtied(entity, snapshot, entityColumn))
                .toList();
    }

    private boolean isDirtied(Object entity, Object snapshot, EntityColumn entityColumn) {
        return !Objects.equals(entityColumn.extractValue(entity), entityColumn.extractValue(snapshot));
    }
}
