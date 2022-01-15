
#define trig01 03
#define echo01 04

unsigned long sensor01Timer = 0, currentSensor01Timer = 0;
short sensor01State = LOW;
long duration01 = 0, distance01 = 0;
long MAX_DIS = 300000;
long Cal_Sensor_1(short triggerPin, short echoPin)
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
void Cal_Sensor(short triggerPin, short echoPin,short *state, long *t, unsigned long *cTimer,unsigned long *d, long *dis)
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
void setup() 
{
    Serial.begin(9600);
    Serial.setTimeout(5);

    pinMode(trig01, OUTPUT);
    pinMode(echo01, INPUT);
}


void loop() 
{
    Cal_Sensor(trig01, echo01, &sensor01State, &sensor01Timer, &currentSensor01Timer, &duration01, &distance01);
    ///distance01 = Cal_Sensor_1(trig01, echo01);
    Serial.println(distance01);
}
