
#include "Sleeper_Library.h"
#include <string.h>


//////////////////// DiviceID distinguish partion ////////////////////

/**
* Constructor
*/
MSG_Analyzer::MSG_Analyzer()
{
}

/**
*Analayse message and return response
*@param MSG message to analyse
*@return response message to send
*/
char* MSG_Analyzer::analize_MSG(char MSG[])  // 아두이노가 모은 15바이트의 수신 메시지는 'analize_MSG()'가 제일 처음 분석합니다.
{

    this->deviceID = MSG[9];  //get device id, it changes if message format changes
    char *toSend ;

    if(deviceID == tmp)  // 'deviceID' indicates temperature sensor
    {
        //Serial.print("TMP TMP TMP");
        THERMOMETER buf;  // create temperature message analyzer
        toSend=buf.MSG_DIVISION(MSG);  // analyze temperature and make response message


    }
    else if(deviceID == fan)  // 'deviceID' indicates fan
    {
        //Serial.print("FAN FAN FAN");
        FAN buf;  // create fan message analyzer
        toSend=buf.MSG_DIVISION(MSG);  // analyze temperature and make response message
    }

    memcpy(toSend_MSG,toSend,30) ;//copy response message to response buffer

    return toSend_MSG ;

}



//////////////////// Thermometer analysis partion ////////////////////


/**
* Analyze temperature sensor message
* @param MSG received message
*/
char* MSG_Analyzer::THERMOMETER::MSG_DIVISION(char MSG[])
{
    command = this->get_Command(MSG);  // get command from message


    if(command == tmp_Get)  // if command is tmp_Get, getting temperature
    {
		//assemble response message
        Returning_MSG[0] = RES;
        Returning_MSG[1] = 0x0A;
        Returning_MSG[2] = SUCCESS;
        Returning_MSG[3] = 0x0A;
        Returning_MSG[4] = tmp;
        Returning_MSG[5] = 0x0A;
        Returning_MSG[6] = tmp_Get;
        Returning_MSG[7] = get_Temperature();//get temperature
        Returning_MSG[8] = 0x0A;
        Returning_MSG[9] = END1;
        Returning_MSG[10] = END2;

        return Returning_MSG ;// return assembled response message
    }
}

/*
* Get command from received message
* @param MSG received message
*/
char MSG_Analyzer::THERMOMETER::get_Command(char MSG[])
{
    return MSG[11];  //currently, command is at index number 11 of received message
}

/*
* Get temperature from temperature sensor
* @return temperature
*/
char MSG_Analyzer::THERMOMETER::get_Temperature()
{
    int value = analogRead(0);//read temperature from analog pin 0
    float voltage = (value/1024.0)*5000;
    float Celsius = voltage / 10;

    return Celsius;
}



//////////////////// Fan analysis partion ////////////////////

/**
* Analyze fan message
* @param MSG received message
*/
char* MSG_Analyzer::FAN::MSG_DIVISION(char MSG[])
{
    command = this->get_Command(MSG);// get command from message

    if(command == fan_PwmSet)  // if command is fan_PwmSet, setting pwm of fan
    {
        pwm_Set(MSG[12]);  //set pwm of fan

		//assemble response message
        Returning_MSG[0] = RES;
        Returning_MSG[1] = 0x0A;
        Returning_MSG[2] = SUCCESS;
        Returning_MSG[3] = 0x0A;
        Returning_MSG[4] = fan;
        Returning_MSG[5] = 0x0A;
        Returning_MSG[6] = fan_PwmSet;
        Returning_MSG[7] = MSG[12];
        Returning_MSG[8] = 0x0A;
        Returning_MSG[9] = END1;
        Returning_MSG[10] = END2;

        //response_MSG(Returning_MSG);  
		
		return Returning_MSG ;//return assembled response message
    }
}


char MSG_Analyzer::FAN::get_Command(char MSG[])
{
    return MSG[11];  //currently, command is at index number 11 of received message
}

void MSG_Analyzer::FAN::pwm_Set(char pwm)  // set pwm of fan
{
    analogWrite(fan_Pin, 255-(pwm*2));
}




