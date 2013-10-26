package org.beatcoin;


import javax.servlet.ServletContext;

import org.apache.shiro.config.Ini;
import org.apache.shiro.guice.web.ShiroWebModule;
import org.apache.shiro.realm.text.IniRealm;

import com.google.inject.Provides;


public class BitcoinIShiroWebModule extends ShiroWebModule {

	public BitcoinIShiroWebModule(ServletContext servletContext) {
		super(servletContext);
	}

	@Override
	protected void configureShiroWeb() {
		try {
            bindRealm().toConstructor(IniRealm.class.getConstructor(Ini.class));
            //addFilterChain("/wallets/**", AUTHC_BASIC, config(PERMS, "yes"));
        } catch (NoSuchMethodException e) {
            addError(e);
        }
		
	}
	@Provides
    Ini loadShiroIni() {
        return Ini.fromResourcePath("classpath:shiro.ini");
    }
	
}
