package persistence.entity.impl;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.sql.dialect.H2ColumnType;
import persistence.sql.schema.meta.EntityClassMappingMeta;
import persistence.sql.schema.meta.EntityObjectMappingMeta;
import registry.EntityMetaRegistry;

@DisplayName("EntityIdentifier 테스트")
class EntityIdentifierTest {

    private static final Class<EntityIdentifierTestEntity> testClazz = EntityIdentifierTestEntity.class;
    private static EntityMetaRegistry entityMetaRegistry;

    @BeforeAll
    static void setMetaRegistry() {
        entityMetaRegistry = EntityMetaRegistry.of(new H2ColumnType());
        entityMetaRegistry.addEntityMeta(EntityIdentifierTestEntity.class);
    }

    @Test
    @DisplayName("Entity 식별자가 같으면 두 Entity는 같다.")
    void entityIdenfierIsSameThenEntityAlsoSame() {
        final EntityIdentifierTestEntity identity1 = new EntityIdentifierTestEntity(1L, "identity", "identity@gmail.com");
        final EntityIdentifierTestEntity identity2 = new EntityIdentifierTestEntity(1L, "identity", "identity@gmail.com");

        final EntityClassMappingMeta entityClassMappingMeta = entityMetaRegistry.getEntityMeta(testClazz);
        final EntityObjectMappingMeta objectMappingMeta1 = EntityObjectMappingMeta.of(identity1, entityClassMappingMeta);
        final EntityObjectMappingMeta objectMappingMeta2 = EntityObjectMappingMeta.of(identity2, entityClassMappingMeta);

        assertThat(objectMappingMeta1.getEntityIdentifier()).isEqualTo(objectMappingMeta2.getEntityIdentifier());
    }

    @Test
    @DisplayName("Entity 식별자가 다르면 두 Entity는 다르다.")
    void canMakeEntityIdentifierWithColumnAndValueMeta() {
        final EntityIdentifierTestEntity identity1 = new EntityIdentifierTestEntity(1L, "identity", "identity@gmail.com");
        final EntityIdentifierTestEntity identity2 = new EntityIdentifierTestEntity(2L, "identity", "identity@gmail.com");

        final EntityClassMappingMeta entityClassMappingMeta = entityMetaRegistry.getEntityMeta(testClazz);

        final EntityObjectMappingMeta objectMappingMeta1 = EntityObjectMappingMeta.of(identity1, entityClassMappingMeta);
        final EntityObjectMappingMeta objectMappingMeta2 = EntityObjectMappingMeta.of(identity2, entityClassMappingMeta);

        assertThat(objectMappingMeta1.getEntityIdentifier()).isNotEqualTo(objectMappingMeta2.getEntityIdentifier());
    }

    @Entity
    private static class EntityIdentifierTestEntity {

        @Id
        private Long id;

        private String name;

        private String email;

        public EntityIdentifierTestEntity(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        protected EntityIdentifierTestEntity() {

        }
    }
}
