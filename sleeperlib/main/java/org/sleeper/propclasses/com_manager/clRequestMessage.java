package org.sleeper.propclasses.com_manager;

//
//
//  Generated by StarUML(tm) Java Add-In
//
//  @ Project : Sleeper
//  @ File Name : clRequestMessage
//  @ Date : 2015-09-06
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

/**
 * Make, dissolve request message to/from remote device. Currently, there is no use of dissolving
 * request message since request message is always made and sent from app side and ap device only
 * work as server. But for future modification, we remain code for dissolving.
 */
public class clRequestMessage extends clMessage {


    /**
     * Constructor
     */
    public clRequestMessage(){

        super() ;

        Header= clMessage.REQ ;
    }

    @Override
    /**
     * Make request message
     */
    public String makeMessage(){

        String reqMsg= (char)REQ+"\n"+(char)DeviceID+"\n" ;

        reqMsg+=(char)command ;//add command

        //add data if available
        if(data!=null) {

            for (int i = 0; i < data.length; i++)
                reqMsg += (char) data[i];
        }

        reqMsg += "\n" ;

        return reqMsg ;
    }

    @Deprecated
    @Override
    public void dissolveMessage(String message){

        setDeviceID((byte)(message.charAt(2))) ;
        setCommand((byte) (message.charAt(4))) ;

        //check if message contains data
        if(message.length()>5)
            setData(message.substring(5, message.length() - 2).getBytes()) ;

    }
}