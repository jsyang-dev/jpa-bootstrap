package persistence.bootstrap;

import jdbc.DefaultRowMapper;
import jdbc.RowMapper;
import persistence.meta.EntityTable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RowMapperBinder {
    private final Map<String, RowMapper> rowMapperRegistry = new HashMap<>();

    public RowMapperBinder(List<Class<?>> entityTypes, EntityTableBinder entityTableBinder) {
        for (Class<?> entityType : entityTypes) {
            final EntityTable entityTable = entityTableBinder.getEntityTable(entityType);
            final DefaultRowMapper defaultRowMapper = getDefaultRowMapper(entityTable, entityTableBinder);
            rowMapperRegistry.put(entityType.getTypeName(), defaultRowMapper);
        }
    }

    public RowMapper getRowMapper(Class<?> entityType) {
        return rowMapperRegistry.get(entityType.getName());
    }

    private DefaultRowMapper getDefaultRowMapper(EntityTable entityTable, EntityTableBinder entityTableBinder) {
        if (Objects.isNull(entityTable.getAssociationEntityColumn())) {
            return new DefaultRowMapper(entityTable);
        }

        final EntityTable childEntityTable = entityTableBinder.getEntityTable(entityTable.getAssociationColumnType());
        return new DefaultRowMapper(entityTable, childEntityTable);
    }
}
