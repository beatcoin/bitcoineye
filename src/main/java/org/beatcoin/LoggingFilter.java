package org.beatcoin;

import java.io.IOException;

import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.http.HttpRequest;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class LoggingFilter implements Filter {
	public static Logger log = LoggerFactory.getLogger(LoggingFilter.class);

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1,
			FilterChain arg2) throws IOException, ServletException {
		if (arg0 instanceof ShiroHttpServletRequest){
			ShiroHttpServletRequest req = (ShiroHttpServletRequest)arg0;
			log.info(req.getRequestURI());
			log.info(req.getContentType());
		}
		if (arg0 instanceof HttpRequest){
			HttpRequest req = (HttpRequest)arg0;
			log.info(req.getRequestLine().toString());
			log.info(req.toString());			
		}
		arg2.doFilter(arg0, arg1);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub

	}

}
