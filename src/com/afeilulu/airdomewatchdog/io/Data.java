package com.afeilulu.airdomewatchdog.io;

import java.util.List;

public class Data {
	private String id;
	private String password;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public List<Digital> getDigitals() {
		return digitals;
	}
	public void setDigitals(List<Digital> digitals) {
		this.digitals = digitals;
	}

	public List<Analog> getAnalogs() {
		return analogs;
	}
	public void setAnalogs(List<Analog> analogs) {
		this.analogs = analogs;
	}

	private List<Digital> digitals;
	private List<Analog> analogs;
}
