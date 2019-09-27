This modules has few utility classes which help in faster development.
**1. Cache:** 
    This is simple in memory cache which can be stored to cache any configuration data which needs to be refreshed in some time.
    modules also takes care of keeping caches in sync in case of clustered environments with multiple hosts running the app. Rabbitmq or any other amqp can be used for this.
```java
    @Component
    public class PhotoConfigCache extends Cache<PhotoConfig> {
    
    	private static final String PHOTO_CONFIG_CAHCE = "PhotoConfigCache";
    
    	private static final Logger logger = LoggerFactory.getLogger(PhotoConfigCache.class);
    
    	@Resource(name = "photoConfigDaoImpl")
    	private PhotoConfigDao photoConfigDao;
    
    	public PhotoConfigCache() {
    		super(PHOTO_CONFIG_CAHCE, "key");
    	}
    
    	@Override
    	protected PhotoConfig getValueForCache(String key) {
    		PhotoConfig config = null;
    		long t = currentTimeMillis();
    
    		try {
    			config = photoConfigDao.fetchPhotoConfig(key);
    			logger.info("Fetched photo config for key {}", key);
    		}
    		catch (EmptyResultDataAccessException e) {
    			logger.info("No photo config for key {}", key);
    		}
    		catch (Exception e) {
    			logger.error("Error occurred while fetching photo config for key {}", key, e);
    		}
    		finally {
    			logger.debug("time taken : {} to fetch photo config for key: {}", currentTimeMillis() - t, key);
    		}
    
    		if (config == null) {
    			config = new PhotoConfig();
    			config.setKey(key);
    		}
    
    		return config;
    	}
    }

```   
   
**2. CassandraTemplateImpl:**
    This provides a base class for cassandra table crud operations.
    e.g.
    
```java
    @Component
    public class ImportFilteringConfigDaoImpl extends AbstractDao<ObjectToBeReturned> implements ImportFilteringConfigDao {
    
        public static final String TABLE_NAME = "tableName";
    
        public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS %s.tableName (source text, filtering_json text, PRIMARY KEY (source))";
    
        public static final String FIND_QUERY = "SELECT filtering_json from %s.tableName where source=?";
    
        public static final String UPSERT_QUERY = "INSERT INTO %s.tableName (source, filtering_json) VALUES (? ,?)";
    
        public static final String DELETE_QUERY = "DELETE from %s.tableName where source=?";
    
        @Override
        public void upsert(ObjectToBeReturned criteria) throws DataStorageException {
            try {
                String criteriaJson = jacksonJsonObjectMapper.writeValueAsString(criteria);
                cassandraTemplate.upsert(UPSERT_QUERY, CassandraTemplate.WRITE_CONSISTENCY_LEVEL, criteria.getSource(), criteriaJson);
            } catch (JsonProcessingException e) {
                throw new DataStorageException("Error while converting ImportFilteringCriteria into JSON ", e);
            }
        }
    
        @Override
        public ObjectToBeReturned fetch(String source) throws DataStorageException {
            return cassandraTemplate.fetch(FIND_QUERY, CassandraTemplate.READ_CONSISTENCY_LEVEL, ImportFilteringConfigs.class, source);
        }
    
        @Override
        public void delete(String source) throws DataStorageException {
            cassandraTemplate.delete(DELETE_QUERY, CassandraTemplate.DELETE_CONSISTENCY_LEVEL, source);
        }
    
        @Override
        public String getCreateTableQuery() {
            return CREATE_TABLE;
        }
    
        @Override
        public String getTableName() {
            return TABLE_NAME;
        }
    
    }
```
     