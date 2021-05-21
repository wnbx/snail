package com.acgist.snail.net.stun;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

class StunClientTest extends Performance {

	@Test
	void testMappedAddress() {
//		final StunClient client = StunClient.newInstance("stun.l.google.com", 19302);
		final StunClient client = StunClient.newInstance("stun1.l.google.com", 19302);
//		final StunClient client = StunClient.newInstance("stun2.l.google.com", 19302);
//		final StunClient client = StunClient.newInstance("stun3.l.google.com", 19302);
//		final StunClient client = StunClient.newInstance("stun4.l.google.com", 19302);
//		final StunClient client = StunClient.newInstance("stun.xten.com");
//		final StunClient client = StunClient.newInstance("stunserver.org");
//		final StunClient client = StunClient.newInstance("numb.viagenie.ca");
//		final StunClient client = StunClient.newInstance("stun.softjoys.com");
		assertNull(SystemConfig.getExternalIPAddress());
		client.mappedAddress();
		int index = 0;
		while(index++ < 5 && SystemConfig.getExternalIPAddress() == null) {
			ThreadUtils.sleep(1000);
		}
		assertNotNull(SystemConfig.getExternalIPAddress());
	}
	
}
