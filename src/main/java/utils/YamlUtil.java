package utils;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
示例1：
    EnvConfigList envConfig = YamlUtil.readEnvConfig();
    envConfig.getUrl("UAT")                                     // 获取对应环境的url
    envConfig.setUrl("UAT", "https://quote.alltick.co");        // 修改对应环境的url
    YamlUtil.writeEnvConfig(envConfig);
 示例2：
    VarConfigList varConfig = YamlUtil.readVarConfig();
    System.out.println(varConfig.getVarValue("#{username}"));
    varConfig.setVarValue("#{username}", "Jack black", Optional.of("传值")); // desc传值
    varConfig.setVarValue("#{username}", "Jack black", Optional.empty());   // desc不传
    YamlUtil.writeVarConfig(varConfig);
*/

//本类主要实现yaml文件操作
public class YamlUtil {

    public static Logger logger = Logger.getLogger(YamlUtil.class);

    // 存放环境变量的map
    public static Map<String, String> envVariableMap = new HashMap<String, String>();

    private static final String EnvFilePath =
            new File(YamlUtil.class.getResource("").toString()).getParent().substring(6)
            + "\\resources\\testdata\\env.yaml";

    private static final String VarFilePath =
            new File(YamlUtil.class.getResource("").toString()).getParent().substring(6)
            + "\\resources\\testdata\\variables.yaml";

    public static void loadVariablesToMap(EnvConfig envConfig) {
        try {
            Class<?> clazz = envConfig.getClass();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                envVariableMap.put(field.getName(), (String) field.get(envConfig));
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    // 替换变量
    public static String VariableReplacement(String parameters) {
        // 获取所有变量名
        Set<String> names = envVariableMap.keySet();
        for (String name : names) {
            if (parameters.contains("#{" + name + "}")){
                parameters = parameters.replace("#{" + name + "}",envVariableMap.get(name));
                break;
            }
        }
        return parameters;
    }

    // 解析指定YAML的数据，封装到对象中【对象类型使用泛型】
    public static <T> T readYamlBeta(String fileName, Class<T> clazz) {
        Yaml yaml = new Yaml();
        // 特殊工具类
        // 根据输入流 inputStream，判断第一行是不是全局tag
        try (InputStream inputStream = new FileInputStream(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
             ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(byteArrayStream))) {

            String firstLine = reader.readLine();
            // 判断第一行是否以 "!!" 开头
            if (firstLine!= null && firstLine.startsWith("!!")) {
                firstLine = reader.readLine();
            }

            if (firstLine!= null) {
                writer.write(firstLine);
                writer.newLine();
            }

            String line;
            while ((line = reader.readLine())!= null) {
                writer.write(line);
                writer.newLine();
            }
            writer.flush();
            // 生成不包含!!的输入流modifiedInputStream
            InputStream modifiedInputStream = new java.io.ByteArrayInputStream(byteArrayStream.toByteArray());
            // snakeyaml加载
            return yaml.loadAs(modifiedInputStream, clazz);


        } catch (FileNotFoundException e) {
            System.err.println("File no found: " + fileName);
            logger.error(e.toString());
        } catch (IOException e) {
            System.err.println("Read YAML file error: " + fileName);
            logger.error(e.toString());
        } catch (Exception e) {
             System.err.println("Read YAML file error:: " + fileName);
             logger.error(e.toString());
        }
        return null;
    }

    // 读取环境配置
    public static EnvConfigList readEnvConfig() {
        logger.info("===================读取环境配置: env.yaml");
        return readYamlBeta(EnvFilePath, EnvConfigList.class);
    }

    // 写入环境配置
    public static void writeEnvConfig(EnvConfigList envConfigList) {
        logger.info("===================修改环境配置: env.yaml");
        try (Writer out = new FileWriter(EnvFilePath)) {
            Yaml yaml = new Yaml();
            yaml.dump(envConfigList, out);
        } catch (IOException e) {
            logger.error(e.toString());
            throw new RuntimeException(e);
        }
    }

    // 读取变量配置
    public static VarConfigList readVarConfig() {
        logger.info("===================读取变量配置: variables.yaml");
        return readYamlBeta(VarFilePath, VarConfigList.class);
    }

    // 写入变量配置
    public static void writeVarConfig(VarConfigList varConfigList) {
        logger.info("===================修改变量配置: variables.yaml");
        try (Writer out = new FileWriter(VarFilePath)) {
            Yaml yaml = new Yaml();
            yaml.dump(varConfigList, out);
        } catch (IOException e) {
            logger.error(e.toString());
            throw new RuntimeException(e);
        }
    }
}
