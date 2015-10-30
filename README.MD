##Overview

##Arudino Circuit
![Arduino Circuit](./README_IMG/Arduino_Circuit.jpg)

##Note

 - You have to put 'clMessage.h', 'clMessage.cpp' and make it as library as above too. Go to your arduino directory
   like "...\Arduino\libraries\" and make 'SleeperLib' folder. Put above module files inside 'SleeperLib' folder and restart
    arduino ide. Now you can use it as arduino library.
	
 - You also have to download 'ESP8266.h', 'ESP8266.cpp' from https://github.com/itead/ITEADLIB_Arduino_WeeESP8266
    and put them in "...\Arduino'\libraries\ESP8266". You can change folder name 'ESP8266' to one you like. 
    This module is for communicating with esp8266 with Arduino. This module gives send, receive interface. 
    And our 'clMessage' will parse data into meaningful data unit.
	
 - It is compatible with our android application library SleeperApp's sleeperLib.jar's clComManager class.
	
	
	
	  