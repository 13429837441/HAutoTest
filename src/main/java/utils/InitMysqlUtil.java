package utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;
import java.util.List;

/**
 * &#064;描述:  <初始化MYSQL工具类>
 */

public class InitMysqlUtil {
    public static Logger logger = Logger.getLogger(InitMysqlUtil.class);

    public static void doInitSql(String env, String initSql, boolean debug) {
        // 加载mysql属性配置
        JDBCUtil.loadProperties(env);
        // 初始化sql解析为对象
        List<Sql> sqlList = JSONObject.parseArray(initSql, Sql.class);
        // 遍历获取sql，并执行
        for (Sql sql_ : sqlList) {
            String sqlNo = sql_.getSqlNo();
            String sql = sql_.getSql().trim();
            logger.info("准备执行sql编号：sqlNo="+sqlNo);
            String prefix = sql.split(" ")[0].trim().toLowerCase();
            if (prefix.startsWith("select")){
                // 执行查询sql，获取执行结果
                if (!debug) {
                    logger.info("=====执行select语句：" + sql);
                    JDBCUtil.query(sql);
                }
            } else if (prefix.startsWith("delete")){
                if (!debug) {
                    logger.info("=====执行delete语句：" + sql);
                    JDBCUtil.nonQuery(sql);
                }
            } else if (prefix.startsWith("update")){
                if (!debug) {
                    logger.info("=====执行update语句：" + sql);
                    JDBCUtil.nonQuery(sql);
                }
            } else if (prefix.startsWith("insert")){
                if (!debug) {
                    logger.info("=====执行insert语句：" + sql);
                    JDBCUtil.nonQuery(sql);
                }
            } else {
                logger.info("=====sql语句【"+ sql_ +"】不符合规范");
            }
        }
    }
}
