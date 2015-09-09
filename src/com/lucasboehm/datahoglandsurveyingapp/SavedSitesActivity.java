package com.lucasboehm.datahoglandsurveyingapp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class SavedSitesActivity extends Activity {
	private ListView lv;
	ArrayList<String> sitelist = new ArrayList<String>();
	private String selectedSite;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_saved_data);

		String[] alist = readFromFile().split(",");


		for (String a : alist) {
			sitelist.add(a);
		}
		Log.e("Saved sites list = ", String.valueOf(alist.length));

		if (sitelist.size() <= 0){
			Toast.makeText(this, "No sites available", Toast.LENGTH_SHORT).show();
		}
		else{
			lv = (ListView) findViewById(R.id.listView1);

			
			
			
			
			
			lv.setOnItemClickListener(new OnItemClickListener() {
			      public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
			        String selectedFromList =(String) (lv.getItemAtPosition(myItemInt));

					Log.v("DEBUG", "List option :" + selectedFromList);

			        CloseActivityAndSendData(selectedFromList);
			        
			      }                 
			});
			
			
			
			
			
			
			
			ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
					this, 
					android.R.layout.simple_list_item_1,
					sitelist );

			lv.setAdapter(arrayAdapter); 

		}
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



	public String getSelectedSite() {
		return selectedSite;
	}



	public void setSelectedSite(String selectedSite) {
		this.selectedSite = selectedSite;
	}


	
	private void CloseActivityAndSendData(String result){
		Intent returnIntent = new Intent();
		returnIntent.putExtra("result",result);
		Log.v("Check CLosing", returnIntent.toString());
		setResult(RESULT_OK,returnIntent);
		finish();
	}
	
	
	
}
