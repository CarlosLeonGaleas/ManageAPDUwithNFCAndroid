# NFC QUESTIONNAIRE SYSTEM
### Developer: Carlos León Galeas  
### Departamento de Investigación - Instituto Tecnológico Superior Universitario Rumiñahui
#### Resume
Android application that uses **NFC technology** to communicate with an electronic circuit that sends a questionnaire of 5 questions.  
The app uses the **Host Card Emulation (HCE)** feature to receive and respond to different **APDU commands**.

**Useful information:** https://developer.android.com/develop/connectivity/nfc/hce?hl=es-419

## App Screens
### Register/Login Screen

<img width="300" height="600" alt="Login Screen" src="https://github.com/user-attachments/assets/0afe5106-ac3c-49fb-850b-df91e029e5cb" />

### Questionnaire Screen (Video)

[![Watch the video](https://github.com/user-attachments/assets/abcd1234-thumbnail.png)](https://github.com/user-attachments/assets/af90cd3a-344e-4483-a487-b419a592d7b1)

## Electronic Circuit
### Electronic Circuit - Diagram
Initially, esp8266 was used, but it was later replaced by esp32.

<img width="634" height="514" alt="NFC-Circuit" src="https://github.com/user-attachments/assets/7fd1cdfd-0dae-47b6-9742-ac9e42117314" />

### Electronic Circuit - Prototype
<img width="634" height="514" alt="NFC-Circuit" src="https://github.com/user-attachments/assets/2e6082cc-2b79-4be6-9bd2-9418a65a0009" />

## More details

* The app and the electronic circuit use a specific communication protocol with a defined header and structure so they can understand each other.

* The app works through different phases to control communication flow — for example: Logging In, Questionnaire Needed, Confirming Questionnaire, etc.

* The circuit has a CSV file with questions stored on the SD card. It randomly selects 5 and sends them when the app requests them.

* The app calculates and shows the number of correct answers once the phone connects to the circuit. It also stores the user data and score on the SD card.
