package com.archermind.note.bean;

public class UserLoginInfo {

	private String userName;
	private String password;
	private long isSave;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public long getIsSave() {
		return isSave;
	}

	public void setIsSave(long isSave) {
		this.isSave = isSave;
	}

	public UserLoginInfo(String userName, String password, long isSave) {
		super();
		this.userName = userName;
		this.password = password;
		this.isSave = isSave;
	}

	public UserLoginInfo() {
		super();
	}

}
