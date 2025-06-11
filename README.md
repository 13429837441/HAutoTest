# HAutoTest
Testng+allure实现接口自动化

## 用例数据字段解释
Url：只写路径，不需要写ip、端口
RequestType：目前只支持post、get
Headers：json格式字符串，如果没有请求头，填写为{}
Cookies：预留，暂未使用
BodyType：请求body参数类型（目前post只支持json|x-www-form-urlencoded）
Parameters：json格式字符串，如果是非关联变量（也就是上面的常用变量），填写为#{xxx}，如果是关联参数，填写为${xxx}
UploadFile：预留，暂未使用
InitSql：初始化sql，要求是json数组，例如：[{"sqlNo":"1","sql":"delete from users where username = '#{username}';"}]，用到的常用变量，填写为#{xxx}
GlobalVariables：需要关联的参数值，样例：token=$.token;，左侧是字段名，右侧是其jsonpath路径，并以英文分号结尾，多个断言字段用英文分号间隔
AssertFields：要断言字段的jsonpath路径以及值，并以英文分号结尾，多个断言字段用英文分号间隔，如：$.msg=ok;$.ret=200;
