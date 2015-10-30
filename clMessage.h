//
//  @ File Name : clMessage.h
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


#include <Arduino.h> //arduino library

#ifndef __CLMESSAGE__
#define __CLMESSAGE__

class clMessage {

public:
     static const byte REQ = 0x01 ;
	 static const byte RES = 0x02 ;
	 static const byte MAX_DATA_LEN=20 ;

protected:
	 byte Header;//header of message, RES, REQ
	 byte DeviceID;//device id
	 byte command ;//command for device
	 byte data[MAX_DATA_LEN] ;//data buffer for command
	 int dataLen;//length of data

public:
	 /**
	 * Constructor
	 */
	 clMessage() ;
	 /**
	 * Set device id that sends device message
	 * @param _Id device id to send
	 */
	 void setDeviceID(byte _Id) ;	
	 /**
	 * Set device message to send
	 * @param _devMsg device message to send
	 */
	 void setDeviceMessage(String* _devMsg) ;
	  /**
	 * Get header type of message
	 * @return header of message
	 */
	 byte getHeader() ;
	  /**
	 * Get device id that sends/receive device message
	 * @return device id that sends/receive device message
	 */
	 byte getDeviceID() ;
	 /**
	 * Set device command, mainly for sending response message
	 * @param cmd device command
	 */
	 void setCommand(byte cmd) ;
	 /**
	 * Get device command, mainly for getting request message
	 * @return command that message object contains
	 */
	 byte getCommand() ;
	 /**
	 * Set data to send
	 * @param data array of data to send
	 * @param dataLen length of data to send
	 */
	 void setData(byte data[], int dataLen) ;
	 /**
	 * Get data
	 * @param dataLen to store length of data
	 * @return get data array
	 */
	 byte* getData() ;
	 /**
	 * Get data length for command in this message
	 * @return data length for command in this message
	 */
	 byte getDataLength() ;
	 /**
	 * Make message and store in buffer
	 * @param buf buffer to store message
	 * @param bufLen variable to store length of message
	 */
	 virtual void makeMessage(byte *buf, int* bufLen)=0 ;
	 /**
	 * Dissolve message
	 * @param msgLen length of message
	 */
	 virtual void dissolveMessage(const byte *message, int msgLen)=0 ;
	 virtual ~clMessage() ;
} ;

//request message class
class clRequestMessage : public clMessage {

public:
	 /**
     * Constructor
     */
    clRequestMessage() ;
	 /**
     * Make request message.
	 * @param buf buffer to store message
	 * @param buflen store message length
     */
	void makeMessage(byte* buf, int* buflen) ;
	/**
	* Dissolve request message.
	* @param message message to dissolve
	@ @param msgLen message length
	*/
	void dissolveMessage(const byte* message, int msgLen) ;

	~clRequestMessage() ;
} ;

//response message class
class clResponseMessage : public clMessage {

private:
	byte ResponseType ;

public:
	//response message type constant: SUCCESS, FAIL
	static const byte SUCCESS=0x03 ;
    static const byte FAIL=0x04 ;
public:
	clResponseMessage() ;
	/**
	* Set response message type
	* @param _resType response message type
	*/
	void setResponseType(byte _resType) ;
	/**
	* Get response message type
	* @return response type of this message
	*/
	byte getResponseType() ;
	/**
	* Make response message.
	* @param buf buffer to store response message
	* @param buflen variable to store length of response message
	*/
	void makeMessage(byte *buf, int *buflen) ;
	/**
	* Dissolve response message
	* @param message response message to dissolve
	* @param msgLen length of response message
	*/
	void dissolveMessage(const byte* message, int msgLen) ;
	~clResponseMessage() ;
};

#endif


