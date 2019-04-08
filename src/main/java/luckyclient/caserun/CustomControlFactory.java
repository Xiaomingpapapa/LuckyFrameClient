package luckyclient.caserun;

import luckyclient.planapi.entity.TestTaskexcute;

/**
 * 
 * @author 28476
 * 用于外部系统接入自动化测试
 */
public  class CustomControlFactory {
	
	public interface ControlRunnable{
		void run(TestTaskexcute task);
	}
	
	public void runControl(TestTaskexcute task) {
		ControlRunnable runnable = getControlRunnable();
		if (runnable != null) {
			runnable.run(task);
		}
	}
	
	protected ControlRunnable getControlRunnable() {
		return null;
	}
}
