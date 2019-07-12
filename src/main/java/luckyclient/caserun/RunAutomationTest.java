package luckyclient.caserun;

import java.io.File;
import java.util.Properties;

import feign.Feign;
import feign.Retryer;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import luckyclient.planapi.entity.TestJobs;
import luckyclient.publicclass.LogUtil;
import org.apache.log4j.PropertyConfigurator;

import luckyclient.caserun.exappium.AppTestControl;
import luckyclient.caserun.exinterface.TestControl;
import luckyclient.caserun.exwebdriver.WebTestControl;
import luckyclient.planapi.api.GetServerAPI;
import luckyclient.planapi.entity.TestTaskexcute;
import springboot.ScriptServer;

/**
 * =================================================================
 * ����һ�������Ƶ�������������������κ�δ�������ǰ���¶Գ����������޸ĺ�������ҵ��;��Ҳ������Գ�������޸ĺ����κ���ʽ�κ�Ŀ�ĵ��ٷ�����
 * Ϊ���������ߵ��Ͷ��ɹ���LuckyFrame�ؼ���Ȩ��Ϣ�Ͻ��۸� ���κ����ʻ�ӭ��ϵ�������ۡ� QQ:1573584944 seagull1985
 * =================================================================
 * 
 * @author�� seagull
 * 
 * @date 2017��12��1�� ����9:29:40
 * 
 */
public class RunAutomationTest extends TestControl {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
//            PropertyConfigurator.configure(System.getProperty("user.dir") + "\\log4j.properties");
            Properties properties= luckyclient.publicclass.SysConfig.getConfiguration();
			String taskid = args[0];
			TestTaskexcute task = GetServerAPI.cgetTaskbyid(Integer.valueOf(taskid));
//            System.out.println("client ��ѯ���� task ��Ϣ****" + task.toString());
//            System.out.println("client ��ѯ���� job ��Ϣ****" + task.getTestJob().toString());
            LogUtil.APP.info("Task Type is:" + task.getTestJob().getExtype());
            int taskType = task.getTestJob().getExtype();
            switch (taskType) {
                case 0:
                    TestControl.taskExecutionPlan(taskid, task);
                    break;
                case 1:
                    WebTestControl.taskExecutionPlan(taskid, task);
                    break;
                case 2:
                    AppTestControl.taskExecutionPlan(taskid, task);
                    break;
                case 3:
                    ScriptServer scriptServer = Feign.builder()
                            .retryer(Retryer.NEVER_RETRY)
                            .encoder(new JacksonEncoder())
                            .decoder(new JacksonDecoder())
                            .target(ScriptServer.class, "http://" + task.getTestJob().getBaseUrl());
                    scriptServer.runTask(task);
                    break;
                case 4:
                    try {
                        Class  clzz = Class.forName(properties.getProperty("CustomScriptFactory"));
                        CustomControlFactory factory = (CustomControlFactory) clzz.newInstance();
                        if(factory == null) {
                            LogUtil.ERROR.error("factory not exitsts!");
                            return;
                        }
                        factory.runControl(task);
                    } catch (ClassNotFoundException e) {
                        LogUtil.ERROR.error(e);
                    }
                    break;
                default:
                    break;
            }
	 		System.exit(0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
