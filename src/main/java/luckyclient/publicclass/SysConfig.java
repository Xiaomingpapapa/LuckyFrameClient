package luckyclient.publicclass;

import java.io.*;
import java.util.Properties;

/**
 * 系统配置参数
 * @author seagull
 *
 */
public class SysConfig {
	private static final Properties SYS_CONFIG = new Properties();
	private static final String SYS_CONFIG_FILE = "sys_config.properties";
	static{
		try {
			SYS_CONFIG.load(new FileReader(System.getProperty("user.dir") + File.separator + SYS_CONFIG_FILE));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private SysConfig(){}
	public static Properties getConfiguration(){
		return SYS_CONFIG;
	}
}