
#include "Sleeper_Library.h"


/* ESP8266-01 와이파이 모듈이 시리얼 통신으로 해석하는 명령어는 아래 형식과 같아야합니다.
 *
 * Serial1.write('COMMAND');  아두이노가 와이파이 모듈로 전송하는 명령어가 담깁니다.
 *
 * Serial1.write(0x0A);  'Enter'키 입력을 위해 'Carriage return'에 해당하는 '0x0A'와,
 * Serial1.write(0x0D);  'Line feed'에 해당하는 '0x0D'를 마무리에 함께 전송합니다.
 *
 * delay(100);
 *   - 와이파이 모듈은 아두이노가 보낸 메시지를 그대로 돌려주는 오류검사를 하기에
 *     이에 알맞은 지연시간을 설정합니다.
 *
*/


void init_WIFI()  // 아두이노 구동 후 초기 와이파이 모듈 초기화를 수행합니다.
{
  Serial1.write("AT+RST");  // 이전 설정을 지우기 위해 리셋합니다.
  Serial1.write(0x0A);
  Serial1.write(0x0D);
  delay(100);

  Serial1.write("AT+CWMODE=3");  // 와이파이 모듈을 클라이언트-서버 동시 모드로 설정합니다.
  Serial1.write(0x0A);
  Serial1.write(0x0D);
  delay(100);

  Serial1.write("AT+CIPMUX=1");  // 와이파이 모듈을 다중연결 모드로 설정합니다.
  Serial1.write(0x0A);
  Serial1.write(0x0D);
  delay(100);

  Serial1.write("AT+CWSAP=\"Sleeper\",\"1234567890\",11,4");  // '시드명', '비밀번호', '통신채널', '암호화'를 설정합니다.
  /* 암호화 변수에 따른 암호화 방식
   * 0 = 비암호화
   * 1 = WPA_PSK
   * 2 = WPA2_PSK
   * 3 = WPA_WPA2_PSK
  */
  Serial1.write(0x0A);
  Serial1.write(0x0D);
  delay(100);

  Serial1.write("AT+CIPSERVER=1,2323");  // 아두이노를 서버로 열고('1'), 포트번호를 '2323'으로 설정합니다.
  Serial1.write(0x0A);
  Serial1.write(0x0D);
  delay(100);

  Serial1.write("AT+CIFSR");  // 로컬 IP주소를 획득합니다. 서버의 경우 항상 '192.168.4.1'의 IP주소를 가집니다.
  Serial1.write(0x0A);
  Serial1.write(0x0D);
  delay(100);
}

void send_WIFI(char msg[]) ;// 와이파이 모듈을 이용하여 메세지를 보냅니다.
{

	delay(100);

    Serial1.write("AT+CIPSEND=0,10");  // 안드로이드 폰에 보낼 메시지가 10자리임을 와이파이 모듈에 알려줍니다.
    Serial1.write(0x0A);
    Serial1.write(0x0D);
    delay(100);

    Serial1.write(msg);  // 명령받은 메시지에 대한 처리결과를 안드로이드 폰에 응답으로 보내줍니다.
    Serial1.write(0x0A);
    Serial1.write(0x0D);
    delay(100);

}


//////////////////// DiviceID distinguish partion ////////////////////

MSG_Analyzer::MSG_Analyzer()
{
    // 'MSG_Analyzer'의 생성자를 설정해두어야 아두이노에서 미리 인스턴스로 생성될 수 있습니다.
}

char* MSG_Analyzer::analize_MSG(char MSG[])  // 아두이노가 모은 15바이트의 수신 메시지는 'analize_MSG()'가 제일 처음 분석합니다.
{

    this->deviceID = MSG[9];  // 수신 메시지의 10번째 자리에 해당하는 'MSG[9]'는 'deviceID' 값을 담고 있습니다.

    if(deviceID == tmp)  // 'deviceID'가 온도계에 해당한다면,
    {
        THERMOMETER buf;  // 온도계 클래스의 인스턴스를 생성하고.
        return buf.MSG_DIVISION(MSG);  // 온도계에 맞는 메시지 처리 분기를 거칩니다.
    }
    if(deviceID == fan)  // 'deviceID'가 선풍기에 해당한다면,
    {
        FAN buf;  // 선풍기 클래스의 인스턴스를 생성하고,
        return buf.MSG_DIVISION(MSG);  // 선풍기에 맞는 메시지 처리 분기를 거칩니다.
    }
}



