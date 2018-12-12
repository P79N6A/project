package com.yunxiao.base.yunxiaospringbootstarter.util;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.log.NullLogChute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xinyu
 * @date 2017-11-27
 */
public class TemplateUtils {
	private static final Logger logger = LoggerFactory.getLogger(TemplateUtils.class);
	private static VelocityEngine velocityEngine = new VelocityEngine();

	static {
		Properties prop = new Properties();
		prop.setProperty(Velocity.ENCODING_DEFAULT, "UTF-8");
		prop.setProperty(Velocity.INPUT_ENCODING, "UTF-8");
		prop.setProperty(Velocity.OUTPUT_ENCODING, "UTF-8");
		prop.setProperty("file.resource.loader.class", MyClassLoader.class.getName());
		prop.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS, new NullLogChute().getClass().getName());
		try {
			velocityEngine.init(prop);
		} catch (Exception e) {
			logger.warn("init velocity engine with props failed, exception = {}", e);
		}
		try {
			velocityEngine.init();
		} catch (Exception e) {
			logger.warn("init velocity engine with no props failed, exception = {}", e);
		}
	}

	/**
	 * 渲染指定文件路径模板
	 * 
	 * @param templatePath
	 * @param context
	 * @return
	 */
	public static String renderFileTtemplate(String templatePath, VelocityContext context) {
		Template template;
		System.out.println("templatePath: "+templatePath);
		try {
			template = velocityEngine.getTemplate(templatePath);
			StringWriter writer = new StringWriter();
			template.merge(context, writer);
			return writer.toString();
		} catch (Exception e) {
			logger.warn("render file template failed, exception = {}", e);
		}
		return "";
	}

	/**
	 * 渲染文本模板
	 * 
	 * @param templateContent
	 * @param context
	 * @return
	 */
	public static String renderStringTtemplate(String templateContent, VelocityContext context) {
		try {
			StringWriter writer = new StringWriter();
			velocityEngine.evaluate(context, writer, "", templateContent);
			return writer.toString();
		} catch (Exception e) {
			logger.warn("render string template failed, exception = {}", e);
		}
		return "";
	}

	public static class MyClassLoader extends org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader {
		@Override
		public InputStream getResourceStream(String source) throws ResourceNotFoundException {
			return MyClassLoader.class.getResourceAsStream(source);
		}
	}
}
