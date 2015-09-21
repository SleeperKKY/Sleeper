//
//
//  Generated by StarUML(tm) Java Add-In
//
//  @ Project : Sleeper
//  @ File Name : clFanMessageConverter.java
//  @ Date : 2015-07-30
//  @ Author : Kang Shin Wook
//
//

package org.androidtown.sleeper.endclasses;

//
//
//  Generated by StarUML(tm) Java Add-In
//
//  @ Project : Sleeper
//  @ File Name : clApp.java
//  @ Date : 2015-09-06
//  @ Author : Kang Shin Wook, Lim Hyun Woo
//  @ Email : rkdtlsdnr102@naver.com

import android.util.Log;

import org.androidtown.sleeper.propclasses.com_manager.clDeviceMessageConverter;

public class clFanMessageConverter extends clDeviceMessageConverter {

	public static final byte FAN_ID =0x20 ;
	//public static final char TURN_OFF = 0x22 ;
	public static final byte PWM_SET = 0x21 ;

	public clFanMessageConverter() {

		//ID=clDeviceMessageConverter.FAN ;
	}

	@Override
	public String makeDeviceMessage() {

		String ControlInfo="" ;

		//if command doesn't need data

		Log.i("Fan Data",Byte.toString(data)) ;


			switch (Command) {

				//currently no data is required to send to remote device
				/*
				case TURN_OFF:
					ControlInfo += TURN_OFF;
					break;
					*/

				case PWM_SET:
					ControlInfo +=(char)PWM_SET ;
					ControlInfo +=(char)(data) ;
					break ;
			}

		return ControlInfo ;
	}

	@Override
	public void dissolveDeviceMessage(String _ControlInfo) {

		/*
		HashMap<Character,Float> dataMap=new HashMap<Character,Float>() ;

		char dataType ;
		float dataValue;

		dataType=_ControlInfo.charAt(0) ;
		dataValue=_ControlInfo.charAt(1) ;
		Log.i(toString(), "DataType: " + dataType + " DataValue: " + dataValue) ;
		dataMap.put(dataType,dataValue) ;
		*/

		this.Command=(byte)(_ControlInfo.charAt(0)) ;

		if(this.Command== clFanMessageConverter.PWM_SET) {
			this.data = (byte)(_ControlInfo.charAt(1));
		}

		/*
		//if delimiter matches ControlInfo's delimiter

		if(_ControlInfo.length()>2) {

			while (_ControlInfo.charAt(charPos) == ' ') {

				charPos++;

				dataType = _ControlInfo.charAt(charPos);
				dataValue = _ControlInfo.charAt(charPos + 1);

				dataMap.put(dataType, dataValue);

				charPos += 2;
			}
		}
		*/
	}

}
