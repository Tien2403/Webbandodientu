package com.shopme.web.setting;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.shopme.common.entity.Currency;
import com.shopme.common.entity.setting.Setting;
import com.shopme.common.entity.setting.SettingCategory;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettingService {
	
	private final SettingRepository settingRepository;
	private final CurrencyRepository currencyRepository;
	private final String SETTING_CACHE_NAME = "settingCache";

	@Transactional(readOnly = true)
	@Cacheable(value = SETTING_CACHE_NAME, key = "'generalSettings'")
	public List<Setting> getGeneralSettings() {
		return settingRepository.findByCategoryIn(List.of(SettingCategory.GENERAL, SettingCategory.CURRENCY));
	}
	
	@Transactional(readOnly = true)
	public EmailSettingBag getEmailSettings() {
		List<Setting> settings = settingRepository.findByCategory(SettingCategory.MAIL_SERVER);
		settings.addAll(settingRepository.findByCategory(SettingCategory.MAIL_TEMPLATES));
		
		return new EmailSettingBag(settings);
	}
	
	@Transactional(readOnly = true)
	public CurrencySettingBag getCurrencySettings() {
		List<Setting> settings = settingRepository.findByCategory(SettingCategory.CURRENCY);
		return new CurrencySettingBag(settings);
	}
	
	@Transactional(readOnly = true)
	public PaymentSettingBag getPaymentSettings() {
		List<Setting> settings = settingRepository.findByCategory(SettingCategory.PAYMENT);
		return new PaymentSettingBag(settings);
	}
	
	@Transactional(readOnly = true)
	public String getCurrencyCode() {
		Setting setting = settingRepository.findByKey("CURRENCY_ID");
		Integer currencyId = Integer.parseInt(setting.getValue());
		Currency currency = currencyRepository.findById(currencyId).get();
		
		return currency.getCode();
	}
}
