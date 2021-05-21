package com.acgist.snail.pojo.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.net.upnp.UpnpServer;
import com.acgist.snail.utils.Performance;

class HeaderWrapperTest extends Performance {

	@Test
	void testHeaderWrapper() {
		final StringBuilder builder = new StringBuilder();
		builder
			.append("M-SEARCH * HTTP/1.1").append(SymbolConfig.LINE_SEPARATOR_COMPAT)
			.append("HOST: ").append(UpnpServer.upnpHost()).append(":").append(UpnpServer.UPNP_PORT).append(SymbolConfig.LINE_SEPARATOR_COMPAT)
			.append("MX: 3").append(SymbolConfig.LINE_SEPARATOR_COMPAT)
			.append("ST: urn:schemas-upnp-org:device:InternetGatewayDevice:1").append(SymbolConfig.LINE_SEPARATOR_COMPAT)
			.append("MAN: \"ssdp:discover\"").append(SymbolConfig.LINE_SEPARATOR_COMPAT);
		var wrapper = HeaderWrapper.newBuilder("M-SEARCH * HTTP/1.1");
		wrapper
			.header("HOST", UpnpServer.upnpHost() + ":" + UpnpServer.UPNP_PORT)
			.header("MX", "3")
			.header("ST", "urn:schemas-upnp-org:device:InternetGatewayDevice:1")
			.header("MAN", "\"ssdp:discover\"");
		assertEquals(builder.toString(), wrapper.build());
		wrapper = HeaderWrapper.newInstance(wrapper.build());
		assertEquals(builder.toString(), wrapper.build());
		assertNull(wrapper.header("acgist"));
		assertEquals("3", wrapper.header("Mx"));
		Map<String, List<String>> data = new HashMap<String, List<String>>(Map.of(
			"b", List.of(),
			"c", List.of("1"),
			"d", List.of("1", "2")
		));
		data.put("a", null);
		wrapper = HeaderWrapper.newBuilder("=", "$", "acgist", data);
		this.log(wrapper.build());
	}
	
	@Test
	void testDecode() {
		final var wrapper = HeaderWrapper.newInstance("acgist\na: b\n c : d\ne\nf:");
		this.log(wrapper.allHeaders());
		assertEquals("b", wrapper.header("a"));
		assertEquals("d", wrapper.header("c"));
		assertEquals("", wrapper.header("e"));
		assertEquals("", wrapper.header("f"));
	}
	
}
