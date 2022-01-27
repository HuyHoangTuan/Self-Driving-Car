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
#define SPD 50
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
double firstAngle = 0;

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
    Compass.SetDeclination(-1, 41, 'W');
    Compass.SetSamplingMode(COMPASS_SINGLE);
    Compass.SetScale(COMPASS_SCALE_130);
    Compass.SetOrientation(COMPASS_HORIZONTAL_X_NORTH);
    delay(1000);
    firstAngle = Compass.GetHeadingDegrees();
    currentAngle = firstAngle;
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
void Data2Wifi(long D1, long A1, long D2, long A2, long D3, long A3, long D4, long A4, long A,long x, long y)
{

    sprintf(msg, "%ld %ld %ld %ld %ld %ld %ld %ld %ld %ld %ld\n", D1, A1, D2, A2, D3, A3, D4, A4, A, x, y);
    Serial.write(msg);
}

void Cal_Angle()
{
    currentAngle = Compass.GetHeadingDegrees();
}
/// Car
long CurrentTimerSPD=0, TimerSPD=0;
bool check = false;
void controlSPD(int pin, int restTime, int actTime)
/// restTime =28  -> strange
/// actTime = 5

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
double currentX = 0, currentY = 0;
long curDir = 0;
double straight = -1;
double tempX = 0, tempY= 0;
double an = 0, extraX = 0, extraY = 0;
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
    }

    /// seperate message
    currentRunTime = millis();
    
    
    /// Car Angle
    Cal_Angle();
    if (currentRunTime - runTime >= (double) TIMES/SPD)
    {
        
        if(PATH == 6) curDir = (curDir + 90 + 360)%360;
        if(PATH == 4) curDir = (curDir - 90 + 360)%360;

        
        /*if(curDir == 0 && PATH!=0) currentX = currentX - (TIMES/1000);
        else if(curDir == 90 && PATH!=0)
        {
            currentY = currentY+90;
            currentX = currentX+ straight*90;
        }
        else if(curDir == 180 && PATH!=0)
        {
            currentX = currentX + (TIMES/1000);
        }
        else if(curDir == 270 && PATH!=0)
        {
            currentY = currentY - 90;
            currentX = currentX + straight*90;
        }*/
        currentX = tempX;
        currentY = tempY;
        if(PATH == 4)
        {
            firstAngle=firstAngle-57;
            if(firstAngle < 0) firstAngle+=360.0;
        }
        else if (PATH == 6)
        {
            firstAngle=firstAngle+57;
            if(firstAngle >=360) firstAngle-=360.0;
        }
        
        tempX = currentX;
        tempY = currentY;

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

        if(PATH == 4)
        {
            analogWrite(RPin, 0);
            analogWrite(LPin, 255);
        }
        else if (PATH == 6)
        {
            analogWrite(LPin, 0);
            analogWrite(RPin, 255);
        }
        runTime = currentRunTime;
    }
    else
    {
        tempX = currentX;
        tempY = currentY;
        if(PATH == 8)
        {
            if( curDir ==0) tempX = currentX - (((currentRunTime- runTime)*SPD) / 1000);
            else if (curDir ==90) tempY = currentY + (((currentRunTime- runTime)*SPD) / 1000);
            else if (curDir == 180) tempX = currentX + (((currentRunTime- runTime)*SPD) / 1000);
            else if (curDir == 270) tempY = currentY - (((currentRunTime- runTime)*SPD) / 1000);
        }
        else if (PATH == 5)
        {
              if( curDir ==0) tempX = currentX + (((currentRunTime- runTime)*SPD) / 1000);
              else if (curDir ==90) tempY = currentY - (((currentRunTime- runTime)*SPD) / 1000);
              else if (curDir == 180) tempX = currentX - (((currentRunTime- runTime)*SPD) / 1000);
              else if (curDir == 270) tempY = currentY + (((currentRunTime- runTime)*SPD) / 1000);
        }
        else if (PATH == 4 || PATH ==6)
        {   
            if(currentRunTime - runTime <= (double) 20/SPD) analogWrite(DPin, 255);
            else analogWrite(DPin, 0);
            an = 0.0;
            extraX = 0.0;
            extraY = 0.0;
            if(currentAngle > firstAngle)
            {
                if(currentAngle - firstAngle >90)
                {
                    an = 360-currentAngle+firstAngle;
                    an = an*PI/180.0;
                    extraX = sin(an)*90;
                    extraY = 90-cos(an)*90;
                }
                else
                {
                    an = currentAngle - firstAngle;
                    an = an*PI/180.0;
                    extraX = sin(an)*90;
                    extraY = 90-cos(an)*90;
                }
            }
            else
            {
                if(firstAngle - currentAngle > 90)
                {
                    an = 360-firstAngle + currentAngle;
                    an = an*PI/180.0;
                    extraX = sin(an)*90;
                    extraY = 90-cos(an)*90;
                }
                else
                {
                    an = firstAngle - currentAngle;
                    an = an*PI/180.0;
                    extraX = sin(an)*90;
                    extraY = 90-cos(an)*90;
                }
            }
            
            if(extraX >=0 && extraY>=0)
            {
                if(curDir == 0)
                {
                    if(PATH == 4)
                    {
                        tempX = currentX - extraX;
                        tempY = currentY - extraY;
                    }
                    else if (PATH == 6)
                    {
                        tempX = currentX - extraX;
                        tempY = currentY + extraY;
                    }
                }
                else if (curDir == 90)
                {
                    if(PATH == 4)
                    {
                        tempX = currentX - extraY;
                        tempY = currentY + extraX;
                    }
                    else if (PATH == 6)
                    {
                        tempX = currentX + extraY;
                        tempY = currentY + extraX;
                    }
                }
                else if (curDir == 180)
                {
                    if(PATH == 4)
                    {
                        tempX = currentX + extraX;
                        tempY = currentY + extraY;
                    }
                    else if(PATH == 6)
                    {
                        tempX = currentX + extraX;
                        tempY = currentY - extraY;
                    }
                }
                else if(curDir == 270)
                {
                    if(PATH == 4)
                    {
                        tempX = currentX +extraY;
                        tempY = currentY -extraX;
                    }
                    else if (PATH==6)
                    {
                        tempX = currentX - extraY;
                        tempY = currentY - extraX;
                    }
                }
            }
        }
    }
    if (PATH != 0 )
    {
       ///if(PATH !=2) analogWrite(DPin, 0);
       if (PATH !=6) analogWrite(RPin, 0);
       if (PATH !=4) analogWrite(LPin, 0);
       if (PATH == 8) 
       {
            controlSPD(UPin, 15, 5);
       }
       else if(PATH ==4)
       {
            analogWrite(RPin, 0);
            analogWrite(LPin, 255);
            controlSPD(UPin, 8,5);
       }
       else if (PATH == 6)
       {
            analogWrite(LPin, 0);
            analogWrite(RPin, 255);
            controlSPD(UPin, 8, 5);
       }
       else if (PATH == 2)
       {
            analogWrite(DPin, 255);
       }
       else if (PATH == 5)
       {
            analogWrite(DPin, 255);
       }
    }
    else
    {
        analogWrite(DPin, 0);
        analogWrite(UPin, 0);
        analogWrite(RPin, 0);
        analogWrite(LPin, 0);
    }
    if(PATH !=4 && PATH !=6 && PATH !=0)
    {
        if(currentAngle > firstAngle)
        {
            if(currentAngle - firstAngle >= 3)
            {
                if(currentAngle - firstAngle >45)
                {
                    analogWrite(LPin, 0);
                    analogWrite(RPin, 255);
                    controlSPD(UPin, 8, 5);
                }
                else
                {
                    analogWrite(RPin, 0);
                    analogWrite(LPin, 255);
                    controlSPD(UPin, 8, 5);
                }
            }
            else
            {
                analogWrite(RPin, 0);
                analogWrite(LPin, 0);
                controlSPD(UPin, 15, 5);
            }
        }
        else if(currentAngle < firstAngle)
        {
            if(firstAngle - currentAngle >= 3)
            {
                if(firstAngle - currentAngle >45)
                {
                    analogWrite(RPin, 0);
                    analogWrite(LPin, 255);
                    controlSPD(UPin, 8, 5);
                }
                else
                {
                    analogWrite(LPin, 0);
                    analogWrite(RPin, 255);
                    controlSPD(UPin, 8, 5);
                }
            }
            else
            {
                analogWrite(LPin, 0);
                analogWrite(RPin, 0);
                controlSPD(UPin, 15, 5);
            }
        }
    }


    /// Servo
    currentServoTimer = millis();
    if (currentServoTimer - servoTimer >=30)
    {
        long angle01 = -i*75 ;
        long angle02 = angle01 - 90*100;
        long angle03 = angle01 + 90*100;
        long angle04 = angle01 + 180*100;
        
        servo.write(i);
        
        /// Sensors
        if(currentSensorTimer - sensorTimer>=200 && PATH != 4 && PATH != 6)
        {
            distance01 = sensor01.ping_cm();
            distance02 = sensor02.ping_cm();
            distance03 = sensor03.ping_cm();
            distance04 = sensor04.ping_cm();
            ///Cal_Angle();
            Data2Wifi(distance01, angle01, distance02, angle02, distance03, angle03, distance04, angle04, (double)(currentAngle*100), tempX, tempY);
            sensorTimer = currentSensorTimer;
        }
        else if (currentSensorTimer - sensorTimer >= 200 && (PATH == 4 || PATH == 6))
        {
            Data2Wifi(0, angle01, 0, angle02, 0, angle03, 0, angle04, (double)(currentAngle*100), tempX, tempY);
            sensorTimer = currentSensorTimer;
        }
        i= i + ((reverse == true) ? -1 : 1);
        if (i == 120) reverse = true;
        else if (i == 0) reverse = false;
        servoTimer = currentServoTimer;
    }
    
    
    
    
  
}
