package persistence.meta;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class EntityTable {
    public static final EntityTable EMPTY = new EntityTable(EmptyEntity.class);
    public static final String NOT_ENTITY_FAILED_MESSAGE = "클래스에 @Entity 애노테이션이 없습니다.";
    private static final String ALIAS_PREFIX = "_";

    private final Class<?> type;
    private final TableName tableName;
    private final EntityColumns entityColumns;

    public EntityTable(Class<?> entityType) {
        validate(entityType);
        this.type = entityType;
        this.tableName = new TableName(entityType);
        this.entityColumns = new EntityColumns(entityType);
    }

    public Class<?> getType() {
        return type;
    }

    public String getTableName() {
        return tableName.value();
    }

    public List<EntityColumn> getEntityColumns() {
        return entityColumns.getEntityColumns();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityTable that = (EntityTable) o;
        return Objects.equals(type, that.type) && Objects.equals(tableName, that.tableName) && Objects.equals(entityColumns, that.entityColumns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, tableName, entityColumns);
    }

    public EntityColumn getIdEntityColumn() {
        return entityColumns.getIdEntityColumn();
    }

    public String getIdColumnName() {
        return getIdEntityColumn().getColumnName();
    }

    public Object getIdValue(Object entity) {
        return getIdEntityColumn().extractValue(entity);
    }

    public boolean isIdGenerationFromDatabase() {
        return getIdEntityColumn().isIdGenerationFromDatabase();
    }

    public boolean isOneToMany() {
        final EntityColumn associationEntityColumn = getAssociationEntityColumn();
        if (associationEntityColumn == null) {
            return false;
        }
        return associationEntityColumn.isOneToMany();
    }

    public boolean isEager() {
        final EntityColumn associationEntityColumn = getAssociationEntityColumn();
        return associationEntityColumn.getFetchType() == FetchType.EAGER;
    }

    public EntityColumn getAssociationEntityColumn() {
        return entityColumns.getAssociationEntityColumn();
    }

    public Class<?> getAssociationColumnType() {
        final EntityColumn associationEntityColumn = getAssociationEntityColumn();
        if (associationEntityColumn == null) {
            return Object.class;
        }
        return associationEntityColumn.getAssociationColumnType();
    }

    public String getAssociationColumnName() {
        return getAssociationEntityColumn().getColumnName();
    }

    public List<?> getAssociationColumnValue(Object entity) {
        return (List<?>) getAssociationEntityColumn().extractValue(entity);
    }

    public Field getAssociationField() {
        final EntityColumn associationEntityColumn = getAssociationEntityColumn();
        return associationEntityColumn.getField();
    }

    public boolean isSimpleMapping() {
        return getAssociationEntityColumn() == null || getAssociationEntityColumn().isOneToManyAndLazy();
    }

    public String getAlias() {
        return ALIAS_PREFIX + getTableName();
    }

    public AssociationCondition getAssociationCondition(Object entity) {
        return new AssociationCondition(getAssociationColumnName(), getIdValue(entity));
    }

    public List<Field> getFields() {
        return entityColumns.getEntityColumns()
                .stream()
                .map(EntityColumn::getField)
                .collect(Collectors.toList());
    }

    private void validate(Class<?> entityType) {
        if (!entityType.isAnnotationPresent(Entity.class)) {
            throw new IllegalArgumentException(NOT_ENTITY_FAILED_MESSAGE);
        }
    }

    @Entity
    private static class EmptyEntity {
        @Id
        private Long id;
    }
}
