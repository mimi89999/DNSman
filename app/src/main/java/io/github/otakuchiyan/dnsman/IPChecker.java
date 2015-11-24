package io.github.otakuchiyan.dnsman;

import android.util.Log;

public class IPChecker
{
	public static boolean IPv4Checker(String ip){
		if(!ip.equals("")){
		int dotCount = 0;
			//Detecting user inputed a dot as first char
		if(ip.charAt(0) == '.'){
				return false;
			}
		for(int i = 1; i != ip.length(); i++){
			if(ip.charAt(i - 1) == '.'
				//Detecting next dot
				&& !ip.substring(i, i + 1).equals(".")){
				dotCount++;
				if(dotCount == 3){
					//if i is a dot return false
					return i != ip.length();
				}
			}
		}
		}
		return false;
	}

    public static boolean IPv6Checker(String ip){
		return false;
    }
}
