package springbook.user.sqlservice;

import org.junit.After;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.HSQL;

public class EmbeddedDbSqlRegistryTest extends AbstractUpdatableSqlRegistryTest {
    EmbeddedDatabase db;

    @Override
    protected UpdatableSqlRegistry createUpdatableSqlRegistry() {
        db = new EmbeddedDatabaseBuilder()
                .setType(HSQL)
                .addScript("classpath:schema.sql")
                .build();

        EmbeddedDbSqlRegistry embeddedDbSqlRegistry = new EmbeddedDbSqlRegistry();
        embeddedDbSqlRegistry.setDataSource(db);

        return embeddedDbSqlRegistry;
    }

    @After
    public void tearDown() {
        db.shutdown();
    }

    @Override
    public void setUp() {
        super.setUp();
    }

    @Override
    public void find() {
        super.find();
    }

    @Override
    protected void checkFinkdResult(String expected1, String expected2, String expected3) {
        super.checkFinkdResult(expected1, expected2, expected3);
    }

    @Override
    public void unknownKey() {
        super.unknownKey();
    }

    @Override
    public void updateSingle() {
        super.updateSingle();
    }

    @Override
    public void updateMulti() {
        super.updateMulti();
    }

    @Override
    public void updatedWithNotExistingKey() {
        super.updatedWithNotExistingKey();
    }
}
