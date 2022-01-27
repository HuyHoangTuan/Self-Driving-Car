#include "ESP8266WiFi.h"
#include <PubSubClient.h>
#include <ArduinoJson.h>
///const char* ssid = "WFi Kien";

const char* ssid = "Tuyet Lan ";
///const char* password = "wwwfi123456";
const char* password = "12305abc";

#define MQTT_PORT 1884
#define MQTT_BROKER "192.168.1.235" 
#define QOS 1
WiFiClient espClient;
PubSubClient client(espClient);

long Timer = 0;
bool connectedToServer = false;
void setup() 
{
    Serial.begin(9600);
    Serial.setTimeout(5);
  
    WiFi.mode(WIFI_STA);
    WiFi.begin(ssid, password);
    while (WiFi.status() != WL_CONNECTED)
    {
        delay(250);
    }
    delay(100);

    client.setServer(MQTT_BROKER, MQTT_PORT);
    client.setCallback(callback);
}

void Reconnect()
{
    while(!client.connected())
    {
        if(client.connect("Bin",0,0,0,0))
        {
            client.publish("/test/car-response","",QOS);
            client.subscribe("/test/car", QOS);
        }
        else
        {
            delay(1000);
        }
    }
}
char incomeMsg[200];
DynamicJsonDocument jsonRoot(200);
void callback(char* topic, byte* payload, long Length)
{
    jsonRoot.clear();
    for(int i=0;i<Length;i++)
    {
        sprintf(incomeMsg+i,"%c",(char)payload[i]);
    }
    deserializeJson(jsonRoot, incomeMsg);

    const char* type = jsonRoot["type"];
    if(strcmp(type, "connection") == 0 && connectedToServer ==false)
    {
        if(!jsonRoot.containsKey("status"))
        {
            client.publish("/test/car-response","{\"type\" : \"connection\" , \"status\" : \"OK\"}");
            connectedToServer = true;
        }
    }
    else
    if(strcmp(type, "connection") == 0 && connectedToServer ==true)
    {
        if(jsonRoot.containsKey("status"))
        {
            const char* Status = jsonRoot["status"];
            if(strcmp(Status, "cancel") == 0)
            {
                connectedToServer = false;
                client.disconnect();
            }
        } 
    } 
    if(connectedToServer == false) return;

    if(strcmp(type, "data-server") == 0)
    {
        if(jsonRoot.containsKey("entity"))
        {
            if(jsonRoot.containsKey("path"))
            {
                char buff[60];
                const char* entity = jsonRoot["entity"];
                const char* path = jsonRoot["path"];
                const char* times = jsonRoot["times"];
                strcpy(buff, entity);
                strcpy(buff+strlen(entity),":");
                strcpy(buff+strlen(entity)+1,path);
                strcpy(buff+strlen(entity)+1+strlen(path),"|");
                strcpy(buff+strlen(entity)+1+strlen(path)+1,times);
                strcpy(buff+strlen(entity)+1+strlen(path)+1+strlen(times),"\n");
                ///client.publish("/test/car-response", buff);
                Serial.write(buff);
          }
        }
    }
}
char msg[450];
void ReadString(char* s)
{
    ///if(strlen(s)==0) return;
    long ar[11];
    memset(ar, 0, sizeof(ar));
    for(int i=0;i<11;i++) ar[i] = -10000000;
    sscanf(s, "%ld %ld %ld %ld %ld %ld %ld %ld %ld %ld %ld",&ar[0], &ar[1], &ar[2], &ar[3], &ar[4], &ar[5], &ar[6], &ar[7], &ar[8], &ar[9], &ar[10]);
    for(int i=0;i<11;i++) if(ar[i]== -10000000) return;
    snprintf(s,250,"{\"type\" : \"data-sensor\" , \"distance\" : [%ld, %ld, %ld, %ld], \"sensorAngle\" : [%ld, %ld, %ld, %ld], \"carSensor\" : %ld , \"x\" : %ld , \"y\": %ld }", ar[0], ar[2], ar[4], ar[6], ar[1], ar[3], ar[5], ar[7], ar[8], ar[9], ar[10]);
}

void loop() 
{
    Reconnect();   
    client.loop();
    if(connectedToServer ==   true)
    {
        String mess = Serial.readString();
        mess.toCharArray(msg, 450);
        ReadString(msg);
        if(strlen(msg) != 0)
            client.publish("/test/car-response",msg);  
    }
}
