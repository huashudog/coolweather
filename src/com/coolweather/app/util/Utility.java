package com.coolweather.app.util;

import android.text.TextUtils;

import com.coolweather.app.model.City;
import com.coolweather.app.model.CoolWeatherDB;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;

public class Utility {
	//处理格式为code|name,code|name省份数据
    public synchronized static boolean handleProvinceResponse(
    		CoolWeatherDB coolWeatherDB,String response){
    	if(!TextUtils.isEmpty(response)){
    		String[] allProvince= response.split(",");
    		if(allProvince != null && allProvince.length > 0 ){
    			for(String p : allProvince){
    				String[] array = p .split("\\|");
    				Province province = new Province();
    				province.setProvinceCode(array[0]);
    				province.setProvinceName(array[1]);
    				//save
    				coolWeatherDB.saveProvince(province);
    			}
    			return true;
    		}
    	}
    	
				return false;
    }
  //处理格式为code|name,code|name市级数据
    public synchronized static boolean handleCitiesResponse(
    		CoolWeatherDB coolWeatherDB,String response,int provinceId){
    	if(!TextUtils.isEmpty(response)){
    		String[] allCities= response.split(",");
    		if(allCities != null && allCities.length > 0 ){
    			for(String c : allCities){
    				String[] array = c .split("\\|");
    				City city = new City();
    				city.setCityCode(array[0]);
    				city.setCityName(array[1]);
    				city.setProvinceId(provinceId);
    				//save
    				coolWeatherDB.saveCity(city);
    			}
    			return true;
    		}
    	}
    	
				return false;
    }
    
  //处理格式为code|name,code|name县级数据
    public synchronized static boolean handleCountiesResponse(
    		CoolWeatherDB coolWeatherDB,String response,int cityId){
    	if(!TextUtils.isEmpty(response)){
    		String[] allCounties= response.split(",");
    		if(allCounties != null && allCounties.length > 0 ){
    			for(String c : allCounties){
    				String[] array = c .split("\\|");
    				County county = new County();
    				county.setCountyCode(array[0]);
    				county.setCountyName(array[1]);
    				county.setCityId(cityId);
    				//save
    				coolWeatherDB.saveCounty(county);
    			}
    			return true;
    		}
    	}
				return false;
    }
}
