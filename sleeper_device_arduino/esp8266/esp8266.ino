#include <Sleeper_Library.h>  // '슬리퍼' 라이브러리를 포함합니다.

MSG_Analyzer processing;  // '슬리퍼' 라이브러리의 메시지 처리 클래스를 생성합니다.
char Receiving[30];  // 수신 메시지를 30바이트 크기로 선언합니다.
int MSG_Count;  // 수신 메시지의 카운트를 위한 

void setup()
{
  Serial.begin(9600);  // 모니터 디버깅을 위한 시리얼 포트를 엽니다.
  Serial1.begin(9600);  // 와이파이 모듈과 통신을 위한 시리얼 포트를 엽니다.
  
  pinMode(fan_Pin, OUTPUT);  // 선풍기 제어 핀 넘버를 출력으로 선언합니다.

  init_WIFI();  // '슬리퍼' 라이브러리의 와이파이 초기화 메서드를 실행합니다.
}

void loop()
{
  Receiving[0] = '\0';  // 수신 신호가 없을 때마다 수신 메시지를 초기화합니다.
  MSG_Count = 0;  // 수신 신호가 없을 때마다 수신 메시지 카운트 값을 초기화합니다.

  while(Serial1.available())  // 와이파이 모듈로부터 수신 신호가 들어올 때,
  {
      char MSG = Serial1.read();  // 수신 메시지를 바이트 단위로 읽어서,
      Receiving[MSG_Count] = MSG;  // 수신 메시지 버퍼에 저장합니다.
      
      if(Receiving[0] == 0x50)  // 수신 메시지에 아스키코드 0x50에 해당하는 알파벳 'P'가 포함될 때부터 메시지를 카운트합니다.
      {
        Serial.print(" ");
        Serial.print(MSG, HEX);  // 디버깅 용으로 읽어들이는 수신 메시지를 확인하기위해 16진수로 모니터에 출력합니다.
        Serial.print(" ");

        ++MSG_Count;  // 15바이트만 읽어들이기 위해서 수신 메시지의 카운트를 시작합니다.
        
        if(MSG_Count == 14)  // 수신 메시지를 15개 모았을 때,
        {
          send_WIFI(processing.analize_MSG(Receiving));  // '슬리퍼' 라이브러리의 'analize_MSG()' 메서드에 매개변수로 전달해 줍니다.
          Serial.println("Message received");  // 수신 메시지를 메시지 처리 클래스에 넘겨주었음을 모니터에 나타내고,
          break;  // 현재 수신 메시지에 대한 처리 분기를 벗어납니다.
        }
      }
  }
}