//////////////////// Thermometer analysis partion ////////////////////

char* MSG_Analyzer::THERMOMETER::MSG_DIVISION(char MSG[])  // 'deviceID'가 온도계로 분류되었을 때,
{
    command = this->get_Command(MSG);  // 어떤 명령어를 수행할지 해석합니다.


    if(command == tmp_Get)  // 온도센서가 측정한 온도를 알려달라는 명령어에 대한 응답 메시지를 조립합니다.
    {
        Returning_MSG[0] = RES;
        Returning_MSG[1] = 0x0A;
        Returning_MSG[2] = SUCCESS;
        Returning_MSG[3] = 0x0A;
        Returning_MSG[4] = tmp;
        Returning_MSG[5] = 0x0A;
        Returning_MSG[6] = tmp_Get;
        Returning_MSG[7] = get_Temperature();
        Returning_MSG[8] = 0x0A;
        Returning_MSG[9] = END1;
        Returning_MSG[10] = END2;

        //response_MSG(Returning_MSG);  // 조립한 메시지를 안드로이드 폰에 응답하는 메서드에 넘겨줍니다.
		
		return Returning_MSG ;
    }
}

char MSG_Analyzer::THERMOMETER::get_Command(char MSG[])
{
    return MSG[11];  // 온도계 명령어 위치에 맞는 메시지를 반환합니다.
}

/*
void MSG_Analyzer::THERMOMETER::response_MSG(char MSG[])
{
    delay(100);

    Serial1.write("AT+CIPSEND=0,10");  // 안드로이드 폰에 보낼 메시지가 10자리임을 와이파이 모듈에 알려줍니다.
    Serial1.write(0x0A);
    Serial1.write(0x0D);
    delay(100);

    Serial1.write(MSG);  // 명령받은 메시지에 대한 처리결과를 안드로이드 폰에 응답으로 보내줍니다.
    Serial1.write(0x0A);
    Serial1.write(0x0D);
    delay(100);
}
*/

char MSG_Analyzer::THERMOMETER::get_Temperature()  // 아두이노와 연결된 온도센서가 아날로그 신호에서 섭씨를 측정해 반환합니다.
{
    int value = analogRead(0);
    float voltage = (value/1024.0)*5000;
    float Celsius = voltage / 10;

    return Celsius;
}



//////////////////// Fan analysis partion ////////////////////

char* MSG_Analyzer::FAN::MSG_DIVISION(char MSG[])  // 'deviceID'가 선풍기로 분류되었을 때,
{
    command = this->get_Command(MSG);

    if(command == fan_PwmSet)  // 수신한 명령어가 PWM 방법을 통한 선풍기 풍량 세기 변경일 때의 응답 메시지를 조립합니다.
    {
        pwm_Set(MSG[12]);  // PWM 데이터를 풍량 세기를 변경하는 메서드에 전달합니다.

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
		
		return Returning_MSG ;// 조립한 메시지를 안드로이드 폰에 응답하는 메서드에 넘겨줍니다.
    }
}


char MSG_Analyzer::FAN::get_Command(char MSG[])
{
    return MSG[11];  // 선풍기 명령어 위치에 맞는 메시지를 반환합니다.
}

/*
void MSG_Analyzer::FAN::response_MSG(char MSG[])
{
    delay(100);

    Serial1.write("AT+CIPSEND=0,10");  // 안드로이드 폰에 보낼 메시지가 10자리임을 와이파이 모듈에 알려줍니다.
    Serial1.write(0x0A);
    Serial1.write(0x0D);
    delay(100);

    Serial1.write(MSG);  // 명령받은 메시지에 대한 처리결과를 안드로이드 폰에 응답으로 보내줍니다.
    Serial1.write(0x0A);
    Serial1.write(0x0D);
    delay(100);
}
*/

void MSG_Analyzer::FAN::pwm_Set(char pwm)  // 미리 정의된 선풍기 핀 넘버에 pwm 신호를 주어 세기를 조절합니다.
{
    analogWrite(fan_Pin, 255-(pwm*2));
}




