//
//  @ File Name : ControlFanTemp.ino
//  @ Date : 2015-10-28
//  @ Author : Kang Shin Wook
//  @ Email : rkdtlsdnr102@naver.com
//
//
// Copyright (C) 2015  Kang Shin Wook
//
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License along
//  with this program; if not, write to the Free Software Foundation, Inc.,
//  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.


#include <clMessage.h>
#include "ESP8266.h"

//if UNO, remove "//"
//#define __UNO__

#ifdef __UNO__
#include <SoftwareSerial.h>
#endif

////////////////////////////////////////////////////
/////////////////define command/////////////////////
////////////////////////////////////////////////////
#define TEMP_ID 0x10 //temp sensor id
#define TEMP_MEASURE_TEMPERATURE 0x11 //get temperature command
#define FAN_ID 0x20 //fan id
#define FAN_PWMSET 0x21 //set pwm command
////////////////////////////////////////////////////
////////////////////////////////////////////////////
////////////////////////////////////////////////////
#define fan_Pin 3  // fan controlling pin
#define temp_Pin A0 //temperature sensor data pin
#define SSID "Sleeper" //wifi ssid
#define PASSWORD "1234567890" //wifi password

#ifdef __UNO__
SoftwareSerial mySerial(3,2) ;
ESP8266 wifi(mySerial) ;
#else
//instaniate the wifi object
ESP8266 wifi(Serial1) ;
#endif 
  
//declare message parsing objects
clResponseMessage resMsg=clResponseMessage() ;
clRequestMessage reqMsg=clRequestMessage() ;
  
void onReceiveMessage(uint8_t* buffer, int msgLen) ;
byte getTemperature() ;
void setPWM(byte pwm) ;
  
//message buffer, id
uint8_t buffer[128] = {0,} ;
uint8_t mux_id;
uint8_t len ;
String recvStream="" ;

//setup wifi
void setup()
{
   Serial.begin(9600);
    
    wifi.restart() ;
    
    Serial.print("setup begin\r\n");
    
    Serial.print("FW Version:");
    Serial.println(wifi.getVersion().c_str());
      
    //set ESP8266 module as StationAP mode
    if (wifi.setOprToStationSoftAP()) {
        Serial.print("to station + softap ok\r\n");
    } else {
        Serial.print("to station + softap err\r\n");
    }
    
    //allow multi connection
    if (wifi.enableMUX()) {
        Serial.print("multiple ok\r\n");
    } else {
        Serial.print("multiple err\r\n");
    }
    
     delay(100) ;
    
    //set ssid, password, channel, encryption mode
    if (wifi.setSoftAPParam(SSID, PASSWORD,11,4)) {
        Serial.print("Join AP success\r\n");
        Serial.print("IP: ");
        Serial.println(wifi.getLocalIP().c_str());    
    } else {
        Serial.print("Join AP failure\r\n");
    }
    
    //start tcp server, port 8090
    if (wifi.startTCPServer(8090)) {
        Serial.print("start tcp server ok\r\n");
    } else {
        Serial.print("start tcp server err\r\n");
    }
    
    //set server timeout
    if (wifi.setTCPServerTimeout(10)) { 
        Serial.print("set tcp server timout 10 seconds\r\n");
    } else {
        Serial.print("set tcp server timout err\r\n");
    }
    
    Serial.print("setup end\r\n");
}


void loop()
{ 
  //wait for message to come  
  len=wifi.recv(&mux_id, buffer, sizeof(buffer), 1000) ;
  if(len>0)
  {
     Serial.println("") ;
     Serial.println("\nReceived: ") ;
     for(uint32_t i = 0; i < len; i++) { 
           Serial.print((char)buffer[i],HEX);
           Serial.print(" ") ; 
     }
                 
     onReceiveMessage(buffer,len) ;
    }     
}

/**
 * onReceiveMessage is example of how message dissolving & making is done
 */
void onReceiveMessage(uint8_t* buffer, int msglen){
  	
    int i ;
    byte resData[10],toSendBuf[50] ;//response data to send
    reqMsg.dissolveMessage(buffer,msglen) ;//dissolve message
      			
  if(reqMsg.getDeviceID()==FAN_ID)//if device id for FAN
  {						
	if(reqMsg.getCommand()==FAN_PWMSET)//if device 
	{
		// set pwm with data
        	setPWM(reqMsg.getData()[0]) ;		
        }
  		
        //make response message
        //put device message
        resMsg.setCommand(FAN_PWMSET) ;
        resData[0]=reqMsg.getData()[0] ;//just send back pwm
        resMsg.setData(resData,1) ;
        //put device id				
        resMsg.setDeviceID(FAN_ID) ;
        //put response type
	resMsg.setResponseType(clResponseMessage::SUCCESS) ;    					
  }
  else if(reqMsg.getDeviceID()==TEMP_ID)//if device id for temperature sensor
  {          				
        if(reqMsg.getCommand()==TEMP_MEASURE_TEMPERATURE)
        {
		// measure temperature
                resData[0]=getTemperature() ;	
                //resData[0]=23 ;                
	}

	//make response message   
        resMsg.setCommand(TEMP_MEASURE_TEMPERATURE) ;
        resMsg.setData(resData,1) ;  
        //put device id			
	resMsg.setDeviceID(TEMP_ID) ;
        //put response type
	resMsg.setResponseType(clResponseMessage::SUCCESS) ;
  }
  
 //make response message to character stream			                          				                                                     
 resMsg.makeMessage(toSendBuf,&i) ;
 
  Serial.print("\nTo Send: ") ;
  for(uint32_t j = 0; j < i; j++) { 
        Serial.print((char)toSendBuf[j],HEX);
        Serial.print(" ") ; 
  }
  
 //send response message to sleeperApp
 wifi.send(mux_id,toSendBuf,i) ;                          
}
  
//read temperature from sensor, temp sensor pin is connected to analog pin 0
byte getTemperature(){
  
    int value = analogRead(temp_Pin);//read temperature from analog pin 0
    float voltage = (value/1024.0)*5000;
    float Celsius = voltage / 10;

    return (byte)Celsius;
}
  
//set fan's pwm
void setPWM(byte pwm){
  analogWrite(fan_Pin, (pwm*2));					
}
