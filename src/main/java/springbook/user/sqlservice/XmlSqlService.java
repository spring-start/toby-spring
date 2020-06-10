package springbook.user.sqlservice;

import springbook.user.dao.UserDao;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

// 헷갈리기만 하고 딱히 장점은 없는 듯
// 괜히 사람이 더 주의해야 한다
public class XmlSqlService implements SqlService, SqlRegistry, SqlReader {
    private SqlReader sqlReader;
    private SqlRegistry sqlRegistry;
    private String sqlmapFile;
    private Map<String, String> sqlMap = new HashMap<>();

    public void setSqlReader(SqlReader sqlReader) {
        this.sqlReader = sqlReader;
    }

    public void setSqlRegistry(SqlRegistry sqlRegistry) {
        this.sqlRegistry = sqlRegistry;
    }

    public void setSqlmapFile(String sqlmapFile) {
        this.sqlmapFile = sqlmapFile;
    }

    // sqlmapFile을 주입받은 이후에나 call할수 있기 때문에 생성자로 사용할 수 없다
    @PostConstruct
    public void loadSql() {
        this.sqlReader.read(sqlRegistry);
    }


    @Override
    public String getSql(String key) throws SqlRetrievalFailureException {
        try {
            return this.sqlRegistry.findSql(key);
        } catch (SqlNotFoundException e) {
            throw new SqlRetrievalFailureException("에러", e);
        }

    }

    @Override
    public void registerSql(String key, String sql) {
        sqlMap.put(key,sql);
    }

    @Override
    public String findSql(String key) throws SqlNotFoundException {
        String sql  = sqlMap.get(key);
        if (sql == null) {
            throw new SqlRetrievalFailureException(key +
                    "에 대한 SQL을 찾을 수 없습니다");
        } else {
            return sql;
        }
    }

    @Override
    public void read(SqlRegistry sqlRegistry) {
        String contextPath = Sqlmap.class.getPackage().getName();
        try {
            JAXBContext context = JAXBContext.newInstance(contextPath);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            InputStream is = UserDao.class.getResourceAsStream(this.sqlmapFile);
            Sqlmap sqlmap = (Sqlmap)unmarshaller.unmarshal(is);

            // 직접적으로 sqlmap에 접근하지 않고 sqlRegistery를 이용했음을 주목
            for(SqlType sql: sqlmap.getSql()) {
                sqlRegistry.registerSql(sql.getKey(), sql.getValue());
            }
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
