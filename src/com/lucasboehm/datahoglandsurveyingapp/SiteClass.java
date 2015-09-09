package com.lucasboehm.datahoglandsurveyingapp;

import java.util.ArrayList;

public class SiteClass {


	private String Name;
	private ArrayList<MapMarker> mlist;
	 


	public String getName() {
		return Name;
	}


	public void setName(String name) {
		Name = name;
	}


	public ArrayList<MapMarker> getList() {
		return mlist;
	}


	public void setList(ArrayList<MapMarker> mlist) {
		this.mlist = mlist;
	}

}
