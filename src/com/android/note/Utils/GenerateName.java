package com.android.note.Utils;

public class GenerateName {
	private String prefix;
	private long curNum = 0;
	
	public GenerateName(String prefix) {
		this.prefix = prefix;
	}
	
	public String generateName() {
		String retStr = prefix + String.valueOf(curNum);
		curNum++;
		return retStr;
	}
	
	public void setCurNum(long num) {
		curNum = num;
	}
	
	public long getCurNum() {
		return curNum;
	}
}
