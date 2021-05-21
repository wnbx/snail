package com.acgist.snail.context.recycle;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.StringUtils;

class WindowsRecycleTest extends Performance {

	@Test
	void testDelete() throws IOException {
		final String path = "E:/snail/tmp/" + System.currentTimeMillis() + ".acgist";
		FileUtils.write(path, "acgist".repeat(1024).getBytes());
		final File file = new File(path);
		file.createNewFile();
		assertTrue(file.exists());
		new WindowsRecycle(path).delete();
		assertFalse(file.exists());
	}
	
	@Test
	void testFileInfo() throws IOException {
		if(SKIP_COSTED) {
			this.log("跳过testRecycle测试");
			return;
		}
		final var bytes = Files.readAllBytes(Paths.get("E:/$RECYCLE.BIN/S-1-5-21-1082702080-4186364021-1016170526-1001/$I80331708.zip"));
		this.log(StringUtils.hex(bytes));
		assertNotNull(bytes);
	}
	
}
