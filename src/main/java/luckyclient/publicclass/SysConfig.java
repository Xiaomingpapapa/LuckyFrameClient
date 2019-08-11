package luckyclient.publicclass;

import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.util.Properties;

/**
 * ϵͳ���ò���
 * @author seagull
 *
 */
public class SysConfig {
	private static final Properties SYS_CONFIG = new Properties();
	private static final String SYS_CONFIG_FILE = "sys_config.properties";
	static{
		try {
			SYS_CONFIG.load(new InputStreamReader(new ClassPathResource(SYS_CONFIG_FILE).getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private SysConfig(){}
	public static Properties getConfiguration(){
		return SYS_CONFIG;
	}
}