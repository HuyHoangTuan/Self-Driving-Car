const amqp = require("amqplib");


const amqpUrl = "amqps://cviicssw:VBPzFAzdC5ztCH2Vnv6Xm1cRGOLXRz9U@jaguar.rmq.cloudamqp.com/cviicssw";
const queue = "rabbitmq";
const msg =
    {
        "id": "11",
        "packet_no": 126,
        "temperature": 30,
        "humidity": 60,
        "tds": 1100,
        "pH": 5.0
    }
let connection;
const Produce = async () => {
    try {
        connection = await amqp.connect(amqpUrl);
        const channel = await connection.createChannel();
        await channel.assertQueue(queue,{
            durable:false,
        });
        await channel.sendToQueue(queue, Buffer.from(JSON.stringify(msg)));
        connection.on("error", (err) =>
        {
            console.log(err);
            setTimeout(connection, 10000);
        });

        connection.on("close", () =>
        {
            console.error("connection to RabbitQM closed!");
            setTimeout(connection, 10000);
        });
        console.log("[x] Sent %s", msg)

    } catch (err) {
        console.error(err);
        setTimeout(connection, 10000);
    }
};
const Customer = async () =>{
    try {
        connection = await amqp.connect(amqpUrl);
        const channel = await connection.createChannel();
        await channel.assertQueue(queue,{
            durable:false
        });
        await channel.consume(queue,async (msg) =>{
            console.log(msg.content.toString());
            channel.ack(msg);
        })
        connection.on("error", (err) =>
        {
            console.log(err);
            setTimeout(connection, 10000);
        });

        connection.on("close", () =>
        {
            console.error("connection to RabbitQM closed!");
            setTimeout(connection, 10000);
        });
    }catch (err){
        console.error(err);
        setTimeout(connection,10000);
    }
}
Produce();
Customer();