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
import persistence.meta.EntityTable;

public class Metamodel {
    private final EntityTableBinder entityTableBinder;
    private final EntityLoaderBinder entityLoaderBinder;
    private final EntityPersisterBinder entityPersisterBinder;
    private final CollectionPersisterBinder collectionPersisterBinder;

    public Metamodel(JdbcTemplate jdbcTemplate, EntityBinder entityBinder, EntityTableBinder entityTableBinder) {
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

    public void close() {
        entityTableBinder.clear();
        entityLoaderBinder.clear();
        entityPersisterBinder.clear();
        collectionPersisterBinder.clear();
    }
}
