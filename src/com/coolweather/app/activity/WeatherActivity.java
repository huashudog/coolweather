package com.coolweather.app.activity;


import com.coolweather.app.R;
import com.coolweather.app.service.AutoUpdateService;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;



public class WeatherActivity extends Activity implements OnClickListener{
    
	private LinearLayout weatherInfoLayout;
	private TextView cityNameText;
	private TextView publishText;
	private TextView weatherDespText;
	private TextView temp1Text;
	private TextView temp2Text;
	private TextView currentDateText;
	private Button switchCity;
	private Button refreshWeather;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.weather_layout);
		
		weatherInfoLayout= (LinearLayout) this.findViewById(
				R.id.weather_info_layout);
		cityNameText= (TextView) this.findViewById(R.id.city_name);
		publishText= (TextView) this.findViewById(R.id.publish_text);
		weatherDespText= (TextView) this.findViewById(R.id.weather_desp);
		temp1Text= (TextView) this.findViewById(R.id.temp1);
		temp2Text= (TextView) this.findViewById(R.id.temp2);
		currentDateText= (TextView) this.findViewById(R.id.current_data);
		switchCity= (Button) this.findViewById(R.id.switch_city);
		refreshWeather= (Button) this.findViewById(R.id.refresh_weather);
		String countyCode= this.getIntent().getStringExtra("county_code");
		
		if(!TextUtils.isEmpty(countyCode)){
			//retrive
			publishText.setText("同步中...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		}else{
			showWeather();
		}
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
	}

	private void showWeather() {
		// TODO Auto-generated method stub
		SharedPreferences prefs= PreferenceManager
				.getDefaultSharedPreferences(this);
		cityNameText.setText(prefs.getString("city_name", ""));
		temp1Text.setText(prefs.getString("temp1", ""));
		temp2Text.setText(prefs.getString("temp2", ""));
		weatherDespText.setText(prefs.getString("weather_desp", ""));
		publishText.setText(
				"今天"+prefs.getString("publish_time", "")+"发布");
		currentDateText.setText(prefs.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		Intent i= new Intent(this,AutoUpdateService.class);
		this.startService(i);
	}

	private void queryWeatherCode(String countyCode) {
		// TODO Auto-generated method stub
		String address= "http://www.weather.com.cn/data/list3"
				+ "/city"+countyCode+".xml";
		queryFromServer(address,"countyCode");	
	}
	
	private void queryWeatherInfo(String weatherCode){
		String address= "http://www.weather.com.cn/data/"
				+ "cityinfo/"+weatherCode+".html";
		queryFromServer(address,"weatherCode");
	
	}

	private void queryFromServer(
			final String address, final String type) {
		// TODO Auto-generated method stub
		HttpUtil.sendHttpRequest(address, 
				new HttpCallbackListener(){

					@Override
					public void onError(Exception e) {
						// TODO Auto-generated method stub
						runOnUiThread(new Runnable(){

							@Override
							public void run() {
								// TODO Auto-generated method stub
								  publishText.setText("同步失败");
							}
							
						});
					}

					@Override
					public void onFinish(final String response) {
						// TODO Auto-generated method stub
						if ("countyCode".equals(type)){
						    if(!TextUtils.isEmpty(response)){
						    	String[] array= response.split("\\|");
						    	if(array != null && array.length == 2){
						    		String weatherCode= array[1];
						    		queryWeatherInfo(weatherCode);
						    	}
						    }
						}else if ("weatherCode".equals(type)){
						    Utility.handleWeatherResponse(
						    		WeatherActivity.this, response);
						    runOnUiThread(new Runnable(){

								@Override
								public void run() {
									// TODO Auto-generated method stub
									showWeather();
								}
						    	
						    });
						}
					}
			
		});
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.switch_city:
			Intent intent= new Intent(this,ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_weather:
			publishText.setText("同步中...");
			SharedPreferences prefs= PreferenceManager
					.getDefaultSharedPreferences(this);
			String weatherCode= prefs.getString("weather_code", "");
			if(!TextUtils.isEmpty(weatherCode)){
				queryWeatherInfo(weatherCode);
			}
			break;
		default:
			break;
		}
		
	}

}
