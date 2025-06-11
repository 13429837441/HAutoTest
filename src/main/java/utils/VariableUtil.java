package utils;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class VariableUtil {
    public static Logger logger = Logger.getLogger(VariableUtil.class);

    // 存放变量和值的map
    public static Map<String, String> variableMap = new HashMap<String, String>();

    // 替换符
    public static Pattern replaceParamPattern = Pattern.compile("\\$\\{(.*?)}");

    // 存放全局变量的map
    public static Map<String, Object> globalVariableMap = new HashMap<String, Object>();

    public static void loadVariablesToMap(List<VarConfig> varConfigList) {
        for (VarConfig varConfig : varConfigList) {
            String prefix = varConfig.getName().trim().toLowerCase();
            if (prefix.startsWith("#")) {
                variableMap.put(varConfig.getName(), varConfig.getValue());
            } else {
                globalVariableMap.put(varConfig.getName(), varConfig.getValue());
            }
        }
    }

    // 替换变量
    public static String variableReplacement(String parameters) {
        // 获取所有变量名
        Set<String> names = variableMap.keySet();
        for (String name : names) {
            if (parameters.contains(name)){
                parameters = parameters.replace(name,variableMap.get(name));
                break;
            }
        }
        return parameters;
    }

    public static String globalVariableReplacement(String parameters){
        if (!StringUtils.isNotBlank(parameters)){
            return "";
        }
        Matcher matcher = replaceParamPattern.matcher(parameters);
        while (matcher.find()) {
            // 得到第一个匹配内容
            String replaceKey = matcher.group(1).trim();
            String value;
            // 从全局变量map中获取值
            if (replaceKey.isEmpty() || !globalVariableMap.containsKey(replaceKey)){
                value = null;
                logger.error("【" + replaceKey + "】 变量未被发现！");
            } else {
                value = (String) globalVariableMap.get(replaceKey);
            }
            if (value != null) {
                parameters = parameters.replace(matcher.group(), value);
            }
        }
        return parameters;
    }

    public static void globalVariableSave(String res, String globalVariables){
        if (!StringUtils.isNotBlank(globalVariables) || !StringUtils.isNotBlank(res)){
            return;
        }

        try {
            String key,value;
            Object document = Configuration.defaultConfiguration().jsonProvider().parse(res);
            String[] globalVariableList = globalVariables.split(";");
            for (String globalVariable : globalVariableList) {
                if (StringUtils.isNotBlank(globalVariable)){
                    key = globalVariable.split("=")[0].trim();
                    value = globalVariable.split("=")[1].trim();
                    // 解析token=$.data.token;
                    Object value_real = JsonPath.read(document, value);
                    globalVariableMap.put(key, value_real);
                }
            }
        } catch (Exception e) {
            logger.error("【" + globalVariables + "】保存全局变量失败！");
        }
    }

}
