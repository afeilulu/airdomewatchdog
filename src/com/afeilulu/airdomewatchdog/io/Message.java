package com.afeilulu.airdomewatchdog.io;

import java.util.List;

public class Message {

	private String id;
	private String ret;
	private String verify_code;
	private String nexttime;
	private String period;
	private String password;
	private List<Digital_C> dCs;
	private List<Analog_C> aCs;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getRet() {
		return ret;
	}
	public void setRet(String ret) {
		this.ret = ret;
	}
	public String getVerify_code() {
		return verify_code;
	}
	public void setVerify_code(String verify_code) {
		this.verify_code = verify_code;
	}
	public String getNexttime() {
		return nexttime;
	}
	public void setNexttime(String nexttime) {
		this.nexttime = nexttime;
	}
	public String getPeriod() {
		return period;
	}
	public void setPeriod(String period) {
		this.period = period;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public List<Digital_C> getdCs() {
		return dCs;
	}
	public void setdCs(List<Digital_C> dCs) {
		this.dCs = dCs;
	}
	public List<Analog_C> getaCs() {
		return aCs;
	}
	public void setaCs(List<Analog_C> aCs) {
		this.aCs = aCs;
	}
	
	
	
}
