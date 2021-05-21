package com.acgist.snail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.format.XML;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.Performance;

class VerifyTest extends Performance {

	private static final String PROJECT_BASE_PATH = "E:/gitee/snail/";
	
	@Test
	void testVersionVerify() throws IOException {
		final String basePath = PROJECT_BASE_PATH;
		final String parentPomPath = basePath + "pom.xml";
		final String snailPomPath = basePath + "snail/pom.xml";
		final String snailJavaFXPomPath = basePath + "snail-javafx/pom.xml";
		final String systemConfigPath = basePath + "snail/src/main/resources/config/system.properties";
		final String parentPomVersion = xml(parentPomPath, "version");
		this.log("当前版本：{}", parentPomVersion);
		final String snailPomVersion = xml(snailPomPath, "version");
		assertEquals(parentPomVersion, snailPomVersion);
		final String snailJavaFXPomVersion = xml(snailJavaFXPomPath, "version");
		assertEquals(parentPomVersion, snailJavaFXPomVersion);
		final String systemConfigVersion = property(systemConfigPath, "acgist.system.version");
		assertEquals(parentPomVersion, systemConfigVersion);
	}
	
	private String xml(String path, String name) {
		final XML xml = XML.loadFile(path);
		return xml.elementValue(name);
	}
	
	private String property(String path, String name) throws IOException {
		final File file = new File(path);
		final var input = new InputStreamReader(new FileInputStream(file), SystemConfig.DEFAULT_CHARSET);
		final Properties properties = new Properties();
		properties.load(input);
		return properties.getProperty(name);
	}
	
	@Test
	void testFormat() throws IOException {
		format(new File(PROJECT_BASE_PATH));
	}
	
	void format(File file) throws IOException {
		if (file.isFile()) {
			final String name = file.getName();
			if (
				name.endsWith(".properties") ||
				name.endsWith(".java") ||
				name.endsWith(".xml") ||
				name.endsWith(".md")
			) {
				Files.readAllLines(file.toPath()).forEach(line -> {
					if(line.endsWith(" ") && !line.endsWith("* ")) {
						this.log("文件格式错误（空格）：{}-{}", file.getAbsolutePath(), line);
					}
					if(line.endsWith("	") && !line.trim().isEmpty()) {
						this.log("文件格式错误（制表）：{}-{}", file.getAbsolutePath(), line);
					}
				});
			}
		} else {
			var files = file.listFiles();
			for (File children : files) {
				format(children);
			}
		}
	}

	@Test
	void checkFile() {
		final var sources = new File("E:\\snail\\server\\Scans\\Vol.1").listFiles();
		final var targets = new File("E:\\snail\\tmp\\client\\Scans\\Vol.1").listFiles();
		boolean same = true;
		Arrays.sort(sources);
		Arrays.sort(targets);
		for (int index = 0; index < sources.length; index++) {
			final var source = sources[index];
			final var target = targets[index];
			final var sourceHash = FileUtils.sha1(source.getAbsolutePath());
			final var targetHash = FileUtils.sha1(target.getAbsolutePath());
			if(sourceHash.equals(targetHash)) {
				this.log("文件匹配成功：{}-{}", source, target);
			} else {
				this.log("文件匹配失败：{}-{}", source, target);
			}
		}
		assertTrue(same);
	}
	
}
