#include <Arduino.h> //arduino library

#define fan_Pin 3  // fan controlling pin

#define REQ 0x00  //request message header
#define RES 0x01 //response message header
#define SUCCESS 0x02
#define FAIL 0x03
#define END1 0x7E
#define END2 0xE7

#define tmp 0x10  // temperature sensor id
#define tmp_Get 0x11

#define fan 0x20  // fan id
#define fan_PwmSet 0x21 // fan pwm set command

class MSG_Analyzer  // message analyzer
{                   // each devices' message analyzer is inner class of this class
private:
    int deviceID = 0;
    char toSend_MSG[30];

public:
    MSG_Analyzer();
    char* analize_MSG(char MSG[]);




    class THERMOMETER  // temperature sensor message analyzer
    {
    private:
        char command = 0;
        char Returning_MSG[30];

    public:
        char* MSG_DIVISION(char MSG[]);
        char get_Command(char MSG[]);
        char* response_MSG(char MSG[]);
        char get_Temperature();
    };



    class FAN  // fan message analyzer
    {
    private:
        char command = 0;
        char Returning_MSG[30];
		
    public:
		char get_Command(char MSG[]);
        char* MSG_DIVISION(char MSG[]);
        char* response_MSG(char MSG[]);
        void pwm_Set(char pwm);
    };


};



