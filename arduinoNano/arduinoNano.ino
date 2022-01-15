#include <Servo.h>
#include <Arduino.h>
#include <Wire.h>
#include <HMC5883L_Simple.h>

#define trig01 3
#define echo01 4

#define trig02 5
#define echo02 6

#define trig03 7
#define echo03 8

#define trig04 9
#define echo04 10

#define servoPin 12

#define DPin A0
#define UPin A1
#define LPin A2
#define RPin A3

#define SPD 30


/// Servo
Servo servo;

/// exp
long EXP = 2000;

/// compass
HMC5883L_Simple Compass;
long compassTimer = 0;
double currentAngle = 0;

/// send-data
long sendingTimer = 0;

void setup()
{
    Serial.begin(9600);
    Serial.setTimeout(5);
    Wire.begin();

    /// Compass
    Compass.SetDeclination(106, 47, 'E');
    Compass.SetSamplingMode(COMPASS_SINGLE);
    Compass.SetScale(COMPASS_SCALE_088);
    Compass.SetOrientation(COMPASS_HORIZONTAL_X_NORTH);

    /// Servo
    servo.attach(servoPin);
    servo.write(0);

    /// ultrasonic sensor
    pinMode(trig01, OUTPUT);
    pinMode(echo01, INPUT);
    pinMode(trig02, OUTPUT);
    pinMode(echo02, INPUT);
    pinMode(trig03, OUTPUT);
    pinMode(echo03, INPUT);
    pinMode(trig04, OUTPUT);
    pinMode(echo04, INPUT);
    

    pinMode(UPin, OUTPUT);
    pinMode(DPin, OUTPUT);
    /// pinMode(Lpin, OUTPUT);
    /// pinMode(Rpin, OUTPUT);

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
    if (millis() - compassTimer >= 50)
    {
        currentAngle = Compass.GetHeadingDegrees();
        compassTimer = millis();
    }
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

long MAX_DIS = 300000;
/*
long Cal_Sensor(short triggerPin, short echoPin)
{
    long distance = 0;
    digitalWrite(triggerPin, LOW);
    delayMicroseconds(2);
    digitalWrite(triggerPin, HIGH); /// Send signal;
    delayMicroseconds(10);
    digitalWrite(triggerPin, LOW); /// Stop sending signal;

    long duration = pulseIn(echoPin, HIGH); /// respond signal;

    distance = duration * 34 / 2;

    if (distance > MAX_DIS)
    {
        distance = 10000000;
    }
    return distance;
}
*/
/*
long arr[21];
long avg = 0, _avg = 0, num = 0;
long Cal_Real_Dis(long cal_time, short triggerPin, short echoPin)
{
    avg = _avg = num = 0;
    for (int i = 0; i < cal_time; i++)
    {
        arr[i] = 0;
        arr[i] = Cal_Sensor(triggerPin, echoPin);
        avg += arr[i];
    }
    avg /= cal_time;
    for (int i = 0; i < cal_time; i++)
    {
        if (abs(arr[i] - avg) <= EXP)
        {
            _avg += arr[i];
            num++;
        }
    }
    if (num == 0)
        return 10000000;
    _avg /= num;
    return _avg;
}
*/

/// Car
long CurrentTimerSPD=0, TimerSPD=0;
bool check = false;
void controlSPD(int pin, int restTime, int actTime)
/// restTime =30
/// actTime = 4
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

unsigned long sensor01Timer = 0, currentSensor01Timer = 0;
long duration01 = 0, distance01 = 0;
unsigned long sensor02Timer = 0, currentSensor02Timer = 0;
long duration02 = 0, distance02 = 0;
unsigned long sensor03Timer = 0, currentSensor03Timer = 0;
long duration03 = 0, distance03 = 0;
unsigned long sensor04Timer = 0, currentSensor04Timer = 0;
long duration04 = 0, distance04 = 0;

void Cal_Sensor(short triggerPin, short echoPin,unsigned long *t,unsigned long *cTimer, long *d, long *dis)
{
    (*cTimer) = micros();
    digitalWrite(triggerPin, LOW);
    if ((*cTimer) - (*t) >= 3)
    {
        (*t) = (*cTimer);
        digitalWrite(triggerPin, HIGH);
    }
    if ((*cTimer) -(*t) >= 11)
    {
        (*t) = (*cTimer);
        digitalWrite(triggerPin, LOW);
    }
    (*d) = pulseIn(echoPin, HIGH);
    (*dis) = (*d) *34 /2;
    if ((*dis) > MAX_DIS)
    {
        (*dis) = 10000000;
    }
}
void loop()
{
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
       if (PATH == 8 ) controlSPD(UPin, 30, 4);
       else if (PATH == 2) controlSPD(DPin, 30, 4); 
    }

    /// Sensors
    Cal_Sensor(trig01, echo01, &sensor01Timer, &currentSensor01Timer, &duration01, &distance01);
    Cal_Sensor(trig02, echo02, &sensor02Timer, &currentSensor02Timer, &duration02, &distance02);
    Cal_Sensor(trig03, echo03, &sensor03Timer, &currentSensor03Timer, &duration03, &distance03);
    Cal_Sensor(trig04, echo04, &sensor04Timer, &currentSensor04Timer, &duration04, &distance04);
    Serial.print(distance01);
    Serial.print(", ");
    Serial.print(distance02);
    Serial.print(", ");
    Serial.print(distance03);
    Serial.print(", ");
    Serial.println(distance04);
    
    /// Servo
    currentServoTimer = millis();
    if (currentServoTimer - servoTimer >=10)
    {
        servo.write(i);
        i= i + ((reverse == true) ? -1 : 1);
        if (i == 101) reverse = true;
        else if (i == -1) reverse = false;
        servoTimer = currentServoTimer;
    }
    
    
    
    
  
}
