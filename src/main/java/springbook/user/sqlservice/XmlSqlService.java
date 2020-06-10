package springbook.user.sqlservice;

import springbook.user.dao.UserDao;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class XmlSqlService implements SqlService {
    private String sqlmapFile;
    private Map<String, String> sqlMap = new HashMap<>();

    // sqlmapFile을 주입받은 이후에나 call할수 있기 때문에 생성자로 사용할 수 없다
    @PostConstruct
    public void loadSql() {
        String contextPath = Sqlmap.class.getPackage().getName();
        try {
            JAXBContext context = JAXBContext.newInstance(contextPath);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            InputStream is = UserDao.class.getResourceAsStream(this.sqlmapFile);
            Sqlmap sqlmap = (Sqlmap)unmarshaller.unmarshal(is);

            for(SqlType sql: sqlmap.getSql()) {
                sqlMap.put(sql.getKey(), sql.getValue());
            }
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public void setSqlmapFile(String sqlmapFile) {
        this.sqlmapFile = sqlmapFile;
    }

    @Override
    public String getSql(String key) throws SqlRetrievalFailureException {
        String sql  = sqlMap.get(key);
        if (sql == null) {
            throw new SqlRetrievalFailureException(key +
                    "에 대한 SQL을 찾을 수 없습니다");
        } else {
            return sql;
        }
    }
}
