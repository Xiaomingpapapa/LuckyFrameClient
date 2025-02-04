package luckyclient.caserun.exwebdriver.ex;

import luckyclient.caserun.exinterface.TestCaseExecution;
import luckyclient.caserun.exinterface.analyticsteps.InterfaceAnalyticCase;
import luckyclient.caserun.exwebdriver.BaseWebDrive;
import luckyclient.caserun.exwebdriver.EncapsulateOperation;
import luckyclient.caserun.publicdispose.ActionManageForSteps;
import luckyclient.caserun.publicdispose.ChangString;
import luckyclient.caserun.publicdispose.ParamsManageForSteps;
import luckyclient.dblog.LogOperation;
import luckyclient.planapi.entity.ProjectCase;
import luckyclient.planapi.entity.ProjectCasesteps;
import luckyclient.planapi.entity.PublicCaseParams;
import luckyclient.publicclass.LogUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * =================================================================
 * 这是一个受限制的自由软件！您不能在任何未经允许的前提下对程序代码进行修改和用于商业用途；也不允许对程序代码修改后以任何形式任何目的的再发布。
 * 为了尊重作者的劳动成果，LuckyFrame关键版权信息严禁篡改
 * 有任何疑问欢迎联系作者讨论。 QQ:1573584944  seagull1985
 * =================================================================
 *
 * @author： seagull
 * @date 2018年3月1日
 */
public class WebCaseExecution extends TestCaseExecution {
    private static Map<String, String> variable = new HashMap<>();
    private static String casenote = "备注初始化";
    private static String imagname = "";

    public static void caseExcution(ProjectCase testcase, List<ProjectCasesteps> steps, String taskid, WebDriver wd, LogOperation caselog, List<PublicCaseParams> pcplist) throws InterruptedException {
        // 把公共参数加入到MAP中
        for (PublicCaseParams pcp : pcplist) {
            variable.put(pcp.getParamsname(), pcp.getParamsvalue());
        }
        // 加入全局变量
        variable.putAll(ParamsManageForSteps.GLOBAL_VARIABLE);
        // 0:成功 1:失败 2:锁定 其他：锁定
        int setcaseresult = 0;
        for (ProjectCasesteps step : steps) {
            Map<String, String> params;
            String result;

            // 根据步骤类型来分析步骤参数
            if (1 == step.getSteptype()){
            	params = WebDriverAnalyticCase.analyticCaseStep(testcase, step, taskid, caselog);
            }else{
            	params = InterfaceAnalyticCase.analyticCaseStep(testcase, step, taskid, caselog);
            }

            // 判断分析步骤参数是否有异常
            if (null != params.get("exception") && params.get("exception").contains("解析异常")) {
            	setcaseresult = 2;
                break;
            }

            // 根据步骤类型来执行步骤
            if (1 == step.getSteptype()){
            	result = runWebStep(params, variable, wd, taskid, testcase.getSign(), step.getStepnum(), caselog);
            }else{
            	result = TestCaseExecution.runStep(params, variable, taskid, testcase.getSign(), step, caselog);
            }

            String expectedResults = params.get("ExpectedResults");
            expectedResults = ChangString.changparams(expectedResults, variable, "预期结果");

            // 判断结果
			int stepresult = judgeResult(testcase, step, params, wd, taskid, expectedResults, result, caselog);
			// 失败，并且不在继续,直接终止
            if (0 != stepresult) {
            	setcaseresult = stepresult;
                if (testcase.getFailcontinue() == 0) {
                    luckyclient.publicclass.LogUtil.APP.error("用例【"+testcase.getSign()+"】第【"+step.getStepnum()+"】步骤执行失败，中断本条用例后续步骤执行，进入到下一条用例执行中......");
                    break;
                } else {
                    luckyclient.publicclass.LogUtil.APP.error("用例【"+testcase.getSign()+"】第【"+step.getStepnum()+"】步骤执行失败，继续本条用例后续步骤执行，进入下个步骤执行中......");
                }
            }
        }

        variable.clear();
        caselog.updateCaseDetail(taskid, testcase.getSign(), setcaseresult);
        if (setcaseresult == 0) {
            luckyclient.publicclass.LogUtil.APP.info("用例【" + testcase.getSign() + "】全部步骤执行结果成功...");
            caselog.caseLogDetail(taskid, testcase.getSign(), "用例全部步骤执行结果成功", "info", "ending", "");
        } else {
            luckyclient.publicclass.LogUtil.APP.error("用例【" + testcase.getSign() + "】步骤执行过程中失败或是锁定...请查看具体原因！" + casenote);
            caselog.caseLogDetail(taskid, testcase.getSign(), "用例执行过程中失败或是锁定" + casenote, "error", "ending", "");
        }
    }

