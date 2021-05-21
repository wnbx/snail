package com.acgist.snail.net.upnp;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.upnp.UpnpService.Status;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.utils.Performance;

class UpnpServiceTest extends Performance {

	private static final String SERVICEURL = "http://192.168.1.1:10087/rootDesc.xml";
//	private static final String SERVICEURL = "http://192.168.1.1:5351/rootDesc.xml";
	
	@BeforeAll
	static final void load() throws NetException {
		UpnpService.getInstance().load(SERVICEURL);
	}
	
	@Test
	void testGetExternalIPAddress() throws NetException {
		final String ip = UpnpService.getInstance().getExternalIPAddress();
		this.log(ip);
		assertNotNull(ip);
	}

	@Test
	void testGetSpecificPortMappingEntry() throws NetException {
		final var tcp = UpnpService.getInstance().getSpecificPortMappingEntry(18888, Protocol.Type.TCP);
		final var udp = UpnpService.getInstance().getSpecificPortMappingEntry(18888, Protocol.Type.UDP);
		this.log(tcp);
		this.log(udp);
		assertNotEquals(Status.UNINIT, tcp);
		assertNotEquals(Status.UNINIT, udp);
	}

	@Test
	void testAddPortMapping() throws NetException {
		assertTrue(UpnpService.getInstance().addPortMapping(18888, 18888, Protocol.Type.TCP));
		assertTrue(UpnpService.getInstance().addPortMapping(18888, 18888, Protocol.Type.UDP));
	}

	@Test
	void testDeletePortMapping() throws NetException {
		assertTrue(UpnpService.getInstance().deletePortMapping(18888, Protocol.Type.TCP));
		assertTrue(UpnpService.getInstance().deletePortMapping(18888, Protocol.Type.UDP));
	}
	
}
