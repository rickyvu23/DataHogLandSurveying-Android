package com.lucasboehm.datahoglandsurveyingapp.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SiteItems {

	/**
	 * @param args
	 */
	private String key;
	private String text;
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public static SiteItems getNew(){
		Locale locale = new Locale("en_US");
		Locale.setDefault(locale);
		
		String pattern = "yyyy-MM-dd HH:mm:ss Z";
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		String key = formatter.format(new Date());
		
		SiteItems site = new SiteItems();
		site.setKey(key);
		site.setText("");
		
		return site;
	}

}
