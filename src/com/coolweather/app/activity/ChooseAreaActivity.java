package com.coolweather.app.activity;


import java.util.ArrayList;
import java.util.List;

import com.coolweather.app.R;
import com.coolweather.app.model.City;
import com.coolweather.app.model.CoolWeatherDB;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;
import com.coolweather.app.util.*;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {
    public static final int LEVEL_PROVINCE= 0;
    public static final int LEVEL_CITY= 1;
    public static final int LEVEL_COUNTY= 2;
	private ArrayAdapter<String> adapter;
	private List<String> dataList = new ArrayList<String>();
	private ListView listView;
	private TextView titleText;
	private CoolWeatherDB coolWeatherDB;
	private int currentLevel;
	
	private List<Province> provinceList;
	private List<City> cityList;
	private List<County> countyList;
    
	private Province selectedProvince;
	private City selectedCity;

	private ProgressDialog progressDialog;
    private boolean isFromWeatherActivity;
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (currentLevel == LEVEL_COUNTY){
			queryCities();
		}else if(currentLevel == LEVEL_CITY){
			queryProvinces();
		}else {
			if(isFromWeatherActivity){
				Intent intent= new Intent(this,WeatherActivity.class);
				this.startActivity(intent);
			}
			finish();
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		isFromWeatherActivity= this.getIntent().getBooleanExtra(
				"from_weather_activity", false);
		SharedPreferences prefs= PreferenceManager
				.getDefaultSharedPreferences(this);
		if(prefs.getBoolean("city_selected", false)
				&&(!isFromWeatherActivity)){
			Intent intent= new Intent(this,WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.choose_area);
		
		listView= (ListView) this.findViewById(R.id.list_view);
		titleText= (TextView) this.findViewById(R.id.title_text);
		
		adapter= new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1,dataList);
		listView.setAdapter(adapter);
		coolWeatherDB= CoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index,
					long arg3) {
				// TODO Auto-generated method stub
				if (currentLevel == LEVEL_PROVINCE){
					selectedProvince= provinceList.get(index);
					queryCities();
				} else if (currentLevel == LEVEL_CITY){
					selectedCity= cityList.get(index);
					queryCounties();
				} else if(currentLevel == LEVEL_COUNTY){
					String countyCode= countyList.get(index).getCountyCode();
					Intent intent= new Intent(
							ChooseAreaActivity.this,WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}
			}
		});
		queryProvinces();
		
	}
	private void queryCities() {
		// TODO Auto-generated method stub
		cityList= coolWeatherDB.loadCity(selectedProvince.getId());
		if(cityList.size() > 0){
			dataList.clear();
			for (City city : cityList){
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel= LEVEL_CITY;
		}else{
			queryFromServer(
					selectedProvince.getProvinceCode(),"city");
		}
		
	}
	private void queryCounties() {
		// TODO Auto-generated method stub
		countyList= coolWeatherDB.loadCounty(selectedCity.getId());
		if(countyList.size() > 0){
			dataList.clear();
			for (County county : countyList){
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel= LEVEL_COUNTY;
		}else{
			queryFromServer(
					selectedCity.getCityCode(),"county");
		}
	}
	
	private void queryProvinces() {
		// TODO Auto-generated method stub
		provinceList= coolWeatherDB.loadProvince();
		if(provinceList.size() > 0){
			dataList.clear();
			for (Province province : provinceList){
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("你国");
			currentLevel= LEVEL_PROVINCE;
		}else{
			queryFromServer(null,"province");
		}
	}
	
	private void queryFromServer(final String code, final String type) {
		// TODO Auto-generated method stub
		String address;
        if(!TextUtils.isEmpty(code)){
			address = "http://www.weather.com.cn/data/list3/city"
					+code+".xml";
		}else{
			address = "http://www.weather.com.cn/data/list3/city"
					+".xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener(){
			
			@Override
			public void onFinish(String response){
				boolean result= false;
				if("province".equals(type)){
					result= Utility.handleProvinceResponse(
							coolWeatherDB, response);
				}else if("city".equals(type)){
					result= Utility.handleCitiesResponse(
							coolWeatherDB, response, 
							selectedProvince.getId());
				}else if ("county".equals(type)){
					result= Utility.handleCountiesResponse(
							coolWeatherDB, response, 
							selectedCity.getId());
				}
				
				if(result){
					runOnUiThread(new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							closeProgressDialog();
							if("province".equals(type)){
								queryProvinces();
							}else if("city".equals(type)){
								queryCities();
							}else if ("county".equals(type)){
								queryCounties();
							}
						}			
					});
				}
			}

			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this,
							"加载失败", Toast.LENGTH_SHORT).show();
					}
					
				});
			}
		});
		
	}
	private void showProgressDialog() {
		// TODO Auto-generated method stub
		if (progressDialog == null){
			progressDialog= new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	private void closeProgressDialog() {
		// TODO Auto-generated method stub
		if (progressDialog != null){
			progressDialog.dismiss();
		}
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
	}
    
    
}
