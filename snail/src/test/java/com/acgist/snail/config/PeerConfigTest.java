package com.acgist.snail.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class PeerConfigTest extends Performance {

	@Test
	void testReserved() {
		int value = PeerConfig.RESERVED_DHT_PROTOCOL + PeerConfig.RESERVED_PEER_EXCHANGE + PeerConfig.RESERVED_FAST_PROTOCOL;
		this.log(PeerConfig.RESERVED);
		assertEquals(value, PeerConfig.RESERVED[7]);
		assertEquals(PeerConfig.RESERVED_EXTENSION_PROTOCOL, PeerConfig.RESERVED[5]);
		value += PeerConfig.RESERVED_NAT_TRAVERSAL;
		PeerConfig.nat();
		this.log(PeerConfig.RESERVED);
		assertEquals(value, PeerConfig.RESERVED[7]);
	}
	
	@Test
	void testPeerId() {
		this.log(PeerConfig.getInstance().peerId());
		this.log(PeerConfig.getInstance().peerIdUrl());
		assertEquals(20, PeerConfig.getInstance().peerId().length);
	}
	
	@Test
	void testClientName() {
		String name = PeerConfig.clientName("-A~1000-xxxxxxxxxxxxxxxx".getBytes());
		assertEquals("Ares", name);
		name = PeerConfig.clientName("-XL1000-xxxxxxxxxxxxxxxx".getBytes());
		assertEquals("Xunlei", name);
		name = PeerConfig.clientName("-AS1000-xxxxxxxxxxxxxxxx".getBytes());
		assertEquals("Acgist Snail", name);
		name = PeerConfig.clientName("S1000-----xxxxxxxxxxxxxxxx".getBytes());
		assertEquals("Shadow's client", name);
		name = PeerConfig.clientName("".getBytes());
		assertEquals("unknown", name);
		name = PeerConfig.clientName(null);
		assertEquals("unknown", name);
	}
	
	@Test
	void testCheckPiece() {
		assertTrue(PeerConfig.checkPiece(0));
		assertFalse(PeerConfig.checkPiece(-1));
		assertFalse(PeerConfig.checkPiece(10000000));
	}
	
	@Test
	void testEnum() {
		assertEquals(PeerConfig.Type.ALLOWED_FAST, PeerConfig.Type.of((byte) 0x11));
		assertNull(PeerConfig.Type.of((byte) 0xFF));
	}
	
}
