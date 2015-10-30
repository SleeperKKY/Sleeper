//
//  @ File Name : clMessage.cpp
//  @ Date : 2015-10-28
//  @ Author : Kang Shin Wook, Lim Hyun Woo
//  @ Email : rkdtlsdnr102@naver.com
//
//
// Copyright (C) 2015  Kang Shin Wook, Lim Hyun Woo
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

//////////////////////////////////////////////////////////////////////////////////
////////////////////////Message class implementation//////////////////////////////
//////////////////////////////////////////////////////////////////////////////////

clMessage::clMessage(){
	
	dataLen=0 ;
}

void clMessage::setDeviceID(byte _Id) {

		DeviceID=_Id ;
}

byte clMessage::getHeader() {

		return Header ;
}

byte clMessage::getDeviceID() {

		return DeviceID ;
}

void clMessage::setCommand(byte cmd){

	command=cmd ;
}

byte clMessage::getCommand(){

	return command ;
}

void clMessage::setData(byte data[], int dataLen){

	int i ;
	
	//manually copy all data to internal buffer
	if(data!=NULL){
	
		for(i=0;i<dataLen;i++)
			this->data[i]=data[i] ;
	
		this->dataLen=dataLen ;
	}
}

byte* clMessage::getData(){	
	
	if(dataLen>0)//if there's data
		return data ;
	else
		return NULL ;
}

byte clMessage::getDataLength(){	 

	return dataLen ;
}

clMessage::~clMessage(){

}

//////////////////////////////////////////////////////////////////////////////////////////
////////////////////////RequestMessage class implementation///////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////

clRequestMessage::clRequestMessage() : clMessage(){

      Header= clMessage::REQ ;  
}
   
void clRequestMessage::makeMessage(byte *buf, int* bufLen){

	int msglen=0,i ;
	
	buf[0]=(char)REQ ;
	buf[1]='\n' ;
	buf[2]=(char)DeviceID ;
	buf[3]='\n' ;
	buf[4]=(char)command ;
	
	//insert all data
	if(data!=NULL){
		for(i=0;i<dataLen;i++)
		{
			buf[i+5]=(char)data[i] ;
		}
	}
	
	buf[5+dataLen]='\n' ;
	msglen=6+dataLen ;
	
	*bufLen=msglen ;
}

void clRequestMessage::dissolveMessage(const byte *message, int msgLen){

	int i,mLen=msgLen ;
    setDeviceID((byte)message[2]) ;
	setCommand((byte)message[4]) ;
	
	if(msgLen>5){
		for(i=5;i<=mLen-1;i++)
		{
			data[i-5]=(byte)message[i] ;
			dataLen++ ;
		}
	}

}

clRequestMessage::~clRequestMessage(){

}

//////////////////////////////////////////////////////////////////////////////////////////
////////////////////////ResponseMessage class implementation//////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////

clResponseMessage::clResponseMessage() : clMessage() {
      
        Header= clMessage::RES ;
}

void clResponseMessage::setResponseType(byte _resType){

    ResponseType=_resType ;
}

byte clResponseMessage::getResponseType(){

   return ResponseType ;
}

    
void clResponseMessage::makeMessage(byte *buf, int* bufLen){

 int msglen=0,i ;
	
 buf[0]=RES ;
 buf[1]='\n' ;
 buf[2]=ResponseType ;
 buf[3]='\n' ;
 buf[4]=DeviceID ;
 buf[5]='\n' ;
 buf[6]=command ;
 
 //insert all data
 if(data!=NULL){
	 for(i=0;i<dataLen;i++)
	 {
		 buf[i+7]=data[i] ;
	 }
 }
 buf[7+dataLen]='\n' ;
 msglen=8+dataLen ;
	
 *bufLen=msglen ;  
}
 
void clResponseMessage::dissolveMessage(const byte* message, int msgLen){

	int i,mLen=msgLen ;
	
	setResponseType(message[2]) ;
    setDeviceID(message[4]) ;
	setCommand(message[6]) ;
	
	if(msgLen>7){
		for(i=7;i<=mLen-1;i++)
		{
			data[i-7]=message[i] ;
			dataLen++ ;
		}
	}
}

clResponseMessage::~clResponseMessage(){

}