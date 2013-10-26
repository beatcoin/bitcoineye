package org.beatcoin;


import javax.servlet.ServletContext;

import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.guice.web.ShiroWebModule;
import org.apache.shiro.realm.AuthorizingRealm;


public class BitcoinIShiroWebModule extends ShiroWebModule {

	public BitcoinIShiroWebModule(ServletContext servletContext) {
		super(servletContext);
	}

	@Override
	protected void configureShiroWeb() {
		bindRealm().to(AuthorizingRealm.class).asEagerSingleton();
		bind(Authenticator.class).toInstance(new ModularRealmAuthenticator());
	}
	
}
