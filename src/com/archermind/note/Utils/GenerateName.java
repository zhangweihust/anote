package com.archermind.note.Utils;

public class GenerateName {
	private long curNum = 0;
	
	public GenerateName() {
		
	}
	
	public String generateName() {
		String retStr = String.valueOf(curNum);
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
