call mvn clean test
call allure generate target/allure-results -o allure-report --clean
call allure open allure-report