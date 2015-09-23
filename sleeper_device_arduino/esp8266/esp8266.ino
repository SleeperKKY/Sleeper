#include <SoftwareSerial.h>
#include <Sleeper_Library.h>  // include 'Sleeper' library
SoftwareSerial mySerial(10,11);//=SoftwareSerial(10,11); // RX, TX


MSG_Analyzer processing;  // declare message analyzer from Sleeper library
char Receiving[30];  // wifi received message buffer with 30byte 
int MSG_Count = 0;  // received message byte count

void init_WIFI() ;//wifi initialize
void send_WIFI(char msg[]) ;//send message via wifi

void setup()
{
  Serial.begin(9600);  // open serial for desktop debug
  mySerial.begin(9600); //software serial , connected to wifi TX,RX
  
  pinMode(fan_Pin, OUTPUT);  // set fan pwm pin

  init_WIFI();  // initialize wifi module
  
}

void loop()
{
  Receiving[0] = '\0';  //Receving[0[ indicate indicator for receving 'valid data' from module
  MSG_Count = 0;  //message count to 0 if nothing received

  while(mySerial.available())  // if got something from wifi
  {
      char MSG = mySerial.read();  // read 1 byte
      Receiving[0] = MSG;  // store indicator
      
      if(Receiving[0] == 0x50)  //if indicator is 'p' of IPD message from wifi module
      {
        //Serial.print(" ");
        Serial.print(MSG, HEX);  // print to monitor for debug
        //Serial.print(MSG);
        Serial.print(" ");

        ++MSG_Count;  // increment message count
        
        if((Receiving[1] == 0x44) && (MSG_Count == 14))  //if read 15 bytes and 'D' of IPD
        {
          send_WIFI(processing.analize_MSG(Receiving));  // analyze message and return response message and send back to phone
          //processing.analize_MSG(Receiving);
          Serial.println("Message received");  // print received message to monitor
          break;
        }
      }
  }
}

/**
*Initialize wifi module with at command
*/
void init_WIFI()
{
  mySerial.write("AT+RST");  // reset at command
  mySerial.write(0x0A);
  mySerial.write(0x0D);
  delay(5000);

  mySerial.write("AT+CWMODE=3");  // set wifi module as station-ap mode
  mySerial.write(0x0A);
  mySerial.write(0x0D);
  delay(500);

  mySerial.write("AT+CIPMUX=1");  // set wifi module to be multi-connectable
  mySerial.write(0x0A);
  mySerial.write(0x0D);
  delay(500);

  mySerial.write("AT+CWSAP=\"Sleeper\",\"1234567890\",11,4");  // set 'ssid', 'password', 'channel', 'crypto type'
  /* Crypto type
   * 0 = NONE
   * 2 = WPA_PSK
   * 3 = WPA2_PSK
   * 4 = WPA_WPA2_PSK
  */
  mySerial.write(0x0A);
  mySerial.write(0x0D);
  delay(500);

  mySerial.write("AT+CIPSERVER=1,2323");  // open tcp socket server with id 1, port 2323
  mySerial.write(0x0A);
  mySerial.write(0x0D);
  delay(500);

  mySerial.write("AT+CIFSR");  // get local ip, always '192.168.4.1' in server mode
  mySerial.write(0x0A);
  mySerial.write(0x0D);
  delay(500);
}

/**
* send message via wifi module
* @param msg message to send
*/
void send_WIFI(char* msg)
{
    delay(100);


    mySerial.write("AT+CIPSEND=0,10"); // notify module that message length is 10 byte
    mySerial.write(0x0A);
    mySerial.write(0x0D);
    delay(100);

    mySerial.write(msg);  // send message
    mySerial.write(0x0A);
    mySerial.write(0x0D);
    delay(100);
}

