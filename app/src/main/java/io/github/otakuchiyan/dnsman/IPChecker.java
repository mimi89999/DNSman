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

    public static boolean IPSegmentChecker43C(String ip){
	if(ip.length() == 3 ||
	   ip.length() == 7 ||
	   ip.length() == 11){

	        int c = ip.length();
		if(!ip.substring(c - 1, c).equals(".") &&
		   !ip.substring(c - 2, c - 1).equals(".") &&
		   !ip.substring(c - 3, c - 2).equals(".")){
		    return true;
		}
		
	}
	return false;
    }
}
