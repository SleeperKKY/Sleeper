//
//
//  Generated by StarUML(tm) Java Add-In
//
//  @ Project : Sleeper
//  @ File Name : clTempSensorMessageConverter.java
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

public class clTempSensorMessageConverter extends clDeviceMessageConverter {

	public static final byte TEMP_ID =0x10 ;
	public static final byte MEASURE_TEMPERATURE = 0x11 ;


	public clTempSensorMessageConverter(){

		//ID=clDeviceMessageConverter.TEMP ;
	}

	@Override
	public String makeDeviceMessage() {


		String ControlInfo="" ;

			switch (Command) {

				//currently no data is required to send to remote device
				case MEASURE_TEMPERATURE:
					ControlInfo += (char)MEASURE_TEMPERATURE;
					break;

			}

		return ControlInfo ;
	}


	@Override
	public void dissolveDeviceMessage(String _ControlInfo) {

		this.Command=(byte)(_ControlInfo.charAt(0)) ;
		this.data=(byte)(_ControlInfo.charAt(1)) ;

		Log.i("Device command",""+_ControlInfo.charAt(0)) ;
		Log.i("Device data",""+_ControlInfo.charAt(1)) ;


		/*
		HashMap<Character,Float> dataMap=new HashMap<Character,Float>() ;

		char dataType, charPos ;
		float dataValue;

		dataType=_ControlInfo.charAt(0) ;
		dataValue=_ControlInfo.charAt(1) ;

		Log.i(toString(), "DataType: "+dataType+" DataValue: "+dataValue) ;

		dataMap.put(dataType, dataValue) ;

		charPos=2 ;

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

		return dataMap ;
		*/

	}

}


