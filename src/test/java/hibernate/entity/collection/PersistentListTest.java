package hibernate.entity.collection;

import database.DatabaseServer;
import database.H2;
import hibernate.ddl.CreateQueryBuilder;
import hibernate.entity.EntityLoader;
import hibernate.entity.meta.EntityClass;
import jakarta.persistence.*;
import jdbc.JdbcTemplate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class PersistentListTest {

    private static DatabaseServer server;
    private static JdbcTemplate jdbcTemplate;

    @BeforeAll
    static void beforeAll() throws SQLException {
        server = new H2();
        server.start();
        jdbcTemplate = new JdbcTemplate(server.getConnection());

        jdbcTemplate.execute(CreateQueryBuilder.INSTANCE.generateQuery(new EntityClass<>(TestEntity.class)));
    }

    @AfterAll
    static void afterAll() {
        jdbcTemplate.execute("drop table test_entity;");
        server.stop();
    }

    @Test
    void 데이터_사용시점에_load하여_사용한다() {
        // given
        jdbcTemplate.execute("insert into test_entity (id, nick_name, age) values (1, '최진영', 19)");
        jdbcTemplate.execute("insert into test_entity (id, nick_name, age) values (2, '진영최', 29)");

        EntityLoader<TestEntity> entityLoader = new EntityLoader<>(jdbcTemplate, new EntityClass<>(TestEntity.class));

        // when
        int actual = new PersistentList<>(new EntityClass<>(TestEntity.class), entityLoader)
                .size();

        // then
        assertThat(actual).isEqualTo(2);
    }

    @Entity
    @Table(name = "test_entity")
    private static class TestEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "nick_name")
        private String name;

        private Integer age;

        @Transient
        private String email;

        public TestEntity() {
        }

        public TestEntity(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public TestEntity(String name) {
            this.name = name;
        }
    }
}
