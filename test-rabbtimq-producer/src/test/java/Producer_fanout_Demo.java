import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * <br/>Author hanhaotian
 * <br/>Description : 发布/订阅模式
 * <br/>CreateTime 2020/7/12
 */
public class Producer_fanout_Demo {
    public static String QUEUE_INFORM_EMAIL = "queue_inform_email";
    public static String QUEUE_INFORM_SMS = "queue_inform_sms";
    public static String EXCHANGE_FANOUT_INFORM = "exchange_fanout_inform";

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

            //因为发布订阅模式中，生产者直接与交换机交互，queue并不参与，queue与N个消费者交互，所以不需要声明queue
            channel.queueDeclare(QUEUE_INFORM_SMS, true, false, false, null);
            channel.queueDeclare(QUEUE_INFORM_EMAIL, true, false, false, null);

            //声明交换机
            channel.exchangeDeclare(EXCHANGE_FANOUT_INFORM, BuiltinExchangeType.FANOUT);
            //将交换机与队列绑定
            channel.queueBind(QUEUE_INFORM_EMAIL, EXCHANGE_FANOUT_INFORM, "");
            channel.queueBind(QUEUE_INFORM_SMS, EXCHANGE_FANOUT_INFORM, "");

            for (int i = 0; i < 10; i++) {
                //发送消息，设置exchange名称
                String message = "第" + i + "条消息";
                channel.basicPublish(EXCHANGE_FANOUT_INFORM, "", null, message.getBytes());

                System.out.println("消息发送成功，消息内容：" + message);
            }
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

class Consumer_fanout_Email {
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

            /* 声明队列
                durable：是否持久化
                exclusive：是否声明一个仅用于此连接的独占队列
                autoDelete：是否声明队列不再使用后自动删除，与exclusive配合使用实现临时队列
                arguments：队列拓展参数
             */
            channel.queueDeclare(Producer_fanout_Demo.QUEUE_INFORM_EMAIL, true, false, false, null);

            //声明交换机
            channel.exchangeDeclare(Producer_fanout_Demo.EXCHANGE_FANOUT_INFORM, BuiltinExchangeType.FANOUT);
            //将交换机与队列绑定
            channel.queueBind(Producer_fanout_Demo.QUEUE_INFORM_EMAIL, Producer_fanout_Demo.EXCHANGE_FANOUT_INFORM, "");
            //channel.queueBind(Producer_fanout_Demo.QUEUE_INFORM_SMS, Producer_fanout_Demo.EXCHANGE_TOPICS_INFORM, "");

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
            channel.basicConsume(Producer_fanout_Demo.QUEUE_INFORM_EMAIL, true, defaultConsumer);

            System.out.println("消息完全接受完毕");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //消费者需要保持连接，不用关闭连接。
        }
    }
}

class Consumer_fanout_Sms {
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

            /* 声明队列
                durable：是否持久化
                exclusive：是否声明一个仅用于此连接的独占队列
                autoDelete：是否声明队列不再使用后自动删除，与exclusive配合使用实现临时队列
                arguments：队列拓展参数
             */
            channel.queueDeclare(Producer_fanout_Demo.QUEUE_INFORM_SMS, true, false, false, null);

            //声明交换机
            channel.exchangeDeclare(Producer_fanout_Demo.EXCHANGE_FANOUT_INFORM, BuiltinExchangeType.FANOUT);
            //将交换机与队列绑定
            channel.queueBind(Producer_fanout_Demo.QUEUE_INFORM_SMS, Producer_fanout_Demo.EXCHANGE_FANOUT_INFORM, "");

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
            channel.basicConsume(Producer_fanout_Demo.QUEUE_INFORM_SMS, true, defaultConsumer);

            System.out.println("消息完全接受完毕");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //消费者需要保持连接，不用关闭连接。
        }
    }
}