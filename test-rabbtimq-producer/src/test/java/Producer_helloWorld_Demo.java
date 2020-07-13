import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * <br/>Author hanhaotian
 * <br/>Description : 生产者入门程序
 * <br/>CreateTime 2020/7/12
 */
public class Producer_helloWorld_Demo {

    //队列名称
    static String QUEUE_NAME = "helloWorld";

    /**
     * 发送消息
     */
    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("127.0.0.1");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        //每个虚拟主机都会有一套独立的 Channel、queue、exchange
        connectionFactory.setVirtualHost("/");

        //建立连接
        Connection connection = null;
        Channel channel = null;
        try {
            //建立新连接
            connection = connectionFactory.newConnection();
            //创建会话通道，与mq的通信都在channel中完成
            channel = connection.createChannel();
            //默认带有一个交换机，此处跳过 创建exchange步骤
            /*声明队列
                durable：是否持久化
                exclusive：是否声明一个仅用于此连接的独占队列
                autoDelete：是否声明队列不再使用后自动删除，与exclusive配合使用实现临时队列
                arguments：队列拓展参数
             */
            channel.queueDeclare(Producer_helloWorld_Demo.QUEUE_NAME, true, false, false, null);

            //发送消息，exchange为null时使用默认交换机，此时routingKey必须设置为队列名称
            String message = "第一条消息";
            channel.basicPublish("", Producer_helloWorld_Demo.QUEUE_NAME, null, message.getBytes());

            System.out.println("消息发送成功，消息内容：" + message);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (channel != null) {
                    channel.close();
                }
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class Consumer_helloWorld {
    /**
     * 启动消费者监听队列
     */
    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("127.0.0.1");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        //每个虚拟主机都会有一套独立的 Channel、queue、exchange
        connectionFactory.setVirtualHost("/");

        //建立连接
        Connection connection = null;
        Channel channel = null;
        try {
            //建立新连接
            connection = connectionFactory.newConnection();
            //创建会话通道，与mq的通信都在channel中完成
            channel = connection.createChannel();
            //默认带有一个交换机（direct），此处跳过 创建exchange步骤
            /* 声明队列
                durable：是否持久化
                exclusive：是否声明一个仅用于此连接的独占队列
                autoDelete：是否声明队列不再使用后自动删除，与exclusive配合使用实现临时队列
                arguments：队列拓展参数
             */
            channel.queueDeclare(Producer_helloWorld_Demo.QUEUE_NAME, true, false, false, null);

            //实现消费方法
            DefaultConsumer defaultConsumer = new DefaultConsumer(channel) {
                /**
                 * 接收到消息后此方法被调用
                 * @param consumerTag 消费者标签，用于标识消费者
                 * @param envelope    信封
                 * @param properties  队列拓展参数
                 * @param body        发送的具体内容
                 * @throws IOException
                 */
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    super.handleDelivery(consumerTag, envelope, properties, body);
                    //消息id，在channel中标识消息，用于确认消息被接受
                    long deliveryTag = envelope.getDeliveryTag();
                    String exchange = envelope.getExchange();
                    System.out.println("消息已被订阅接受，deliveryTag为：" + deliveryTag + "\t 消息内容为：" + new String(body, "utf-8"));
                }
            };

            //监听队列
            channel.basicConsume(Producer_helloWorld_Demo.QUEUE_NAME, true, defaultConsumer);

            System.out.println("消息接受完毕");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //消费者需要保持连接，不用关闭连接。
        }
    }
}