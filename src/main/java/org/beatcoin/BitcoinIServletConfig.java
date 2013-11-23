package org.beatcoin;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.PriorityBlockingQueue;

import javax.jdo.PersistenceManagerFactory;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.apache.http.Consts;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.shiro.guice.web.GuiceShiroFilter;
import org.beatcoin.pojo.Notification;
import org.beatcoin.pojo.Payment;
import org.beatcoin.pojo.Song;
import org.beatcoin.pojo.Subscribe;
import org.beatcoin.pool.AddressPool;
import org.beatcoin.pool.PoolInitializer;
import org.restnucleus.PersistenceConfiguration;
import org.restnucleus.filter.PaginationFilter;
import org.restnucleus.filter.PersistenceFilter;
import org.restnucleus.filter.QueryFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.bcJsonRpc.BitcoindClientFactory;
import com._37coins.bcJsonRpc.BitcoindInterface;
import com._37coins.bcJsonRpc.events.WalletListener;
import com._37coins.bcJsonRpc.pojo.Transaction;
import com._37coins.bcJsonRpc.pojo.Transaction.Category;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.mysql.jdbc.AbandonedConnectionCleanupThread;

public class BitcoinIServletConfig extends GuiceServletContextListener {
	public static URL bcdUrl;
	public static String bcdUser;
	public static String bcdPassword;
	public static String notUrl;
	public static String basePath;
	public static String resPath;
	public static Logger log = LoggerFactory.getLogger(BitcoinIServletConfig.class);
	public static Injector injector;
	public static SocketIOServer server;
	public static int poolSize;
	static {
		try {
			bcdUrl = new URL(System.getProperty("bcdUrl"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		bcdUser = System.getProperty("bcdUser");
		bcdPassword = System.getProperty("bcdPassword");
		notUrl = System.getProperty("notUrl");
		basePath = System.getProperty("basePath");
		resPath = System.getProperty("resPath");
		poolSize = (null!=System.getProperty("poolSize"))?Integer.parseInt(System.getProperty("poolSize")):20;
	}
	private WalletListener listener;
	private ServletContext servletContext;

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		servletContext = servletContextEvent.getServletContext();
		super.contextInitialized(servletContextEvent);
		final Injector i = getInjector();
		BitcoindInterface client = i.getInstance(BitcoindInterface.class);
		final Cache cache = i.getInstance(Cache.class);
		try {
			listener = new WalletListener(client);
			listener.addObserver(new Observer() {
				@Override
				public void update(Observable o, Object arg) {
					Transaction tx = ((Transaction)arg);
					//check cache
					if (null == cache.get(tx.getTxid())){
						cache.put(new Element(tx.getTxid(), tx));
					}else{
						System.out.println("dropping transaction: "+tx.getTxid());
						return;
					}
					
					for (Transaction t : tx.getDetails()){
						if (t.getCategory()==Category.RECEIVE){
							Notification n = new Notification().setSubject("payment")
								.setPayment(new Payment()
									.setAddress(t.getAddress())
									.setAmount(t.getAmount())
									.setAccount(t.getAccount())
									.setTime(tx.getTimereceived()));
							//handle transaction
							Element e = cache.get(t.getAccount());
							if (null!=e){
								@SuppressWarnings("unchecked")
								PriorityBlockingQueue<Song> pq = (PriorityBlockingQueue<Song>)e.getObjectValue();
								for (Song song: pq){
									if (song.getAddress().equals(t.getAddress())){
										song.setSum(t.getAmount().add(song.getSum()).setScale(8));
										cache.put(new Element(t.getAccount(),pq));
									}
								}
							}
							//notify if configured
							if (null!=notUrl){
								HttpClient client = HttpClientBuilder.create().build();
								try {
									HttpPost httpPost = new HttpPost(notUrl);
									StringEntity entity = new StringEntity(new ObjectMapper().writeValueAsString(n), Consts.UTF_8);
									entity.setContentType("application/json");
									httpPost.setEntity(entity);
									client.execute(httpPost);
								} catch (IOException ex) {
									ex.printStackTrace();
								}					
							}
						}
					}
				}
			});
			
			log.info("ServletContextListener started");
		} catch (IOException e) {
			e.printStackTrace();
		}
		i.getInstance(AddressPool.class);
		server = i.getInstance(SocketIOServer.class);
		server.addJsonObjectListener(Subscribe.class, new DataListener<Subscribe>() {
	        @Override
	        public void onData(SocketIOClient client, Subscribe data, AckRequest ackRequest) {
	        	client.joinRoom(data.getRoom());
	        }
	    });
		server.start();
	}

	@Override
	protected Injector getInjector() {
		injector = Guice.createInjector(new ServletModule() {
			
			@Provides @Singleton @SuppressWarnings("unused")
			BitcoindInterface getClient(){
				BitcoindClientFactory bcf = null;
				try {
					bcf = new BitcoindClientFactory(bcdUrl, bcdUser, bcdPassword);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return bcf.getClient();
			}
			
			@Provides @SuppressWarnings("unused")
			public PoolInitializer provideInit(BitcoindInterface client){
				return new PoolInitializer(client);
			}
			
			@Provides @Singleton @SuppressWarnings("unused")
			public AddressPool provideAD(PoolInitializer pi){
				return new AddressPool(pi);
			}
			
			@Provides @Singleton @SuppressWarnings("unused")
			public Directory provideDirectory(){
				Directory directory = new RAMDirectory();
				return directory;
			}
			
			@Provides @Singleton @SuppressWarnings("unused")
			public SocketIOServer provideSocket(){
			 	Configuration config = new Configuration();
			    config.setPort(8081);
			    SocketIOServer server = new SocketIOServer(config);
			    return server;
			}

        	@Provides @Singleton @SuppressWarnings("unused")
        	public Cache provideCache(){
        		//Create a singleton CacheManager using defaults
        		CacheManager manager = CacheManager.create();
        		//Create a Cache specifying its configuration.
        		Cache testCache = new Cache(new CacheConfiguration("cache", 1000)
        		    .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
        		    .eternal(false)
        		    .timeToLiveSeconds(7200)
        		    .timeToIdleSeconds(3600)
        		    .diskExpiryThreadIntervalSeconds(0));
        		  manager.addCache(testCache);
        		  return testCache;
        	}
        	
			@Provides @Singleton @SuppressWarnings("unused")
			PersistenceManagerFactory providePersistence(){
				PersistenceConfiguration pc = new PersistenceConfiguration();
				pc.createEntityManagerFactory();
				return pc.getPersistenceManagerFactory();
			}
        	
			@Override
			public void configureServlets() {
            	filter("/*").through(GuiceShiroFilter.class);
            	filter("/*").through(LoggingFilter.class);
            	filter("/*").through(CorsFilter.class);
            	filter("/*").through(PersistenceFilter.class);
            	filter("/*").through(QueryFilter.class);
            	filter("/*").through(PaginationFilter.class);
			}

		},new BitcoinIShiroWebModule(this.servletContext));
		return injector;
	}
	
    public void deregisterJdbc(){
        // This manually deregisters JDBC driver, which prevents Tomcat 7 from complaining about memory leaks wrto this class
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                log.info(String.format("deregistering jdbc driver: %s", driver));
            } catch (SQLException e) {
            	log.info(String.format("Error deregistering driver %s", driver));
                e.printStackTrace();
            }
        }
        try {
            AbandonedConnectionCleanupThread.shutdown();
        } catch (InterruptedException e) {
            log.warn("SEVERE problem cleaning up: " + e.getMessage());
            e.printStackTrace();
        }    	
    }

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		Injector injector = (Injector) sce.getServletContext().getAttribute(Injector.class.getName());
		injector.getInstance(PersistenceManagerFactory.class).close();
		deregisterJdbc();
		super.contextDestroyed(sce);
		server.stop();
		listener.stop();
		log.info("ServletContextListener destroyed");
	}

}
