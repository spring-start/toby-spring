package springbook.user.sqlservice;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import springbook.user.dao.UserDao;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;

public class JaxbXmlSqlReader implements SqlReader {
    private static final String DEFAULT_SQLMAP_FILE = "sqlmap.xml";
    private Resource sqlmap = new ClassPathResource(DEFAULT_SQLMAP_FILE, UserDao.class);

    public void setSqlmap(Resource sqlmap) {
        this.sqlmap = sqlmap;
    }

    @Override
    public void read(SqlRegistry sqlRegistry) {
        String contextPath = Sqlmap.class.getPackage().getName();
        try {
            JAXBContext context = JAXBContext.newInstance(contextPath);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            Source source = new StreamSource(sqlmap.getInputStream());
            Sqlmap sqlmap = (Sqlmap)unmarshaller.unmarshal(source);

            // 직접적으로 sqlmap에 접근하지 않고 sqlRegistery를 이용했음을 주목
            for(SqlType sql: sqlmap.getSql()) {
                sqlRegistry.registerSql(sql.getKey(), sql.getValue());
            }
        } catch (JAXBException | IOException e) {
            throw new RuntimeException(e);
        }

    }
}
