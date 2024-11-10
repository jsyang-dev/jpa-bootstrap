package persistence.bootstrap;

import jdbc.JdbcTemplate;
import jdbc.RowMapper;
import persistence.entity.CollectionLoader;
import persistence.entity.EntityLoader;
import persistence.entity.proxy.ProxyFactory;
import persistence.meta.EntityTable;
import persistence.sql.dml.DmlQueries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EntityLoaderBinder {
    private final Map<String, EntityLoader> entityLoaderRegistry = new HashMap<>();

    public EntityLoaderBinder(List<Class<?>> entityTypes, EntityTableBinder entityTableBinder,
                              CollectionLoaderBinder collectionLoaderBinder, RowMapperBinder rowMapperBinder, JdbcTemplate jdbcTemplate,
                              DmlQueries dmlQueries, ProxyFactory proxyFactory) {
        for (Class<?> entityType : entityTypes) {
            final EntityTable entityTable = entityTableBinder.getEntityTable(entityType);
            final EntityLoader entityLoader =
                    getEntityLoader(collectionLoaderBinder, rowMapperBinder, jdbcTemplate, dmlQueries, proxyFactory, entityType, entityTable);
            entityLoaderRegistry.put(entityType.getTypeName(), entityLoader);
        }
    }

    public EntityLoader getEntityLoader(Class<?> entityType) {
        return entityLoaderRegistry.get(entityType.getName());
    }

    private EntityLoader getEntityLoader(CollectionLoaderBinder collectionLoaderBinder, RowMapperBinder rowMapperBinder,
                                         JdbcTemplate jdbcTemplate, DmlQueries dmlQueries, ProxyFactory proxyFactory,
                                         Class<?> entityType, EntityTable entityTable) {
        final RowMapper<?> rowMapper = rowMapperBinder.getRowMapper(entityType);
        if (Objects.isNull(entityTable.getAssociationEntityColumn())) {
            return new EntityLoader(entityTable, jdbcTemplate, dmlQueries.getSelectQuery(), proxyFactory, rowMapper, null);
        }

        final CollectionLoader collectionLoader =
                collectionLoaderBinder.getCollectionLoader(entityType, entityTable.getAssociationColumnName());
        return new EntityLoader(entityTable, jdbcTemplate, dmlQueries.getSelectQuery(), proxyFactory, rowMapper, collectionLoader);
    }
}
