import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * <br/>Author hanhaotian
 * <br/>Description : routing模式
 *
 * 工作流程：
 * 1. 交换机绑定多个队列，每个队列可以设置routingkey，一个队列可以设置多个routingkey
 * 2. 生产者将消息发送给交换机，发送消息时指定routingkey，交换机会判断routingkey和队列中的routingkey，将消息发送到指定的队列中
 *
 * 工作流程与routing模式一致，不同的是routing是相等匹配，而topics是通配符匹配。
 * <br/>CreateTime 2020/7/12
 */
public class ProducerTopicsDemo {
    //队列名
    public static String QUEUE_INFORM_EMAIL = "queue_inform_email";
    public static String QUEUE_INFORM_SMS = "queue_inform_sms";
    //交换机名称
    public static String EXCHANGE_TOPICS_INFORM = "exchange_topics_inform";

    public static String ROUTINGKEY_EMAIL = "inform.#.email.#";
    public static String ROUTINGKEY_SMS = "inform.#.sms.#";

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

            //声明队列
            channel.queueDeclare(QUEUE_INFORM_SMS, true, false, false, null);
            channel.queueDeclare(QUEUE_INFORM_EMAIL, true, false, false, null);

            //声明交换机
            channel.exchangeDeclare(EXCHANGE_TOPICS_INFORM, BuiltinExchangeType.TOPIC);
            //将交换机与队列绑定
            channel.queueBind(QUEUE_INFORM_EMAIL, EXCHANGE_TOPICS_INFORM, ROUTINGKEY_EMAIL);
            channel.queueBind(QUEUE_INFORM_SMS, EXCHANGE_TOPICS_INFORM, ROUTINGKEY_SMS);

            //发送消息
            for (int i = 0; i < 10; i++) {
                String message = "第" + i + "条消息";
                //发送消息，设置exchange名称，设置routingkey，所有的消息都会被发往该routingkey匹配的队列中
                channel.basicPublish(EXCHANGE_TOPICS_INFORM, "inform.email", null, message.getBytes());
                System.out.println("消息发送成功，消息内容：" + message);
            }
            System.out.println();
            for (int i = 0; i < 10; i++) {
                String message = "第" + i + "条消息";
                //发送消息，设置exchange名称，设置routingkey，所有的消息都会被发往该routingkey匹配的队列中
                channel.basicPublish(EXCHANGE_TOPICS_INFORM, "inform.sms", null, message.getBytes());
                System.out.println("消息发送成功，消息内容：" + message);
            }
            System.out.println();
            for (int i = 0; i < 5; i++) {
                String message = "第" + i + "条消息";
                //发送消息，设置exchange名称，设置routingkey，所有的消息都会被发往该routingkey匹配的队列中
                channel.basicPublish(EXCHANGE_TOPICS_INFORM, "inform.sms.email", null, message.getBytes());
                System.out.println("消息发送成功，消息内容：" + message);
            }
            System.out.println();
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

class ConsumerTopicsEmail {
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
            channel.queueDeclare(ProducerTopicsDemo.QUEUE_INFORM_EMAIL, true, false, false, null);

            //声明交换机
            channel.exchangeDeclare(ProducerTopicsDemo.EXCHANGE_TOPICS_INFORM, BuiltinExchangeType.TOPIC);
            //将交换机与队列绑定
            channel.queueBind(ProducerTopicsDemo.QUEUE_INFORM_EMAIL, ProducerTopicsDemo.EXCHANGE_TOPICS_INFORM, ProducerTopicsDemo.ROUTINGKEY_EMAIL);

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
            channel.basicConsume(ProducerTopicsDemo.QUEUE_INFORM_EMAIL, true, defaultConsumer);

            System.out.println("消息完全接受完毕");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //消费者需要保持连接，不用关闭连接。
        }
    }
}

class ConsumerTopicsSms {
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
            channel.queueDeclare(ProducerTopicsDemo.QUEUE_INFORM_SMS, true, false, false, null);

            //声明交换机
            channel.exchangeDeclare(ProducerTopicsDemo.EXCHANGE_TOPICS_INFORM, BuiltinExchangeType.TOPIC);
            //将交换机与队列绑定
            channel.queueBind(ProducerTopicsDemo.QUEUE_INFORM_SMS, ProducerTopicsDemo.EXCHANGE_TOPICS_INFORM, ProducerTopicsDemo.ROUTINGKEY_SMS);

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
            channel.basicConsume(ProducerTopicsDemo.QUEUE_INFORM_SMS, true, defaultConsumer);

            System.out.println("消息完全接受完毕");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //消费者需要保持连接，不用关闭连接。
        }
    }
}