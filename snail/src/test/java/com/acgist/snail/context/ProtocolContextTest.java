package com.acgist.snail.context;

import org.junit.jupiter.api.Test;

import com.acgist.snail.Snail.SnailBuilder;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.utils.Base32Utils;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.StringUtils;

class ProtocolContextTest extends Performance {

	@Test
	void testSupport() {
		SnailBuilder.newBuilder().enableAllProtocol().buildSync();
		var result = ProtocolContext.getInstance().support("https://www.acgist.com");
		this.log(result);
		result = ProtocolContext.getInstance().support("641000d9be79ad8947701c338c06211ba69e1b09");
		this.log(result);
		result = ProtocolContext.getInstance().support(Base32Utils.encode(StringUtils.unhex("641000d9be79ad8947701c338c06211ba69e1b09")));
		this.log(result);
		result = ProtocolContext.getInstance().support("thunder://QUFodHRwOi8vdXBvcy1oei1taXJyb3Jic3l1LmFjZ3ZpZGVvLmNvbS91cGdjeGNvZGUvMjIvNjkvMTI0NDY5MjIvMTI0NDY5MjItMS02NC5mbHY/ZT1pZzhldXhaTTJyTmNOYmhIaGJVVmhvTXpuV05CaHdkRXRvOGc1WDEwdWdOY1hCQl8mZGVhZGxpbmU9MTU2MTAyMTI1NCZnZW49cGxheXVybCZuYnM9MSZvaT0xNzAzMTc4Nzk0Jm9zPWJzeXUmcGxhdGZvcm09aHRtbDUmdHJpZD1kZWIzMTdkMjI0NDc0ZDg5YWI4YmI1ZDgzNWMzMGY3MyZ1aXBrPTUmdXBzaWc9YWY3NmExOTUyYjFlNjZhYmQ0NzBiNmRmOWYyNTA2MWImdXBhcmFtcz1lLGRlYWRsaW5lLGdlbixuYnMsb2ksb3MscGxhdGZvcm0sdHJpZCx1aXBrJm1pZD00NTU5MjY3Wlo==");
		this.log(result);
		result = ProtocolContext.getInstance().support("e:/snail/868f1199b18d05bf103aa8a8321f6428854d712e.torrent");
		this.log(result);
	}
	
	@Test
	void testBuildTaskSession() throws DownloadException {
		SnailBuilder.newBuilder().enableAllProtocol().buildSync();
		ITaskSession result;
//		result = ProtocolContext.getInstance().buildTaskSession("https://www.acgist.com");
//		this.log(result);
//		result = ProtocolContext.getInstance().buildTaskSession("641000d9be79ad8947701c338c06211ba69e1b09");
//		this.log(result);
//		result = ProtocolContext.getInstance().buildTaskSession(Base32Utils.encode(StringUtils.unhex("641000d9be79ad8947701c338c06211ba69e1b09")));
//		this.log(result);
		result = ProtocolContext.getInstance().buildTaskSession("thunder://QUFodHRwOi8vdXBvcy1oei1taXJyb3Jic3l1LmFjZ3ZpZGVvLmNvbS91cGdjeGNvZGUvMjIvNjkvMTI0NDY5MjIvMTI0NDY5MjItMS02NC5mbHY/ZT1pZzhldXhaTTJyTmNOYmhIaGJVVmhvTXpuV05CaHdkRXRvOGc1WDEwdWdOY1hCQl8mZGVhZGxpbmU9MTU2MTAyMTI1NCZnZW49cGxheXVybCZuYnM9MSZvaT0xNzAzMTc4Nzk0Jm9zPWJzeXUmcGxhdGZvcm09aHRtbDUmdHJpZD1kZWIzMTdkMjI0NDc0ZDg5YWI4YmI1ZDgzNWMzMGY3MyZ1aXBrPTUmdXBzaWc9YWY3NmExOTUyYjFlNjZhYmQ0NzBiNmRmOWYyNTA2MWImdXBhcmFtcz1lLGRlYWRsaW5lLGdlbixuYnMsb2ksb3MscGxhdGZvcm0sdHJpZCx1aXBrJm1pZD00NTU5MjY3Wlo==");
		this.log(result);
		result = ProtocolContext.getInstance().buildTaskSession("E:/snail/b3e9dcb123b80078aa5ace79323f925e8f755a6a.torrent");
		this.log(result);
	}
	
}
