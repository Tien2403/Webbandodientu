package com.shopme.web.setting;

import static com.shopme.common.utils.UrlUtils.isResourceURL;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.shopme.common.entity.setting.Setting;

@Component
@Order(-123)
@Slf4j
@RequiredArgsConstructor
public class SettingFilter implements Filter {
	
	private final SettingService settingService;
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletRequest servletRequest = (HttpServletRequest) request;
		String url = servletRequest.getRequestURL().toString();
		
		if (isResourceURL(url)) {
			chain.doFilter(request, response);
			return;
		}
		
		List<Setting> generalSettings = settingService.getGeneralSettings();
		
		generalSettings.forEach(setting -> {
			request.setAttribute(setting.getKey(), setting.getValue());
		});
		
		chain.doFilter(request, response);

	}

}
