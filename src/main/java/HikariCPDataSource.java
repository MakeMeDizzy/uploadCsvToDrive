import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class HikariCPDataSource {

    private static HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;

    static {
        Properties properties = new Properties();
        try (InputStream inputStream = HikariCPDataSource.class.getClassLoader().getResourceAsStream("sent.properties")) {
            properties.load(inputStream);
            config.setJdbcUrl(properties.getProperty("jdbcUrl.t"));
            config.setUsername(properties.getProperty("usernamedb.t"));
            config.setPassword(properties.getProperty("passworddb.t"));
            config.addDataSourceProperty("cachePrepStmts.t",properties.getProperty("cachePrepStmts.t"));
            config.addDataSourceProperty("prepStmtCacheSize.t", properties.getProperty("prepStmtCacheSize.t"));
            config.addDataSourceProperty("prepStmtCacheSqlLimit.t", properties.getProperty("prepStmtCacheSqlLimit.t"));
            ds = new HikariDataSource(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    private HikariCPDataSource(){}

}
