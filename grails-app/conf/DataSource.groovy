dataSource {
    pooled = true
    //driverClassName = "oracle.jdbc.driver.OracleDriver"
    //username = "rforce_app_sec"
    //password = "rforce"
    driverClassName = "org.h2.Driver"
    dbCreate = "create-drop"
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = false
    cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory'
    //dialect = 'org.hibernate.dialect.Oracle10gDialect'
}
// environment specific settings
environments {
    development {

        dataSource {
            //dbCreate = "create-drop" // one of 'create', 'create-drop', 'update', 'validate', ''
            dbCreate = "update"
            //url = "jdbc:oracle:thin:@localhost:1522:XE"
            url = "jdbc:h2:mem:devDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
        }
    }
    test {
        dataSource {
            //url = "jdbc:oracle:thin:@localhost:1522:XE"
            url = "jdbc:h2:mem:devDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
        }
    }
    production {
        dataSource {
            //url = "jdbc:oracle:thin:@localhost:1522:XE"
            url = "jdbc:h2:mem:devDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
        }
    }
}
