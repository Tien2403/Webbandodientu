package com.shopme.web.setting;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.shopme.common.entity.setting.Setting;
import com.shopme.common.entity.setting.SettingCategory;

public interface SettingRepository extends CrudRepository<Setting, String> {
	List<Setting> findByCategory(SettingCategory category);
	
	List<Setting> findByCategoryIn(List<SettingCategory> categories);
	
	Setting findByKey(String key);
}
