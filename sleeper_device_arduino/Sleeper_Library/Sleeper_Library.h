#include <Arduino.h>  // 아두이노 관련 메서드 처리를 위해 '슬리퍼' 라이브러리에 포함시킵니다.


#define fan_Pin 3  // 아두이노가 선풍기를 조작하는 핀 넘버를 설정합니다.

#define REQ 0x00  // 메시지에 관련된 전처리 상수를 설정합니다.
#define RES 0x01
#define SUCCESS 0x02
#define FAIL 0x03
#define END1 0x7E
#define END2 0xE7

#define tmp 0x10  // 전처리 중 '온도계'와 관련된 상수를 설정합니다.
#define tmp_Get 0x11

#define fan 0x20  // 전처리 중 '선풍기'와 관련된 상수를 설정합니다.
#define fan_PwmSet 0x21



void init_WIFI();  // 와이파이 모듈 초기화를 위한 설정이 포함되어 있습니다.
void send_WIFI(char msg[]) ;// 와이파이 모듈을 이용하여 메세지를 보냅니다.


class MSG_Analyzer  // 아두이노가 수신한 15바이트 수신 메시지를 처리하는 클래스입니다.
{                   // 아두이노와 연결된 모듈의 처리를 중첩 클래스로 처리하는 구조입니다.
private:
    int deviceID = 0;
    char worked_MSG[30];

public:
    MSG_Analyzer();
    void analize_MSG(char MSG[]);




    class THERMOMETER  // 수신한 메시지가 연결된 온도계에 관련된 명령일 때 처리하는 클래스입니다.
    {
    private:
        char command = 0;
        char Returning_MSG[30];

	private:
        void MSG_DIVISION(char MSG[]);
        char get_Command(char MSG[]);
        void response_MSG(char MSG[]);
        char get_Temperature();
    };



    class FAN  // 수신한 메시지가 연결된 선풍기에 관련된 명령일 때 처리하는 클래스입니다.
    {
    private:
        char command = 0;
        char Returning_MSG[30];
		
	private:
		char get_Command(char MSG[]);
		void MSG_DIVISION(char MSG[]);     
        void response_MSG(char MSG[]);
        void pwm_Set(char pwm);
    };


};



