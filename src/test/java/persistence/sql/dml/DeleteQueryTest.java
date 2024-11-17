package persistence.sql.dml;

import fixture.EntityWithId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.meta.EntityTable;

import static org.assertj.core.api.Assertions.*;

class DeleteQueryTest {
    @Test
    @DisplayName("delete 쿼리를 생성한다.")
    void delete() {
        // given
        final DeleteQuery deleteQuery = DeleteQuery.getInstance();
        final EntityWithId entity = new EntityWithId(1L, "Jaden", 30, "test@email.com");
        final EntityTable entityTable = new EntityTable(EntityWithId.class);

        // when
        final String sql = deleteQuery.delete(entityTable, entity);

        // then
        assertThat(sql).isEqualTo("DELETE FROM entity WHERE id = 1");
    }
}
