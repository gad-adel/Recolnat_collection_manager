package org.recolnat.collection.manager.common.config;


import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.dialect.pagination.LimitOffsetLimitHandler;

public class PostgreSQL10DialectUUID extends PostgreSQLDialect {
	
	 @Override
	    public LimitHandler getLimitHandler() {
	        return LimitOffsetLimitHandler.INSTANCE;
	    }

}
