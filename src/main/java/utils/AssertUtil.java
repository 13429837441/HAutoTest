package utils;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;

import java.util.List;
import java.util.Map;

public class AssertUtil {
    // 根据字段断言
    public static void assertFieldsEquals(String actual, String assertFields){
        if (StringUtils.isNotBlank(actual)){
            String key,value;
            Object document = Configuration.defaultConfiguration().jsonProvider().parse(actual);
            String[] assertFieldArray = assertFields.split(";");
            for (String assertField : assertFieldArray) {
                if (StringUtils.isNotBlank(assertField)){
                    key = assertField.split("=")[0].trim();
                    value = assertField.split("=")[1].trim();
                    String key_real = JsonPath.read(document, key).toString();
                    Assert.assertEquals(key_real, value);
                } else {
                    Assert.fail("assertField is null or empty");
                }
            }
        } else {
            Assert.fail("actual is null or empty");
        }
    }

    // 断言是否包含
    public static <T> void assertFieldsContains(T actual, T assertFields){
        if (actual instanceof String && assertFields instanceof String){
            boolean isTrue = ((String) actual).contains((String) assertFields);
            Assert.assertTrue(isTrue);
        } else if (actual instanceof List && assertFields instanceof List){
            boolean isTrue = ((List<?>) actual).containsAll((List<?>) assertFields);
            Assert.assertTrue(isTrue);
        } else if (actual instanceof Map && assertFields instanceof Map){
            boolean isTrue = ((Map<?, ?>) actual).entrySet().containsAll(((Map<?, ?>) assertFields).entrySet());
            Assert.assertTrue(isTrue);
        } else {
            Assert.fail("actual and assertFields must be of type String or List or Map");
        }
    }

}
