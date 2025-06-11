package autotest.testcase;

import io.qameta.allure.*;
import org.apache.log4j.Logger;
import org.testng.annotations.*;
import org.testng.annotations.Optional;
import utils.*;
import java.util.*;


@Feature("testNG+allure接口自动化框架")
public class BaseCaseTest {
    public static Logger logger = Logger.getLogger(BaseCaseTest.class);

    // 保存环境变量对象
    public static EnvConfigList envs = new EnvConfigList();

    // 保存所有用例对象
    public static List<CaseData> cases = new ArrayList<CaseData>();

    // 存放所有变量对象
    public static VarConfigList variables = new VarConfigList();

    // 当前环境
    private static String env;

    // 用例执行前初始化测试用例
    @Parameters({"excelPath", "dataSheetName", "environment"})
    @BeforeTest
    public void initTestData(@Optional("testdata/xlsx/caseData_kline.xlsx") String excelPath, @Optional("case") String dataSheetName, @Optional("uat") String environment) {
        // 环境变量
        env = environment;
        logger.info("当前执行环境：" + env);
        envs = YamlUtil.readEnvConfig();
        YamlUtil.loadVariablesToMap(envs.getCurrentEnv(env));
        logger.info("读取文件获取到的envs对象：" + envs);
        // 读取用例
        logger.info("excelPath: " + excelPath);
        logger.info("dataSheetName: " + dataSheetName);
        cases = ExcelUtil.loadExcel(excelPath, dataSheetName, CaseData.class);
        logger.info("读取文件获取到的cases对象：" + cases);
        // 读取变量
        variables = YamlUtil.readVarConfig();
        logger.info("读取文件获取到的variables对象：" + variables);
        VariableUtil.loadVariablesToMap(variables.getAllVariables());
        Set<String> keys = VariableUtil.variableMap.keySet();
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        logger.info("============================" + methodName + " END============================");
    }

    // 数据驱动
    @DataProvider(name = "dataFromExcel")
    public Iterator<Object[]> dataFromExcel() {
        List<Object[]> apiDataList = new ArrayList<Object[]>();
        for (CaseData caseData : cases) {
            apiDataList.add(new Object[]{caseData});
        }
        return apiDataList.iterator();
    }

    @Test(dataProvider = "dataFromExcel", timeOut = 600000)
    @Story("框架功能调试")
    public void test_example(CaseData caseData){
        // 获取用例里面的参数
        String url = caseData.getUrl();
        String requestType = caseData.getRequestType();
        String headers = caseData.getHeaders();
        String bodyType = caseData.getBodyType();
        String parameters = caseData.getParameters();
        String initSql = caseData.getInitSql();
        String globalVariables = caseData.getGlobalVariables();
        String assertFields = caseData.getAssertFields();

        logger.info("url: " + url);
        logger.info("requestType: " + requestType);
        logger.info("headers: " + headers);
        logger.info("bodyType: " + bodyType);
        logger.info("parameters: " + parameters);
        logger.info("initSql: " + initSql);
        logger.info("globalVariables: " + globalVariables);
        logger.info("assertFields: " + assertFields);

        logger.info("处理前的请求参数是："+parameters);

        // 替换入参中的环境变量
        parameters = YamlUtil.VariableReplacement(parameters);
        // 替换入参中的非关联参数
        parameters = VariableUtil.variableReplacement(parameters);
        // 替换入参中的关联参数
        parameters = VariableUtil.globalVariableReplacement(parameters);

        logger.info("处理后的请求参数是："+parameters);

        // 执行用例中的初始化sql
        if (initSql!=null && !initSql.trim().isEmpty()){
            // 替换sql中的参数
            initSql = VariableUtil.variableReplacement(initSql);
            // 调用方法
            InitMysqlUtil.doInitSql(env, initSql, true);
        }

        // http请求接口(get)
        url = envs.getUrl(env) + caseData.getUrl();
        Map<String, Object> res = HttpRequestUtil.sendRequest(url, requestType, bodyType, headers, parameters);
        logger.info("请求状态码："+res.get("statusCode"));
        logger.info("请求结果："+res.get("res"));

        // 保存全局变量
        VariableUtil.globalVariableSave((String) res.get("res"), globalVariables);
        logger.info("全局变量："+VariableUtil.globalVariableMap);

        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        logger.info("============================" + methodName + " END============================");
    }

    @Test(dataProvider = "dataFromExcel", timeOut = 600000)
    @Story("框架断言调试")
    public void test_assert(CaseData caseData) {
        // 断言List是否包含
        ArrayList<String> listA = new ArrayList<String>();
        listA.add("Google");
        listA.add("Yahoo");
        listA.add("Taobao");
        ArrayList<String> listB = new ArrayList<String>();
        listB.add("Google");
        listB.add("Yahoo");
        AssertUtil.assertFieldsContains(listA, listB);

        // 断言Map是否包含
        HashMap<String, String> mapA = new HashMap<String, String>();
        mapA.put("1", "Google");
        mapA.put("2", "Yahoo");
        mapA.put("3", "Taobao");
        HashMap<String, String> mapB = new HashMap<String, String>();
        mapB.put("1", "Google");
        mapB.put("2", "Yahoo");
        AssertUtil.assertFieldsContains(mapA, mapB);

        // 断言String是否包含
        String stringA = "GoogleYahooTaobao";
        String stringB = "GoogleYahoo";
        AssertUtil.assertFieldsContains(stringA, stringB);
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        logger.info("============================" + methodName + " END============================");
    }
}


