package org.beatcoin;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;

import javax.servlet.ServletContextEvent;

import org.apache.shiro.guice.web.GuiceShiroFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.bcJsonRpc.BitcoindClientFactory;
import com._37coins.bcJsonRpc.BitcoindInterface;
import com._37coins.bcJsonRpc.events.WalletListener;
import com._37coins.bcJsonRpc.pojo.Transaction;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

public class BitcoinIServletConfig extends GuiceServletContextListener {
	public static String domainName;
	public static URL bcdUrl;
	public static String bcdUser;
	public static String bcdPassword;
	public static Logger log = LoggerFactory.getLogger(BitcoinIServletConfig.class);
	public static Injector injector;
	static {
		domainName = System.getProperty("swfDomain");
		try {
			bcdUrl = new URL(System.getProperty("url"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		bcdUser = System.getProperty("user");
		bcdPassword = System.getProperty("password");
	}
	private WalletListener listener;

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		super.contextInitialized(servletContextEvent);
		final Injector i = getInjector();
		BitcoindInterface client = i.getInstance(BitcoindInterface.class);
		
		try {
			listener = new WalletListener(client);
			listener.addObserver(new Observer() {
				@Override
				public void update(Observable o, Object arg) {
					Transaction t = (Transaction)arg;
					t.getAccount();
				}
			});
			
			log.info("ServletContextListener started");
		} catch (IOException e) {
			e.printStackTrace();
		}
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

			@Override
			public void configureServlets() {
            	filter("/*").through(GuiceShiroFilter.class);
			}

		});
		return injector;
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		super.contextDestroyed(sce);
		listener.stop();
		log.info("ServletContextListener destroyed");
	}

}
