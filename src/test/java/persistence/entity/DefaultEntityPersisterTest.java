package persistence.entity;

import database.H2ConnectionFactory;
import jdbc.JdbcTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.entity.proxy.ProxyFactory;
import persistence.fixture.EntityWithId;
import persistence.meta.EntityTable;
import persistence.sql.dml.DeleteQuery;
import persistence.sql.dml.InsertQuery;
import persistence.sql.dml.SelectQuery;
import persistence.sql.dml.UpdateQuery;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static util.QueryUtils.*;

class DefaultEntityPersisterTest {
    private JdbcTemplate jdbcTemplate;
    private EntityLoader entityLoader;
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        jdbcTemplate = new JdbcTemplate(H2ConnectionFactory.getConnection());
        entityLoader = new DefaultEntityLoader(jdbcTemplate, new SelectQuery(), new ProxyFactory());
        entityManager = DefaultEntityManager.of(jdbcTemplate);

        createTable(EntityWithId.class);
    }

    @AfterEach
    void tearDown() {
        dropTable(EntityWithId.class);
    }

    @Test
    @DisplayName("엔티티를 저장한다.")
    void insert() {
        // given
        final EntityPersister entityPersister = new DefaultEntityPersister(jdbcTemplate, new InsertQuery(),
                new UpdateQuery(), new DeleteQuery());
        final EntityWithId entity = new EntityWithId("Jaden", 30, "test@email.com", 1);

        // when
        entityPersister.insert(entity);

        // then
        final EntityWithId managedEntity = entityLoader.load(entity.getClass(), entity.getId());
        assertAll(
                () -> assertThat(managedEntity).isNotNull(),
                () -> assertThat(managedEntity.getId()).isNotNull(),
                () -> assertThat(managedEntity.getName()).isEqualTo(entity.getName()),
                () -> assertThat(managedEntity.getAge()).isEqualTo(entity.getAge()),
                () -> assertThat(managedEntity.getEmail()).isEqualTo(entity.getEmail()),
                () -> assertThat(managedEntity.getIndex()).isNull()
        );
    }

    @Test
    @DisplayName("엔티티를 수정한다.")
    void update() {
        // given
        final EntityPersister entityPersister = new DefaultEntityPersister(jdbcTemplate, new InsertQuery(),
                new UpdateQuery(), new DeleteQuery());
        final EntityWithId entity = new EntityWithId("Jaden", 30, "test@email.com", 1);
        insertData(entity);
        final EntityWithId updatedEntity = new EntityWithId(entity.getId(), "Jackson", 20, "test2@email.com");
        final EntityTable entityTable = new EntityTable(updatedEntity);

        // when
        entityPersister.update(entity, entityTable.getEntityColumns());

        // then
        final EntityWithId managedEntity = entityLoader.load(entity.getClass(), entity.getId());
        assertAll(
                () -> assertThat(managedEntity).isNotNull(),
                () -> assertThat(managedEntity.getId()).isEqualTo(updatedEntity.getId()),
                () -> assertThat(managedEntity.getName()).isEqualTo(updatedEntity.getName()),
                () -> assertThat(managedEntity.getAge()).isEqualTo(updatedEntity.getAge()),
                () -> assertThat(managedEntity.getEmail()).isEqualTo(updatedEntity.getEmail()),
                () -> assertThat(managedEntity.getIndex()).isNull()
        );
    }

    @Test
    @DisplayName("엔티티를 삭제한다.")
    void delete() {
        // given
        final EntityPersister entityPersister = new DefaultEntityPersister(jdbcTemplate, new InsertQuery(),
                new UpdateQuery(), new DeleteQuery());
        final EntityWithId entity = new EntityWithId("Jaden", 30, "test@email.com", 1);
        insertData(entity);

        // when
        entityPersister.delete(entity);

        // then
        assertThatThrownBy(() -> entityLoader.load(entity.getClass(), entity.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Expected 1 result, got");
    }

    private void insertData(EntityWithId entity) {
        entityManager.persist(entity);
    }
}
