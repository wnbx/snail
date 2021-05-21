package com.acgist.snail.context.initializer;

import com.acgist.snail.context.EntityContext;

/**
 * <p>实体初始化器</p>
 * 
 * @author acgist
 */
public final class EntityInitializer extends Initializer {

	private EntityInitializer() {
		super("实体");
	}
	
	public static final EntityInitializer newInstance() {
		return new EntityInitializer();
	}
	
	@Override
	protected void init() {
		EntityContext.getInstance().load();
	}
	
}
