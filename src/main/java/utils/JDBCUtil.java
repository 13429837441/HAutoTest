package utils;

import org.apache.log4j.Logger;

import java.io.File;
import java.sql.*;
import java.io.FileInputStream;
import java.util.Objects;
import java.util.Properties;
import java.util.HashMap;

/**
 * &#064;描述:  <MYSQL工具类>
 */

public class JDBCUtil {
    public static Logger logger = Logger.getLogger(JDBCUtil.class);

    public static Properties properties = new Properties();

    private static final String MysqlConfigPath =
            new File(JDBCUtil.class.getResource("").toString()).getParent().substring(6)
            + "\\resources\\";

    public static void loadProperties(String env) {
        String filePath = MysqlConfigPath + "dbConfig-" + env.toLowerCase() + ".properties";
        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            properties.load(inputStream);
            logger.info("加载数据库配置信息：url = " +
                    properties.getProperty("jdbc.url") +
                    ", username = " +
                    properties.getProperty("jdbc.username") +
                    ", password = " +
                    properties.getProperty("jdbc.password"));
        }  catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void query(String sql) {
        HashMap<String, Object> stringObjectHashMap = null;
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            // 1、调用方法，获取连接
            connection = getConnection();
            // 2、获取PreparedStatement
            statement = connection.createStatement();
            // 3、调用查询方法，执行查询，返回结果集（ResultSet）
            resultSet = statement.executeQuery(sql);
            // 获取查询相关的信息
            ResultSetMetaData metaData = resultSet.getMetaData();
            // 获取sql中有多少个查询字段
            int columnCount = metaData.getColumnCount();

            stringObjectHashMap = new HashMap<String, Object>();
            // 从结果集获取查询数据：循环取出每个查询字段
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String columnLabel = metaData.getColumnLabel(i);
                    String columnValue = resultSet.getObject(columnLabel).toString();
                    stringObjectHashMap.put(columnLabel, columnValue);
                }
            }
            logger.info("查询结果：" + stringObjectHashMap);
        } catch (Exception e) {
            logger.error(e.toString());
        } finally {
            close(connection, statement, resultSet);
        }
    }

    public static void nonQuery(String sql) {
        Connection connection = null;
        Statement statement = null;
        try {
            // 1、调用方法，获取连接
            connection = getConnection();
            // 2、获取statement
            statement = connection.createStatement();
            // 3、调用查询方法，执行查询，返回结果集（ResultSet）
            int n = statement.executeUpdate(sql);
            if (n > 0) {
                logger.info("操作成功数据条数：【" + n + "】");
            } else {
                logger.info("操作成功数据条数：【0】");
            }
        } catch (Exception e) {
            logger.error(e.toString());
        } finally {
            close(connection, statement, null);
        }
    }

    // 获取数据库连接
    public static Connection getConnection() throws SQLException {
        String url = properties.getProperty("jdbc.url");
        String username = properties.getProperty("jdbc.username");
        String password = properties.getProperty("jdbc.password");
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * 关闭数据库相关资源
     * @param conn 数据库连接对象
     * @param ps sql语句执行对象
     * @param rs 查询结果集
     */
    public static void close(Connection conn, Statement ps, ResultSet rs) {
        try {
            if (conn != null) conn.close();
            if (ps != null) ps.close();
            if (rs != null) rs.close();
        } catch (SQLException e) {
            logger.error(e.toString());
        }
    }

}
