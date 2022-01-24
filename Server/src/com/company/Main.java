package com.company;


public class Main
{
    /*
        "type" : "connection",
        "width" : "",
        "height" : "",
        "numRow" : "",
        "numCol" : "",
        "baseApla" : ""
        ->

        "type" : "connection",
        "status" : "OK"
     */

    /*
        /// data from sensor
        "type" : "data-sensor"
        "entity" : "obstacle",
        "id" : "",
        "distance" : "",
        "angle" : ""
        -----------------------

        /// data from web
        "type" : "data-web",
        "entity" : "finish-point",
        "distance" : "",
        "angle" : ""
        ---------------------------

        "type" : "data"
        "entity" : "car",
        "direction" : "Left, Right, Up, Down",

        /// web ->
        "type" : "data",
        "entity" : "finish-point",
        "x" : "",
        "y" : ""
     */
    public static int inProcess = 0;
    public static String JSONConverter(String... content)
    {
        StringBuffer res = new StringBuffer();
        res.append("{");
        for(int i=0;i<content.length;i+=2)
        {
            String field = content[i];
            String val = content[i+1];
            res.append("\"" + field +"\"  : \"" + val +"\"");
            if(i + 2 != content.length) res.append(",");
        }
        res.append('}');
        return res.toString();
    }
    public static void disconnect(Publisher client) throws Exception
    {
        client.publish   (
                JSONConverter   (
                        new String[]    {
                                "type","connection",
                                "status","cancel"
                        }
                )
        );
        client.disconnect();
    }
    public static void main(String[] args) throws Exception
    {
        Publisher clientWeb = new Publisher("ws://broker.emqx.io:8083/mqtt","/test/web", "/test/web-response", "Fankychop");
        Publisher clientCar = new Publisher("tcp://anlozrer.duckdns.org:31884","/test/car", "/test/car-response", "123");
        /// Set-up connection
        while(!clientWeb.isConnected() || !clientCar.isConnected())
        {
            System.out.println();
            System.out.print("Waiting for");
            if(!clientWeb.isConnected())
            {
                clientWeb.publish(
                        JSONConverter(
                                new String[]{"type", "connection"

                                }
                        )
                );
                System.out.print(" Web");
                Thread.sleep(250);
            }
            System.out.print(" +");

            if(!clientCar.isConnected())
            {
                clientCar.publish   (
                        JSONConverter   (
                                new String[]    {
                                        "type", "connection"
                                }
                        )
                );
                System.out.println(" Car");
                Thread.sleep(250);
            }

        }
        System.out.println("Connected to "+ clientWeb.getBroker() +" and "+clientCar.getBroker()+" !!!");
        System.out.println("Pub topic (web and car): "+clientWeb.getPubTopic()+" and "+clientCar.getPubTopic());
        System.out.println("Sub topic (web and car): "+clientWeb.getSubTopic()+" and "+clientCar.getSubTopic());
        System.out.println("-----------------------------------------------------------------");

        Thread.sleep(500);
        while(clientCar.isConnected()== true && clientWeb.isConnected() == true)
        {
            if(!clientCar.queue.isEmpty())
            {
                clientWeb.publish(clientCar.queue.poll());
            }
            Thread.sleep(5);
            if(Car.checkPath() == false)
            {
                Car.find_path();
            }
            Thread.sleep(5);
            if(!Car.queue.isEmpty())
            {
                clientWeb.publish(Car.queue.poll());
            }
            Thread.sleep(5);
            if(!Car.queueModule.isEmpty())
            {
                clientCar.publish(Car.queueModule.poll());
            }
            Thread.sleep(5);

        }

        /// disconnect
        disconnect(clientWeb);
        Thread.sleep(50);
        disconnect(clientCar);
        Thread.sleep(50);
    }
}
