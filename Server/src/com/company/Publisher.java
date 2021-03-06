package com.company;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.Queue;

public class Publisher implements MqttCallback
{
    private String pubTopic;
    private String subTopic;
    private String broker ;
    private String clientId;
    private MemoryPersistence persistence ;
    private MqttClient client;
    private MqttConnectOptions options;
    public Queue<String> queue;
    private boolean connected = false;

    public boolean isConnected() {
        return connected;
    }

    public String getSubTopic() {
        return subTopic;
    }

    public String getBroker() {
        return broker;
    }

    public String getPubTopic()
    {
        return pubTopic;
    }

    public Publisher(String broker, String pubTopic, String subTopic, String clientId)
    {
        this.broker = broker;
        this.pubTopic = pubTopic;
        this.subTopic = subTopic;
        this.clientId = clientId;
        this.queue = new LinkedList<String>();
        /// Set-up MQTT connection
        persistence = new MemoryPersistence();
        try
        {
            client = new MqttClient(broker, clientId, persistence);
            options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setKeepAliveInterval(180); ///180s
            options.setCleanSession(true);
            client.connect(options);
            client.setCallback(this);
            client.subscribe(subTopic,0);
            ///client.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void publish(String content) throws Exception
    {
        MqttMessage message =  new MqttMessage(content.getBytes());
        message.setQos(0);
        ///message.setRetained(true);
        client.publish(pubTopic, message);
    }

    public void disconnect() throws Exception
    {
        client.disconnect();
    }



    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken)
    {

    }
    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception
    {

        try
        {
            JSONObject res = new JSONObject(new String(mqttMessage.getPayload()));
            if(res.getString("type").equals("connection"))
            {

                if(res.getString("status").equals("OK"))
                {
                    this.connected = true;
                    return;
                }
                else
                if(res.getString("status").equals("STOP"))
                {
                    this.connected = false;
                }
            }
            if(this.connected == false) return;
            if(res.getString("type").equals("data-web"))
            {
                if(res.getString("entity").equals("finish-point"))
                {
                    double x = Double.parseDouble(res.getString("x"));
                    double y = Double.parseDouble(res.getString("y"));
                    double distance = Math.sqrt(x*x + y*y);
                    double angle = Math.atan(x/y)*180.0/ Math.PI;
                    if(x>0 && y<0) {angle+=180.0;}
                    if(x<0 && y<0) {angle=180.0+Math.abs(angle);}
                    if(x<0 && y>0) {angle=360.0+angle;}
                    Car.PointToMap(distance, angle, Car.FINISH);
                    Car.find_path();
                }
                else
                if(res.getString("entity").equals("obstacle"))
                {
                    System.out.println(res.getString("distance")+" , "+res.getString("angle"));
                    System.out.println("------------------------");
                }
                else if (res.getString("entity").equals("key"))
                {
                    int keyCode = res.getInt("value");
                    if(keyCode < 37 || keyCode > 40) return;
                    int path = 0;
                    int times = 0;
                    if(keyCode == 40)
                    {
                        path = 5;
                        times = 20;
                    }
                    else if (keyCode == 38)
                    {
                        path = 8;
                        times = 60;
                    }
                    else if(keyCode == 37)
                    {
                        queue.add(Main.JSONConverter( new String [] {
                                "type","data-server",
                                "entity", "1",
                                "path","2,4",
                                "times","20,180"
                        }));
                        return;
                    }
                    else if(keyCode == 39)
                    {
                        queue.add(Main.JSONConverter( new String [] {
                                "type","data-server",
                                "entity", "1",
                                "path","2,6",
                                "times","20,180"
                        }));
                        return;
                    }
                    queue.add(Main.JSONConverter( new String [] {
                            "type","data-server",
                            "entity", "1",
                            "path",Integer.toString(path),
                            "times",Integer.toString(times)
                    }));
                }
            }
            else
            if(res.getString("type").equals("data-sensor"))
            {
                if(Main.inProcess == 1) return;
                if(Main.incomeData <=4)
                {
                    Main.incomeData++;
                    return;
                }
                JSONArray Distance = res.getJSONArray("distance");
                JSONArray Angle = res.getJSONArray("sensorAngle");
                double carAngle = res.getInt("carSensor");
                long X = res.getInt("x");
                long Y = res.getInt("y");
                carAngle/=100.0;
                if(Car.currentAngle == -1) Car.currentAngle = carAngle;
                ///System.out.println(carAngle+", "+Car.currentAngle);

                String newContent = Main.JSONConverter(new String[] {
                        "type","data-server",
                        "entity","map",
                        "x", Long.toString(Y-Car.Y),
                        "y",Long.toString(X-Car.X)
                });
                queue.add(newContent);
                newContent = Main.JSONConverter(new String[] {
                        "type","data-server",
                        "entity","path",
                        "x",Long.toString(0),
                        "y",Long.toString(0)
                });
                queue.add(newContent);
                Car.PointToMap(X, Y, Car.START);
                ///queue.add(newContent);
                for(int i=0;i<4;i++)
                {
                    double distance = Distance.getInt(i);
                    ///distance = distance / 1000.0;
                    if (Math.abs(distance - 0.0) <= 0.01) continue;

                    double sensorAngle = Angle.getInt(i);
                    sensorAngle = sensorAngle / 100.0;
                    if(Math.abs(sensorAngle) >360.0 ) continue;

                    if (i == 0)
                    {
                        if (sensorAngle < -90.0 || sensorAngle > 0.0) continue;
                    }
                    ///if(i!=0) continue;
                    if (i==1)
                    {
                        if (sensorAngle < -180.0 || sensorAngle > -90.0) continue;
                    }
                    if (i==2)
                    {
                        if (sensorAngle >90.0 || sensorAngle < 0.0) continue;
                    }
                    if (i==3)
                    {
                        if(sensorAngle >180.0 || sensorAngle < 90.0) continue;
                    }
                    double angle = Math.toRadians(sensorAngle+carAngle-Car.currentAngle);
                    double x = Math.sin(angle)*distance*601.0/801.0;
                    double y = Math.cos(angle)*distance*601.0/801.0;

                    y=(y*(-1.0));
                    /*if(i ==0)
                    {
                        System.out.println(angle+", "+carAngle+", "+Car.currentAngle);
                    }*/
                    Car.PointToMap(distance, sensorAngle+carAngle-Car.currentAngle, Car.OBSTACLE);
                    if(Car.isObstacle(distance, sensorAngle+carAngle-Car.currentAngle) == true)
                    {
                        newContent = Main.JSONConverter(new String[]{
                                "type", "data-server",
                                "entity", "obstacle",
                                "x", String.valueOf(x),
                                "y", String.valueOf(y),
                        });

                        queue.add(newContent);
                    }


                    /*System.out.print("ID: ");
                    System.out.println(i);
                    System.out.print("Distance: ");
                    System.out.println(distance);
                    System.out.print("Sensor-Angle: ");
                    System.out.println(sensorAngle);
                    System.out.print("Car-Angle: ");
                    System.out.println(carAngle);
                    System.out.print("X: ");
                    System.out.println(x);
                    System.out.print("Y: ");
                    System.out.println(y);
                    System.out.println("-------------------------------------------");*/
                }
            }
        } catch(Exception e)
        {
            return;
        }

    }

    @Override
    public void connectionLost(Throwable throwable) {
        System.out.println("LOST!!!!");
    }

}
