package com.lucasboehm.datahoglandsurveyingapp;

import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapMarker {
	LocationClient mLocationClient;
	private double latpos;
	private double lngpos;
	private int makernumber;
	private String name;
	Marker marker;
	private boolean isSelected = false;
	public MapMarker(int num) {
	    // TODO Auto-generated constructor stub
		this.makernumber = num;
		this.name = "Marker" + this.makernumber;
		
	    
	}

	// returns Latitude for x marker
	public double getLat(){
		
		return latpos;
		
	}
	
	// returns Longitude for x marker
	public double getLng(){
		
		return lngpos;
		
	}
	
	public Marker getMarker(){
		return this.marker;
	}
	
	public String getName(){
		return name;
	}

	public boolean getSelected(){
		return isSelected;
	}
	public void setLat(double num){
		this.latpos = num;
	}
	
	public void setLng(double num){
		this.lngpos = num;

	}
	
	public void setMarker(Marker marker){
		this.marker = marker;
	}
	
	public void createMarkerwithCords(double lat, double lng,int num,GoogleMap mMap){
		MarkerOptions options = new MarkerOptions()
		.title("Marker" + num)
		.position(new LatLng(lat ,lng)); 
		marker  = mMap.addMarker(options);
	}

	
}
