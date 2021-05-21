package com.acgist.snail.context.initializer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class LocalServiceDiscoveryInitializerTest extends Performance {

	@Test
	void testLocalServiceDiscoveryInitializer() {
		assertDoesNotThrow(() -> LocalServiceDiscoveryInitializer.newInstance().sync());
	}
	
}
