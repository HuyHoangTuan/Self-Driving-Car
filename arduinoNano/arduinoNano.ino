#include <Servo.h>
#include <Arduino.h>
#include <Wire.h>
#include <HMC5883L_Simple.h>
#include <NewPing.h>

#define servoPin 12

#define DPin A0
#define UPin A1
#define LPin A2
#define RPin A3

#define SPD 30
/// 30 cm/s
#define TURNSPD 3.3
/// 3.3s

/// Servo
Servo servo;

/// exp
long EXP = 2000;

/// compass
HMC5883L_Simple Compass;
long compassTimer = 0;
long currentCompassTimer = 0;
double currentAngle = 0;

/// send-data
long sendingTimer = 0;

/// sensors
long MAX_DIS = 300;
NewPing sensor01(3,4, MAX_DIS);
NewPing sensor02(5,6, MAX_DIS);
NewPing sensor03(7,8, MAX_DIS);
NewPing sensor04(9,10, MAX_DIS);

void setup()
{
    Serial.begin(9600);
    Serial.setTimeout(5);
    Wire.begin();

    /// Compass
    Compass.SetDeclination(23, 35, 'E');
    Compass.SetSamplingMode(COMPASS_SINGLE);
    Compass.SetScale(COMPASS_SCALE_088);
    Compass.SetOrientation(COMPASS_HORIZONTAL_X_NORTH);
    /// Servo
    servo.attach(servoPin);
    servo.write(0);

    /// Control
    pinMode(UPin, OUTPUT);
    pinMode(DPin, OUTPUT);
    pinMode(LPin, OUTPUT);
    pinMode(RPin, OUTPUT);

    delay(100);
}
char msg[100];
void Data2Wifi(long D1, long A1, long D2, long A2, long D3, long A3, long D4, long A4, long A)
{

    sprintf(msg, "%ld %ld %ld %ld %ld %ld %ld %ld %ld\n", D1, A1, D2, A2, D3, A3, D4, A4, A);
    Serial.write(msg);
}

void Cal_Angle()
{
    currentAngle = Compass.GetHeadingDegrees();
}
void controlL(int Lpin, int Rpin)
{
    analogWrite(Rpin, 0);
    analogWrite(Lpin, 255);
}
void controlR(int Rpin, int Lpin)
{
    analogWrite(Lpin, 0);
    analogWrite(Rpin, 255);
}

/// Car
long CurrentTimerSPD=0, TimerSPD=0;
bool check = false;
void controlSPD(int pin, int restTime, int actTime)
/// restTime =30  -> strange
/// actTime = 4

/// restTime = 13 -> turn
/// actTime = 5
{
    CurrentTimerSPD = millis();
    if (CurrentTimerSPD - TimerSPD >= actTime && check == true)
    {
        analogWrite(pin, 0);
        check = false;
        TimerSPD = CurrentTimerSPD;
    }
    else if (CurrentTimerSPD - TimerSPD >= restTime && check == false)
    {
        analogWrite(pin, 255);
        check = true;
        TimerSPD = CurrentTimerSPD;
    }
}


String path = "", times = "";
String mess = "";
int instruction = 0;
int pos_Times = 0, pos_Path = 0;
long TIMES = 0, PATH = 0;
long runTime = 0, currentRunTime = 0;

char buff[200];
int i=0;
bool reverse = false;

long servoTimer = 0, currentServoTimer = 0;

long distance01 = 0;
long distance02 = 0;
long distance03 = 0;
long distance04 = 0;
unsigned long currentSensorTimer = 0, sensorTimer = 0;

void loop()
{
    currentSensorTimer = millis();
    
    /// Receive message
    mess = "";
    if (Serial.available()) mess = Serial.readString();
    if (mess.length() >0)
    {
        bool t = false, p = false;
        instruction = 0;
        for (char c: mess)
        {
            if (c == '\n' || c==':' || c==',' || c=='|')
            {
                if (c == '\n') {t=false; p=false;break;}
                else if (c == ':' && instruction == 1) {path=""; times=""; pos_Times = 0; pos_Path=0; p=true;}
                else if (c == ':' && instruction >1) {path+=" "; times+=" "; p=true;}
                else if (c == ',' && p==true) {path += " ";}
                else if (c == ',' && t==true) {times += " ";}
                else if (c == '|') {p=false; t=true;}
                continue;
            }
            if (p == false && t == false) instruction = instruction*10 +(c-'0');
            else if (p == true) path+=c;
            else if (t == true) times+=c;
        }
        Serial.print(path);
        Serial.print(",");
        Serial.println(times);
    }

    /// seperate message
    currentRunTime = millis();
    if (currentRunTime - runTime >= (double) TIMES/SPD)
    {
        TIMES = 0;
        PATH = 0;
        while (pos_Times < times.length() && times[pos_Times] >='0' && times[pos_Times] <= '9')
        {
            TIMES = TIMES*10 + (times[pos_Times]-'0');
            pos_Times++;
        }
        while (pos_Times < times.length() && (times[pos_Times] <'0' || times[pos_Times] > '9')) pos_Times++;

        while (pos_Path < path.length() && path[pos_Path] >='0' && path[pos_Path] <= '9')
        {
            PATH = PATH*10 + (path[pos_Path]-'0');
            pos_Path++;
        }
        while (pos_Path < path.length() && (path[pos_Path] < '0' || path[pos_Path] > '9')) pos_Path++;
        TIMES*=1000;
       
        runTime = currentRunTime;
    }
    
    if (PATH != 0 )
    {
       if (PATH !=8) analogWrite(UPin, 0);
       if (PATH !=2) analogWrite(DPin, 0);
       if (PATH !=6) analogWrite(RPin, 0);
       if (PATH !=4) analogWrite(LPin, 0);
       if (PATH == 8) 
       {
            controlSPD(UPin, 30, 4);
       }
       else if (PATH == 2)
       {
            controlSPD(DPin, 30, 4); 
       }
       else if(PATH == 6)
       {
            controlR(RPin, LPin);
            analogWrite(UPin, 255);
       }
       else if(PATH == 4)
       {
            controlL(LPin, RPin);
            analogWrite(UPin, 255);
       }
    }
    else
    {
        analogWrite(DPin, 0);
        analogWrite(UPin, 0);
        analogWrite(RPin, 0);
        analogWrite(LPin, 0);
    }
    
    /// Car Angle
    Cal_Angle();

    /// Servo
    currentServoTimer = millis();
    if (currentServoTimer - servoTimer >=10)
    {
        long angle01 = -i*90 ;
        long angle02 = angle01 - 90*100;
        long angle03 = angle01 + 90*100;
        long angle04 = angle01 + 180*100;
        
        servo.write(i);
        
        /// Sensors
        if(currentSensorTimer - sensorTimer>=200)
        {
            distance01 = sensor01.ping_cm();
            distance02 = sensor02.ping_cm();
            distance03 = sensor03.ping_cm();
            distance04 = sensor04.ping_cm();
            ///Data2Wifi(distance01, angle01, distance02, angle02, distance03, angle03, distance04, angle04, currentAngle);
            sensorTimer = currentSensorTimer;
        }

        i= i + ((reverse == true) ? -1 : 1);
        if (i == 100) reverse = true;
        else if (i == 0) reverse = false;
        servoTimer = currentServoTimer;
    }
    
    
    
    
  
}
