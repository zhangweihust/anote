package com.archermind.note.Screens;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.archermind.note.R;
import com.archermind.note.Adapter.RegionInfoAdapter;
import com.archermind.note.Utils.PreferencesHelper;

public class PersonalInfoRegionScreen extends Screen implements OnClickListener  {
	
	private ListView mProvinceList; 
	private ListView mCityList; 
	private Context mContext; 
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.personalinfo_region);
		
		mContext = PersonalInfoRegionScreen.this;
		
		ImageButton btnback = (ImageButton) this.findViewById(R.id.back);
		btnback.setOnClickListener(this);
		
		final RegionInfoAdapter adapter1 = new RegionInfoAdapter(this, RegionInfoAdapter.REGION_PROVINCE);
		mProvinceList = (ListView) this.findViewById(R.id.region_province);
		mProvinceList.setAdapter(adapter1);
		
		final RegionInfoAdapter adapter2 = new RegionInfoAdapter(this, RegionInfoAdapter.REGION_CITY);
		mCityList = (ListView) this.findViewById(R.id.region_city);
		mCityList.setAdapter(adapter2);
		
		SharedPreferences preferences = PreferencesHelper.getSharedPreferences(mContext, 0);
		int province = preferences.getInt(PreferencesHelper.XML_USER_REGION_PROVINCE, 0);
		int city = preferences.getInt(PreferencesHelper.XML_USER_REGION_CITY, 0);
		
		mProvinceList.setSelection(province);
		
		adapter2.setProvince(province);
		if (adapter2.getCount() > 0) {
			mCityList.setSelection(city);
		}
		
		mProvinceList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				mProvinceList.setVisibility(View.GONE); 
				mCityList.setVisibility(View.VISIBLE);
				adapter2.setProvince(arg2);
				
				if (adapter2.getCount() == 0) {
					saveRegion(arg2, 0);
					finish();
				}
			}});
		
		mCityList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				saveRegion(adapter2.getProvince(), arg2);
				finish();
			}});
		mCityList.setVisibility(View.GONE);
	}
	
	private void saveRegion(int province, int city) {
		SharedPreferences preferences = PreferencesHelper.getSharedPreferences(mContext, Context.MODE_WORLD_WRITEABLE);
		Editor editor = preferences.edit();
		
		editor.putInt(PreferencesHelper.XML_USER_REGION_PROVINCE, province);
		editor.putInt(PreferencesHelper.XML_USER_REGION_CITY, city);
		
		editor.commit();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.back:
			if (mCityList.getVisibility() == View.VISIBLE) {
				mCityList.setVisibility(View.GONE); 
				mProvinceList.setVisibility(View.VISIBLE);
			} else {
				this.finish();
			}
			break;
		}
	}
}