    public static String runWebStep(Map<String, String> params, Map<String, String> variable, WebDriver wd, String taskid, String casenum, int stepno, LogOperation caselog) {
        String result = "";
        String property;
        String propertyValue;
        String operation;
        String operationValue;

        try {
            property = params.get("property");
            propertyValue = params.get("property_value");
            operation = params.get("operation");
            operationValue = params.get("operation_value");

            // 处理值传递
            property = ChangString.changparams(property, variable, "定位方式");
            propertyValue = ChangString.changparams(propertyValue, variable, "定位路径");
            operation = ChangString.changparams(operation, variable, "操作");
            operationValue = ChangString.changparams(operationValue, variable, "操作参数");

            luckyclient.publicclass.LogUtil.APP.info("二次解析用例过程完成，等待进行对象操作......");
            caselog.caseLogDetail(taskid, casenum, "对象操作:" + operation + "; 操作值:" + operationValue, "info", String.valueOf(stepno), "");
        } catch (Exception e) {
            e.printStackTrace();
            luckyclient.publicclass.LogUtil.APP.error("二次解析用例过程抛出异常！---" + e.getMessage());
            return "步骤执行失败：解析用例失败!";
        }

        try {
            //调用另一条用例，支持接口，web类型用例
            if (null != operation && null != operationValue && "runcase".equals(operation)) {
                String[] temp = operationValue.split(",", -1);
                String ex = TestCaseExecution.oneCaseExecuteForUICase(temp[0], taskid, caselog, wd);
                if (!ex.contains("CallCase调用出错！") && !ex.contains("解析出错啦！") && !ex.contains("失败")) {
                    return ex;
                } else {
                    return "步骤执行失败：调用外部用例过程失败";
                }
            }

            // 页面元素层
            if (null != property && null != propertyValue && null != operation) {
                WebElement we = isElementExist(wd, property, propertyValue);
                // 判断此元素是否存在
                if (null == we) {
                    luckyclient.publicclass.LogUtil.APP.error("定位对象失败，isElementExist为null!");
                    return "步骤执行失败：定位的元素不存在！";
                }

                if (operation.contains("select")) {
                    result = EncapsulateOperation.selectOperation(we, operation, operationValue);
                } else if (operation.contains("get")) {
                    result = EncapsulateOperation.getOperation(wd, we, operation, operationValue);
                } else if (operation.contains("mouse")) {
                    result = EncapsulateOperation.actionWeOperation(wd, we, operation, operationValue, property, propertyValue);
                } else {
                    result = EncapsulateOperation.objectOperation(wd, we, operation, operationValue, property, propertyValue);
                }
                // Driver层操作
            } else if (null == property && null != operation) {
                // 处理弹出框事件
                if (operation.contains("alert")) {
                    result = EncapsulateOperation.alertOperation(wd, operation);
                } else if (operation.contains("mouse")) {
                    result = EncapsulateOperation.actionOperation(wd, operation, operationValue);
                } else {
                    result = EncapsulateOperation.driverOperation(wd, operation, operationValue);
                }
            } else {
                luckyclient.publicclass.LogUtil.APP.error("元素操作过程失败！");
                result = "步骤执行失败：元素操作过程失败！";
            }
        } catch (Exception e) {
            luckyclient.publicclass.LogUtil.APP.error("元素定位过程或是操作过程失败或异常！" + e.getMessage());
            return "步骤执行失败：元素定位过程或是操作过程失败或异常！" + e.getMessage();
        }

        if (result.contains("步骤执行失败：")) caselog.caseLogDetail(taskid, casenum, result, "error", String.valueOf(stepno), "");
        else caselog.caseLogDetail(taskid, casenum, result, "info", String.valueOf(stepno), "");

        if (result.contains("获取到的值是【") && result.contains("】")) {
            result = result.substring(result.indexOf("获取到的值是【") + "获取到的值是【".length(), result.length() - 1);
        }
        return result;

    }

