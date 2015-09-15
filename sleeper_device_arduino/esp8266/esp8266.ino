#include <SoftwareSerial.h>
#include <Sleeper_Library.h>  // '슬리퍼' 라이브러리를 포함합니다.
SoftwareSerial mySerial(10,11);//=SoftwareSerial(10,11); // RX, TX

//#include <Sleeper_Library.h>  // '슬리퍼' 라이브러리를 포함합니다.


MSG_Analyzer processing;  // '슬리퍼' 라이브러리의 메시지 처리 클래스를 생성합니다.
char Receiving[30];  // 수신 메시지를 30바이트 크기로 선언합니다.
int MSG_Count = 0;  // 수신 메시지의 카운트를 위한 

void init_WIFI() ;
void send_WIFI(char msg[]) ;

void setup()
{
  Serial.begin(9600);  // 모니터 디버깅을 위한 시리얼 포트를 엽니다.
  mySerial.begin(9600);
  
  pinMode(fan_Pin, OUTPUT);  // 선풍기 제어 핀 넘버를 출력으로 선언합니다.

  init_WIFI();  // '슬리퍼' 라이브러리의 와이파이 초기화 메서드를 실행합니다.
  
}

void loop()
{
  Receiving[0] = '\0';  // 수신 신호가 없을 때마다 수신 메시지를 초기화합니다.
  MSG_Count = 0;  // 수신 신호가 없을 때마다 수신 메시지 카운트 값을 초기화합니다.

  while(mySerial.available())  // 와이파이 모듈로부터 수신 신호가 들어올 때,
  {
      char MSG = mySerial.read();  // 수신 메시지를 바이트 단위로 읽어서,
      Receiving[MSG_Count] = MSG;  // 수신 메시지 버퍼에 저장합니다.
      
      if(Receiving[0] == 0x50)  // 수신 메시지에 아스키코드 0x50에 해당하는 알파벳 'P'가 포함될 때부터 메시지를 카운트합니다.
      {
        //Serial.print(" ");
        Serial.print(MSG, HEX);  // 디버깅 용으로 읽어들이는 수신 메시지를 확인하기위해 16진수로 모니터에 출력합니다.
        //Serial.print(MSG);
        Serial.print(" ");

        ++MSG_Count;  // 15바이트만 읽어들이기 위해서 수신 메시지의 카운트를 시작합니다.
        
        if((Receiving[1] == 0x44) && (MSG_Count == 14))  // 수신 메시지를 15개 모았을 때,
        {
          MSG_Count = 0;  // 수신 신호가 없을 때마다 수신 메시지 카운트 값을 초기화합니다.
          send_WIFI(processing.analize_MSG(Receiving));  // '슬리퍼' 라이브러리의 'analize_MSG()' 메서드에 매개변수로 전달해 줍니다.
          //processing.analize_MSG(Receiving);
          Serial.println("Message received");  // 수신 메시지를 메시지 처리 클래스에 넘겨주었음을 모니터에 나타내고,
          break;  // 현재 수신 메시지에 대한 처리 분기를 벗어납니다.
        }
      }
  }
}

void init_WIFI()  // 아두이노 구동 후 초기 와이파이 모듈 초기화를 수행합니다.
{
  mySerial.write("AT+RST");  // 이전 설정을 지우기 위해 리셋합니다.
  mySerial.write(0x0A);
  mySerial.write(0x0D);
  delay(5000);

  mySerial.write("AT+CWMODE=3");  // 와이파이 모듈을 클라이언트-서버 동시 모드로 설정합니다.
  mySerial.write(0x0A);
  mySerial.write(0x0D);
  delay(500);

  mySerial.write("AT+CIPMUX=1");  // 와이파이 모듈을 다중연결 모드로 설정합니다.
  mySerial.write(0x0A);
  mySerial.write(0x0D);
  delay(500);

  mySerial.write("AT+CWSAP=\"Sleeper\",\"1234567890\",11,4");  // '시드명', '비밀번호', '통신채널', '암호화'를 설정합니다.
  /* 암호화 변수에 따른 암호화 방식
   * 0 = 비암호화
   * 2 = WPA_PSK
   * 3 = WPA2_PSK
   * 4 = WPA_WPA2_PSK
  */
  mySerial.write(0x0A);
  mySerial.write(0x0D);
  delay(500);

  mySerial.write("AT+CIPSERVER=1,2323");  // 아두이노를 서버로 열고('1'), 포트번호를 '2323'으로 설정합니다.
  mySerial.write(0x0A);
  mySerial.write(0x0D);
  delay(500);

  mySerial.write("AT+CIFSR");  // 로컬 IP주소를 획득합니다. 서버의 경우 항상 '192.168.4.1'의 IP주소를 가집니다.
  mySerial.write(0x0A);
  mySerial.write(0x0D);
  delay(500);
}

void send_WIFI(char* msg)// 와이파이 모듈을 이용하여 메세지를 보냅니다.
{
    delay(100);


    mySerial.write("AT+CIPSEND=0,10");  // 안드로이드 폰에 보낼 메시지가 10자리임을 와이파이 모듈에 알려줍니다.
    mySerial.write(0x0A);
    mySerial.write(0x0D);
    delay(100);

    mySerial.write(msg);  // 명령받은 메시지에 대한 처리결과를 안드로이드 폰에 응답으로 보내줍니다.
    mySerial.write(0x0A);
    mySerial.write(0x0D);
    delay(100);
    
    /*
    Serial.println("Send Start!");
    for(int i=0; i<10; i++)
    {
      Serial.print(msg[i], HEX);
      Serial.print(" ");
    }
    Serial.println("Send OK");
    */
}

