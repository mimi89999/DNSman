package io.github.otakuchiyan.dnsman;

import android.util.Log;

public class IPChecker
{
	public static boolean IPv4Checker(String ip){
		if(!ip.equals("")){
		int dotCount = 0;
			//Detecting user inputed a dot as first char
			if(ip.substring(0, 1).equals(".")){
				return false;
			}
		for(int i = 1; i != ip.length(); i++){
			if(ip.substring(i - 1, i).equals(".")
				//Detecting next dot
				&& !ip.substring(i, i + 1).equals(".")){
				dotCount++;
				if(dotCount == 3){
					if (i == ip.length()){
						return false;
					}
					return true;
				}
			}
		}
		}
		return false;
	}
}