    private static WebElement isElementExist(WebDriver wd, String property, String propertyValue) {
        try {
            WebElement we = null;
            property = property.toLowerCase();
            // 处理WebElement对象定位
            switch (property) {
                case "id":
                    we = wd.findElement(By.id(propertyValue));
                    break;
                case "name":
                    we = wd.findElement(By.name(propertyValue));
                    break;
                case "xpath":
                    we = wd.findElement(By.xpath(propertyValue));
                    break;
                case "linktext":
                    we = wd.findElement(By.linkText(propertyValue));
                    break;
                case "tagname":
                    we = wd.findElement(By.tagName(propertyValue));
                    break;
                case "cssselector":
                    we = wd.findElement(By.cssSelector(propertyValue));
                    break;
                default:
                    break;
            }

            return we;

        } catch (Exception e) {
            luckyclient.publicclass.LogUtil.APP.error("当前对象定位失败：" + e.getMessage());
            return null;
        }

    }

    public static int judgeResult(ProjectCase testcase, ProjectCasesteps step, Map<String, String> params, WebDriver driver, String taskid, String expect, String result, LogOperation caselog) throws InterruptedException {
        int setresult = 0;
        java.text.DateFormat timeformat = new java.text.SimpleDateFormat("MMdd-hhmmss");
        imagname = timeformat.format(new Date());
        
        result = ActionManageForSteps.actionManage(step.getAction(), result);
        if (null != result && !result.contains("步骤执行失败：")) {
            // 有预期结果
            if (null != expect && !expect.isEmpty()) {
                luckyclient.publicclass.LogUtil.APP.info("期望结果为【" + expect + "】");
                // 赋值传参模式
                if (expect.length() > ASSIGNMENT_SIGN.length() && expect.startsWith(ASSIGNMENT_SIGN)) {
                    variable.put(expect.substring(ASSIGNMENT_SIGN.length()), result);
                    luckyclient.publicclass.LogUtil.APP.info("用例：" + testcase.getSign() + " 第" + step.getStepnum() + "步，将测试结果【" + result + "】赋值给变量【" + expect.substring(ASSIGNMENT_SIGN.length()) + "】");
                    caselog.caseLogDetail(taskid, testcase.getSign(), "将测试结果【" + result + "】赋值给变量【" + expect.substring(ASSIGNMENT_SIGN.length()) + "】", "info", String.valueOf(step.getStepnum()), "");
                }
                // 赋值全局变量
                else if (expect.length() > ASSIGNMENT_GLOBALSIGN.length() && expect.startsWith(ASSIGNMENT_GLOBALSIGN)) {
                	variable.put(expect.substring(ASSIGNMENT_GLOBALSIGN.length()), result);
                	ParamsManageForSteps.GLOBAL_VARIABLE.put(expect.substring(ASSIGNMENT_GLOBALSIGN.length()), result);
                    luckyclient.publicclass.LogUtil.APP.info("用例：" + testcase.getSign() + " 第" + step.getStepnum() + "步，将测试结果【" + result + "】赋值给全局变量【" + expect.substring(ASSIGNMENT_GLOBALSIGN.length()) + "】");
                    caselog.caseLogDetail(taskid, testcase.getSign(), "将测试结果【" + result + "】赋值给全局变量【" + expect.substring(ASSIGNMENT_GLOBALSIGN.length()) + "】", "info", String.valueOf(step.getStepnum()), "");
                }
                // WebUI检查模式
                else if (1 == step.getSteptype() && params.get("checkproperty") != null && params.get("checkproperty_value") != null) {
                    String checkproperty = params.get("checkproperty");
                    String checkPropertyValue = params.get("checkproperty_value");

                    WebElement we = isElementExist(driver, checkproperty, checkPropertyValue);
                    if (null != we) {
                        luckyclient.publicclass.LogUtil.APP.info("用例：" + testcase.getSign() + " 第" + step.getStepnum() + "步，在当前页面中找到预期结果中对象。当前步骤执行成功！");
                        caselog.caseLogDetail(taskid, testcase.getSign(), "在当前页面中找到预期结果中对象。当前步骤执行成功！", "info", String.valueOf(step.getStepnum()), "");
                    } else {
                        casenote = "第" + step.getStepnum() + "步，没有在当前页面中找到预期结果中对象。执行失败！";
                        setresult = 1;
                        BaseWebDrive.webScreenShot(driver, imagname);
                        luckyclient.publicclass.LogUtil.APP.error("用例：" + testcase.getSign() + " 第" + step.getStepnum() + "步，没有在当前页面中找到预期结果中对象。当前步骤执行失败！");
                        caselog.caseLogDetail(taskid, testcase.getSign(), "在当前页面中没有找到预期结果中对象。当前步骤执行失败！" + "checkproperty【" + checkproperty + "】  checkproperty_value【" + checkPropertyValue + "】", "error", String.valueOf(step.getStepnum()), imagname);
                    }
                }
                // 其它匹配模式
                else {
                    // 模糊匹配预期结果模式
                    if (expect.length() > FUZZY_MATCHING_SIGN.length() && expect.startsWith(FUZZY_MATCHING_SIGN)) {
                        if (result.contains(expect.substring(FUZZY_MATCHING_SIGN.length()))) {
                            luckyclient.publicclass.LogUtil.APP.info("用例：" + testcase.getSign() + " 第" + step.getStepnum() + "步，模糊匹配预期结果成功！执行结果：" + result);
                            caselog.caseLogDetail(taskid, testcase.getSign(), "模糊匹配预期结果成功！执行结果：" + result, "info", String.valueOf(step.getStepnum()), "");
                        } else {
                            casenote = "第" + step.getStepnum() + "步，模糊匹配预期结果失败！";
                            setresult = 1;
                            BaseWebDrive.webScreenShot(driver, imagname);
                            luckyclient.publicclass.LogUtil.APP.error("用例：" + testcase.getSign() + " 第" + step.getStepnum() + "步，模糊匹配预期结果失败！预期结果：" + expect.substring(FUZZY_MATCHING_SIGN.length()) + "，测试结果：" + result);
                            caselog.caseLogDetail(taskid, testcase.getSign(), "模糊匹配预期结果失败！预期结果：" + expect.substring(FUZZY_MATCHING_SIGN.length()) + "，测试结果：" + result, "error", String.valueOf(step.getStepnum()), imagname);
                        }
                    }
                    // 正则匹配预期结果模式
                    else if (expect.length() > REGULAR_MATCHING_SIGN.length() && expect.startsWith(REGULAR_MATCHING_SIGN)) {
                        Pattern pattern = Pattern.compile(expect.substring(REGULAR_MATCHING_SIGN.length()));
                        Matcher matcher = pattern.matcher(result);
                        if (matcher.find()) {
                            luckyclient.publicclass.LogUtil.APP.info("用例：" + testcase.getSign() + " 第" + step.getStepnum() + "步，正则匹配预期结果成功！执行结果：" + result);
                            caselog.caseLogDetail(taskid, testcase.getSign(), "正则匹配预期结果成功！", "info", String.valueOf(step.getStepnum()), "");
                        } else {
                            casenote = "第" + step.getStepnum() + "步，正则匹配预期结果失败！";
                            setresult = 1;
                            BaseWebDrive.webScreenShot(driver, imagname);
                            luckyclient.publicclass.LogUtil.APP.error("用例：" + testcase.getSign() + " 第" + step.getStepnum() + "步，正则匹配预期结果失败！预期结果：" + expect.substring(REGULAR_MATCHING_SIGN.length()) + "，测试结果：" + result);
                            caselog.caseLogDetail(taskid, testcase.getSign(), "正则匹配预期结果失败！预期结果：" + expect.substring(REGULAR_MATCHING_SIGN.length()) + "，测试结果：" + result, "error", String.valueOf(step.getStepnum()), imagname);
                        }
                    }
                    // 精确匹配预期结果模式
                    else {
                        if (expect.equals(result)) {
                            luckyclient.publicclass.LogUtil.APP.info("用例：" + testcase.getSign() + " 第" + step.getStepnum() + "步，精确匹配预期结果成功！执行结果：" + result);
                            caselog.caseLogDetail(taskid, testcase.getSign(), "精确匹配预期结果成功！", "info", String.valueOf(step.getStepnum()), "");
                        } else {
                            casenote = "第" + step.getStepnum() + "步，精确匹配预期结果失败！";
                            setresult = 1;
                            BaseWebDrive.webScreenShot(driver, imagname);
                            luckyclient.publicclass.LogUtil.APP.error("用例：" + testcase.getSign() + " 第" + step.getStepnum() + "步，精确匹配预期结果失败！预期结果是：【"+expect+"】  执行结果：【"+ result+"】");
                            caselog.caseLogDetail(taskid, testcase.getSign(), "精确匹配预期结果失败！预期结果是：【"+expect+"】  执行结果：【"+ result+"】", "error", String.valueOf(step.getStepnum()), imagname);
                        }
                    }
                }
            }
        } else {
            casenote = (null != result) ? result : "";
            setresult = 2;
            BaseWebDrive.webScreenShot(driver, imagname);
            LogUtil.APP.error("用例：" + testcase.getSign() + " 第" + step.getStepnum() + "步，执行结果：" + casenote);
            caselog.caseLogDetail(taskid, testcase.getSign(), "当前步骤在执行过程中解析|定位元素|操作对象失败！" + casenote, "error", String.valueOf(step.getStepnum()), imagname);
        }
        
        return setresult;
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
    }

}