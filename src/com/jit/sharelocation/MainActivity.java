package com.jit.sharelocation;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.AMap.OnMapScreenShotListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;

public class MainActivity extends Activity implements
				OnMapScreenShotListener, OnMarkerClickListener, 
				OnMapLoadedListener, InfoWindowAdapter, OnClickListener {
	
	private Button mShareBtn;
	private ImageView mBackBtn;
	private MapView mapView;

	private AMap aMap;
	private LocationManagerProxy lmp;
	private AMapLocation location;

	private double latitude;
	private double longitude;
	private float zoomLevel = 16;

	// 地图加载完毕的标志位
	private boolean isMapLoad = false;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView( R.layout.activity_main);
		
		mShareBtn = (Button) findViewById(R.id.share_btn);
		mBackBtn = (ImageView) findViewById(R.id.back_2_location);
		mapView = (MapView) findViewById(R.id.mapview);
		mShareBtn.setOnClickListener(this);
		mBackBtn.setOnClickListener(this);
		
		mapView.onCreate(savedInstanceState);
		aMap = mapView.getMap();
		aMap.setOnMapLoadedListener(this);
		aMap.setOnMarkerClickListener(this);
		aMap.setInfoWindowAdapter(this);

		startGetLocation();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
		if (lmp != null) {
			lmp.removeUpdates(locationListener);
			lmp.destory();
		}
		lmp = null;
	}

	private final void startGetLocation() {
		lmp = LocationManagerProxy.getInstance(this);
		lmp.setGpsEnable(true);
		lmp.requestLocationUpdates(LocationProviderProxy.AMapNetwork, 3000, 10, locationListener);
	}

	private void drawMyLocation(final String title, final double latitude,
			final double longitude) {
		if (isMapLoad) {
			aMap.clear();
			LatLng latLng = new LatLng(latitude, longitude);
			MarkerOptions markerOption = new MarkerOptions();
			markerOption.title(title);
			markerOption.snippet(title);
			markerOption.position(latLng);
			markerOption.perspective(true);
			markerOption.draggable(true);
			markerOption.anchor(0.5f, 0.5f);
			Marker marker = aMap.addMarker(markerOption);
			marker.showInfoWindow();
			marker.setVisible(true);
			aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(latLng, zoomLevel, 0, 30)));
		} else {
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					drawMyLocation(title, latitude, longitude);
				}
			}, 1000);
		}
	}

	private AMapLocationListener locationListener = new AMapLocationListener() {
		public void onLocationChanged(AMapLocation loc) {
			if (loc != null && location == null) {
				location = loc;
				aMap.clear();
				String title = loc.getExtras().getString("desc");
				latitude = loc.getLatitude();
				longitude = loc.getLongitude();
				drawMyLocation(title, latitude, longitude);
			}
		}
		@Override
		public void onLocationChanged(Location location) {}
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras){}
		@Override
		public void onProviderEnabled(String provider) {}
		@Override
		public void onProviderDisabled(String provider) {}
	};

	
	@Override
	public void onMapScreenShot(Bitmap bitmap) {
		Intent data = new Intent();

		Bundle locBundle = location.getExtras();
		String desc = null;
		if (locBundle != null) {
			desc = locBundle.getString("desc");
			data.putExtra("title", desc);
		}
		data.putExtra("longitude", location.getLongitude());
		data.putExtra("latitude", location.getLatitude());
		data.putExtra("zoomLevel", aMap.getCameraPosition().zoom);
		String savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/location.jpg";

		int width = DensityUtil.dip2px(MainActivity.this, 206);
		int height = DensityUtil.dip2px(MainActivity.this, 120);
		int x = (bitmap.getWidth() - width) >> 1;
		int y = (bitmap.getHeight() - height) >> 1;
		bitmap = Bitmap.createBitmap(bitmap, x, y, width, height);
		ScreenShot.savePicbyJPG(bitmap, savePath);
		
		AndroidShare as = new AndroidShare(
				MainActivity.this,
				"我在这里" + desc + "!快来和你的小伙伴们分享你的位置吧",
				savePath);
		as.show();
	}

	// 对marker标注点点击响应事件
	@Override
	public boolean onMarkerClick(Marker marker) {
		marker.showInfoWindow();
		aMap.animateCamera(CameraUpdateFactory.changeLatLng(marker.getPosition()));
		return true;
	}

	@Override
	public void onMapLoaded() {
		isMapLoad = true;
	}

	/**
	 * 显示标注content
	 */
	@Override
	public View getInfoContents(Marker marker) {
		return null;
	}

	/**
	 * 显示标注title
	 */
	@Override
	public View getInfoWindow(Marker marker) {
		TextView tv = new TextView(this);
		tv.setText(marker.getTitle());
		tv.setTextColor(Color.BLACK);
		tv.setTextSize(10);
		return tv;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.share_btn:
			if (location == null) {
				UIHelper.showToastShort(MainActivity.this, R.string.no_access_current_location);
				return;
			}
			aMap.getMapScreenShot(this);
			break;
		case R.id.back_2_location:
			LatLng latLng = null;
			if (location != null) {
				latLng = new LatLng(latitude, longitude);
				aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(latLng, zoomLevel, 0, 30)));
			} else {
				UIHelper.showToastShort(MainActivity.this, R.string.no_access_current_location);
			}
			break;
		default:
			break;
		}
	};

}
