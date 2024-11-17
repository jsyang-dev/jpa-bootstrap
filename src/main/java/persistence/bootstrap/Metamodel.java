package persistence.bootstrap;

import jdbc.JdbcTemplate;
import persistence.bootstrap.binder.CollectionLoaderBinder;
import persistence.bootstrap.binder.CollectionPersisterBinder;
import persistence.bootstrap.binder.EntityBinder;
import persistence.bootstrap.binder.EntityLoaderBinder;
import persistence.bootstrap.binder.EntityPersisterBinder;
import persistence.bootstrap.binder.EntityTableBinder;
import persistence.bootstrap.binder.RowMapperBinder;
import persistence.entity.loader.EntityLoader;
import persistence.entity.persister.CollectionPersister;
import persistence.entity.persister.EntityPersister;
import persistence.event.EventListenerGroup;
import persistence.event.EventListenerRegistry;
import persistence.event.EventType;
import persistence.event.clear.ClearEventListener;
import persistence.event.delete.DeleteEventListener;
import persistence.event.dirtycheck.DirtyCheckEventListener;
import persistence.event.flush.FlushEventListener;
import persistence.event.load.LoadEventListener;
import persistence.event.merge.MergeEventListener;
import persistence.event.persist.PersistEventListener;
import persistence.event.update.UpdateEventListener;
import persistence.meta.EntityTable;

public class Metamodel {
    private final EntityTableBinder entityTableBinder;
    private final EntityLoaderBinder entityLoaderBinder;
    private final EntityPersisterBinder entityPersisterBinder;
    private final CollectionPersisterBinder collectionPersisterBinder;
    private final EventListenerRegistry eventListenerRegistry;

    public Metamodel(JdbcTemplate jdbcTemplate, EntityBinder entityBinder, EntityTableBinder entityTableBinder,
                     EventListenerRegistry eventListenerRegistry) {
        this.eventListenerRegistry = eventListenerRegistry;

        RowMapperBinder rowMapperBinder = new RowMapperBinder(entityBinder, entityTableBinder);
        CollectionLoaderBinder collectionLoaderBinder = new CollectionLoaderBinder(entityBinder, entityTableBinder, rowMapperBinder, jdbcTemplate);

        this.collectionPersisterBinder = new CollectionPersisterBinder(entityBinder, entityTableBinder, jdbcTemplate);
        this.entityLoaderBinder = new EntityLoaderBinder(entityBinder, entityTableBinder, collectionLoaderBinder, rowMapperBinder, jdbcTemplate);
        this.entityPersisterBinder = new EntityPersisterBinder(entityBinder, entityTableBinder, jdbcTemplate);
        this.entityTableBinder = entityTableBinder;
    }

    public EntityTable getEntityTable(Class<?> entityType) {
        return entityTableBinder.getEntityTable(entityType);
    }

    public EntityLoader getEntityLoader(Class<?> entityType) {
        return entityLoaderBinder.getEntityLoader(entityType);
    }

    public EntityPersister getEntityPersister(Class<?> entityType) {
        return entityPersisterBinder.getEntityPersister(entityType);
    }

    public CollectionPersister getCollectionPersister(Class<?> entityType, String columnName) {
        return collectionPersisterBinder.getCollectionPersister(entityType, columnName);
    }

    public EventListenerGroup<LoadEventListener> getLoadEventListenerGroup() {
        return (EventListenerGroup<LoadEventListener>) eventListenerRegistry.getEventListenerGroup(EventType.LOAD);
    }

    public EventListenerGroup<PersistEventListener> getPersistEventListenerGroup() {
        return (EventListenerGroup<PersistEventListener>) eventListenerRegistry.getEventListenerGroup(EventType.PERSIST);
    }

    public EventListenerGroup<DeleteEventListener> getDeleteEventListenerGroup() {
        return (EventListenerGroup<DeleteEventListener>) eventListenerRegistry.getEventListenerGroup(EventType.DELETE);
    }
    public EventListenerGroup<UpdateEventListener> getUpdateEventListenerGroup() {
        return (EventListenerGroup<UpdateEventListener>) eventListenerRegistry.getEventListenerGroup(EventType.UPDATE);
    }

    public EventListenerGroup<DirtyCheckEventListener> getDirtyCheckEventListenerGroup() {
        return (EventListenerGroup<DirtyCheckEventListener>) eventListenerRegistry.getEventListenerGroup(EventType.DIRTY_CHECK);
    }

    public EventListenerGroup<MergeEventListener> getMergeEventListenerGroup() {
        return (EventListenerGroup<MergeEventListener>) eventListenerRegistry.getEventListenerGroup(EventType.MERGE);
    }

    public EventListenerGroup<FlushEventListener> getFlushEventListenerGroup() {
        return (EventListenerGroup<FlushEventListener>) eventListenerRegistry.getEventListenerGroup(EventType.FLUSH);
    }

    public EventListenerGroup<ClearEventListener> getClearEventListenerGroup() {
        return (EventListenerGroup<ClearEventListener>) eventListenerRegistry.getEventListenerGroup(EventType.CLEAR);
    }

    public void close() {
        entityTableBinder.clear();
        entityLoaderBinder.clear();
        entityPersisterBinder.clear();
        collectionPersisterBinder.clear();
        eventListenerRegistry.clear();
    }
}
