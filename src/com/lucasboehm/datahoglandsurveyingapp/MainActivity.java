package com.lucasboehm.datahoglandsurveyingapp;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.Telephony.Sms.Conversations;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends FragmentActivity implements 
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener {


	private static final int GPS_ERRORDIALOG_REQUEST = 9001;
	private static final float DEFUALTZOOM = 17;
	private Button button;
	private ImageButton btnRemoveOne;
	private Button btnClear;
	private String CurrentSitename;
	private Boolean changed = false;
	ArrayList<SiteClass> sitelist = new ArrayList<SiteClass>();
	ArrayList<MapMarker> list = new ArrayList<MapMarker>();
	ArrayList<MapMarker> tmplist = new ArrayList<MapMarker>();
	ArrayList<Polyline> linelist = new ArrayList<Polyline>();
	LocationClient mLocationClient;
	GoogleMap mMap;
	Polyline line;
	Marker marker;

	// ON Create makes a map and goes to current location
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){

			if (servicesOK()) {
				// if i have access to google play services then display the map
				setContentView(R.layout.activity_map);    
				if(initMap()) {
					mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID); // changes default look at start up
					mMap.setMyLocationEnabled(true); // allows you to click button to find current location
					mLocationClient = new LocationClient (this,this, this);
					mLocationClient.connect();
					addListenerOnButton();


				}
				else{
					Toast.makeText(this, "Map not available. Please try again later", Toast.LENGTH_SHORT).show();
				}
			}
		}
		else{
			showGPSDisabledAlertToUser();
		}
	}


	@Override
	protected void onPause() {
		super.onPause();

		////////////////////////////////////////
		try {
			// Modes: MODE_PRIVATE, MODE_WORLD_READABLE, MODE_WORLD_WRITABLE
			FileOutputStream output = openFileOutput("tmp.txt",
					Context.MODE_PRIVATE);
			DataOutputStream dout = new DataOutputStream(output);
			dout.writeInt(list.size()); // Save line count
			for (MapMarker point : list) {
				dout.writeUTF(point.getLat() + "," + point.getLng());
				Log.v("write", point.getLat() + "," + point.getLng());
			}
			dout.flush(); // Flush stream ...
			dout.close(); // ... and close.
			list.clear();
			list.removeAll(list);
			Log.v("DEBUG", "Pause listsize is " + list.size());

		} catch (IOException exc) {
			exc.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){

			try {
				FileInputStream input = openFileInput("tmp.txt");
				DataInputStream din = new DataInputStream(input);
				int sz = din.readInt(); // Read line count
				for (int i = 0; i < sz; i++) {
					String str = din.readUTF();
					Log.v("read", str);
					String[] stringArray = str.split(",");
					double latitude = Double.parseDouble(stringArray[0]);
					double longitude = Double.parseDouble(stringArray[1]);
					setMarker(latitude,longitude);
				}

				din.close();

			} catch (IOException exc) {
				exc.printStackTrace();
			}
		}
		else{
			showGPSDisabledAlertToUser();
		}

		MapStateManager mgr = new MapStateManager(this);
		CameraPosition position = mgr.getSavedCameraPosition();
		if(position != null){
			CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
			mMap.moveCamera(update);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		MapStateManager mgr = new MapStateManager(this);
		mgr.saveMapState(mMap);
	}
	// Displays a prompt that will take user to location services on phone will close the app after to prevent issue
	private void showGPSDisabledAlertToUser() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
		.setCancelable(false)
		.setPositiveButton("Goto Settings Page To Enable GPS",
				new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int id){
				Intent callGPSSettingIntent = new Intent(
						android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);	                
				startActivity(callGPSSettingIntent);
				System.exit(0);

			}
		});
		alertDialogBuilder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int id){
				System.exit(0);
				dialog.cancel();
			}
		});
		AlertDialog alert = alertDialogBuilder.create();
		alert.show();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();

	}




	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.mapTypeNormal:
			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			break;
		case R.id.mapTypeSatellite:
			mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			break;
		case R.id.mapTypeTerrain:
			mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
			break;
		case R.id.mapTypeHybrid:
			mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			break;
		case R.id.Load:
			Toast.makeText(this, "Loading Saved Sites", Toast.LENGTH_SHORT).show();
			loadSite();
			break;
		case R.id.Save:
			saveSite();
			Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
			break;
		case R.id.NewSite:
			CreateNewSiteStraightUp();
			break;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}


	private void saveSite() {
		changed = false;
		try {
			// Modes: MODE_PRIVATE, MODE_WORLD_READABLE, MODE_WORLD_WRITABLE
			String FILENAME = CurrentSitename + ".txt";
			FileOutputStream output = openFileOutput(FILENAME,
					Context.MODE_PRIVATE);
			Log.v("SAVING TO ", FILENAME);

			DataOutputStream dout = new DataOutputStream(output);
			Log.v("SAVIGN LISTSIZE OF ", String.valueOf(list.size()));

			dout.writeInt(list.size()); // Save line count
			for (MapMarker point : list) {
				dout.writeUTF(point.getLat() + "," + point.getLng());
				Log.v("write", point.getLat() + "," + point.getLng());
			}
			dout.flush(); // Flush stream ...
			dout.close(); // ... and close.
			writeToFile(sitelist);
			try {
				//removeEverything();
				FileInputStream input = openFileInput(FILENAME);
				DataInputStream din = new DataInputStream(input);
				int sz = din.readInt(); // Read line count
				for (int i = 0; i < sz; i++) {
					String str = din.readUTF();
					Log.v("read", str);
				}

				din.close();

			} catch (IOException exc) {
				exc.printStackTrace();
			}


		} catch (IOException exc) {
			exc.printStackTrace();
		}


	}


	private void loadSite() {
		
		changed = false;
		Intent intent = new Intent(this,SavedSitesActivity.class);
		startActivityForResult(intent, 1001);
	}



	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data); 
		Log.v("DEBUG", "test");


		if(resultCode == RESULT_OK){
			String result =data.getStringExtra("result");
			Log.v("DEBUGED", result);
			String FILENAME = result + ".txt";
			Log.v("Loading from", FILENAME);
			try {
				removeEverything();
				FileInputStream input = openFileInput(FILENAME);
				DataInputStream din = new DataInputStream(input);
				int sz = din.readInt(); // Read line count
				for (int i = 0; i < sz; i++) {
					String str = din.readUTF();
					Log.v("read", str);
					String[] stringArray = str.split(",");
					double latitude = Double.parseDouble(stringArray[0]);
					double longitude = Double.parseDouble(stringArray[1]);
					setMarker(latitude,longitude);
				}

				din.close();

			} catch (IOException exc) {
				exc.printStackTrace();
			} 
		}
		if (resultCode == RESULT_CANCELED) {
			Log.v("FAILED", "GAHH");
		}
	}

	private void ChangesMade(){
		
		if 	(changed == true){

		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Save?");
		alert.setMessage("It seems some changes have been made do you want to save this Site?");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				saveSite();

			}
		});

		alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Canceled.
			}
		});
		alert.show();	
		}
		else{
		changed = false;
		}
	}

	private void CreateNewSite(){
		
		
		changed = false;
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Create");
		alert.setMessage("In order to set a marker you must create a site would you like to do that now?");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				Editable value = input.getText();
				CurrentSitename = value.toString();
				if (CurrentSitename.trim().length() > 0)
				{
					addSitetoList();
				}

			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Canceled.
			}
		});
		alert.show();	
	}

	
	private void CreateNewSiteStraightUp(){
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Create");
		alert.setMessage("Name of site");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Create", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				Editable value = input.getText();
				CurrentSitename = value.toString();
				if (CurrentSitename.trim().length() > 0)
				{
					addSitetoList();
				}

			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Canceled.
				
			}
		});
		alert.show();	
	}
	private void addSitetoList(){
		SiteClass siteInstance = new SiteClass();
		siteInstance.setName(CurrentSitename);
		sitelist.add(siteInstance);
		Log.v("DEBUG", "New Site Created");

		gotoCurrentLocationAndMark();
		//Toast.makeText(this, "Size of list is :" +siteInstance.getList().size(), Toast.LENGTH_SHORT).show();

	}

	// If MARK BUTTON is pressed
	public void addListenerOnButton() {
		button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				if(sitelist.size() > 0){	
					gotoCurrentLocationAndMark();
				}
				else{
					CreateNewSite();
				}
			}

		});
		btnClear = (Button) findViewById(R.id.btnClear);
		btnClear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				removeEverything();
			}

		});
		btnRemoveOne = (ImageButton) findViewById(R.id.btngoback);
		btnRemoveOne.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				removeLastMarker();
				//Toast.makeText(MainActivity.this, "Remove last marker", Toast.LENGTH_LONG).show();            
			}



		});


	}


	// Checks if google play Services is connected and Available
	public boolean servicesOK() {
		int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (isAvailable == ConnectionResult.SUCCESS) {
			return true;
		}
		else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable, this, GPS_ERRORDIALOG_REQUEST);
			dialog.show();
		}
		else {
			Toast.makeText(this, "Can't connect to Google Play services", Toast.LENGTH_SHORT).show();
		}
		return false;
	}

	// Populates the Map
	private boolean initMap(){
		if (mMap == null){
			SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
			mMap = mapFrag.getMap();

		}
		if (mMap != null) {

			mMap.setOnMarkerClickListener(new OnMarkerClickListener() {

				@Override
				public boolean onMarkerClick(Marker marker) {
					String name = marker.getTitle().trim();
					//Toast.makeText(MainActivity.this, , Toast.LENGTH_LONG).show();
					if(list.size() > 1) {
						removeLines();
						removeText();
						for (MapMarker m : list)
							if (m.getName().trim().equals(name)){
							}
							else{
								drawLine(marker, m.getMarker());
								//Toast.makeText(MainActivity.this, m.getName(), Toast.LENGTH_LONG).show();
							}
					}

					return false;
				}
			});


			mMap.setOnMapLongClickListener(new OnMapLongClickListener() {

				@Override
				public void onMapLongClick(LatLng arg0) {
					// TODO Auto-generated method stub
					Toast.makeText(MainActivity.this, marker.getTitle(), Toast.LENGTH_LONG).show();

				}
			});
		}
		return (mMap != null);
	}


	// Creates a marker..
	private void setMarker(double lat, double lng){
		changed = true;
		Log.v("DEBUG", "Enter Set marker " + list.size());
		MapMarker markerInstance = new MapMarker(list.size() + 1);

		MarkerOptions options = new MarkerOptions()
		.title("Marker" + (list.size() + 1))
		.position(new LatLng(lat,lng)); 

		marker  = mMap.addMarker(options);
		markerInstance.setMarker(marker);
		markerInstance.setLat(lat); // this will be used to get distance between two positions...
		markerInstance.setLng(lng);

		list.add(markerInstance);
		Log.v("DEBUG", "New Marker has been created  at "+ lat + " and " + lng + "list now has " + list.size());

		//Toast.makeText(this, "Size of list is :" + list.size(), Toast.LENGTH_SHORT).show();

	}

	private void createTextView(String marker1Title, String marker2Title, double distance){
		LinearLayout layout = (LinearLayout) findViewById(R.id.layout);

		TextView text = new TextView(this);	
		text.setTextSize(20);
		text.setText("  " + marker1Title + "   " + marker2Title + "	  " + distance + "ft");

		layout.addView(text);
	}

	// Draws lines between markers
	private void drawLine(Marker marker1,Marker marker2) {
		// TODO Auto-generated method stub
		PolylineOptions options = new PolylineOptions()
		.add(((Marker) marker1).getPosition())
		.add(((Marker) marker2).getPosition())
		.color(Color.rgb(255, 198, 92))
		.width(8);

		line = mMap.addPolyline(options);

		linelist.add(line);
		double totaldistance = distance(marker1.getPosition().latitude,marker1.getPosition().longitude,marker2.getPosition().latitude,marker2.getPosition().longitude,"h");
		createTextView(marker1.getTitle(),marker2.getTitle(),totaldistance);
	}

	public void removeLines(){
		for (Polyline l : linelist)
			l.remove();
		linelist.removeAll(linelist);
	}

	public void removeText(){
		LinearLayout layout = (LinearLayout) findViewById(R.id.layout);

		if(((LinearLayout) layout).getChildCount() > 0) 
			((LinearLayout) layout).removeAllViews(); 
	}
	private void removeLastMarker() {
		// TODO Auto-generated method stub

		if(list.size()<= 0){
			Toast.makeText(this,"There are no markers to remove",Toast.LENGTH_SHORT).show();
		}
		else{
			list.get(list.size() - 1).getMarker().remove();
			list.remove(list.size() - 1);
			Log.v("DEBUG", " Marker has been removed listsize is " + list.size());

		}

	}

	private void reDrawMap(){
		int count = list.size();
		tmplist = list;
		Log.v("DEBUG", " Size " + list.size());
		removeEverything();
		for (int x = 0; x <= count; x++){
			setMarker(tmplist.get(x).getLat(), tmplist.get(x).getLng());
			Log.v("DEBUG", " Count = " + count);
		}
	}
	// Clears the Map
	private void removeEverything() {
		// TODO Auto-generated method stub
		mMap.clear();
		list.clear();
		list.removeAll(list);
		removeLines();
		removeText();
		Log.v("DEBUG", "Cleared Size of list is" + list.size());

	}
	private void CleanMap(){
		mMap.clear();
		removeLines();
		removeText();
		Log.v("DEBUG", "Cleaned Map" + list.size());

	}


	// GO to current location
	protected void gotoCurrentLocationAndMark() {

		Location currentLocation = mLocationClient.getLastLocation();
		if(currentLocation == null){
			Toast.makeText(this, "Current location isn't available", Toast.LENGTH_SHORT).show();
		}
		else{
			LatLng ll = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());

			setMarker(currentLocation.getLatitude(),currentLocation.getLongitude());

			//CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, DEFUALTZOOM);
			CameraUpdate update = CameraUpdateFactory.newLatLng(ll);
			mMap.moveCamera(update);
		}

	}


	private void writeToFile(ArrayList<SiteClass> list) {
		String data = "";
		for (SiteClass s : list) {
			data = data + s.getName() + ",";

		}
		Log.v("DEBUG", "Data = " + data);

		try {
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("SavedSites.txt", Context.MODE_PRIVATE));
			outputStreamWriter.write(data);
			outputStreamWriter.close();
		}
		catch (IOException e) {
			Log.e("Exception", "File write failed: " + e.toString());
		} 
		Log.v("DEBUG", "Read Data = " +  readFromFile());

	}

	private String readFromFile() {

		String ret = "";

		try {
			InputStream inputStream = openFileInput("SavedSites.txt");

			if ( inputStream != null ) {
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				String receiveString = "";
				StringBuilder stringBuilder = new StringBuilder();

				while ( (receiveString = bufferedReader.readLine()) != null ) {
					stringBuilder.append(receiveString);
				}

				inputStream.close();
				ret = stringBuilder.toString();
			}
		}
		catch (FileNotFoundException e) {
			Log.e("login activity", "File not found: " + e.toString());
		} catch (IOException e) {
			Log.e("login activity", "Can not read file: " + e.toString());
		}

		return ret;
	}

	protected void startupGetCurrentLocation() {

		Location currentLocation = mLocationClient.getLastLocation();
		if(currentLocation == null){
			Toast.makeText(this, "Current location isn't available", Toast.LENGTH_SHORT).show();
		}
		else{

			LatLng ll = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
			CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, DEFUALTZOOM);
			mMap.moveCamera(update);
		}

	}


	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		Toast.makeText(this,"Failed to Connect to location service",Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		//Toast.makeText(this,"Connected to location service",Toast.LENGTH_SHORT).show();
		startupGetCurrentLocation();
	}


	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
	}

	private double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (unit == "K") {
			dist = dist * 1.609344 ;
		} else if (unit == "N") {
			dist = dist * 0.8684;
		}
		return Math.round(dist * 3280.84);
	}

	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::  This function converts decimal degrees to radians             :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	private double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::  This function converts radians to decimal degrees             :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	private double rad2deg(double rad) {
		return (rad * 180 / Math.PI);
	}








}