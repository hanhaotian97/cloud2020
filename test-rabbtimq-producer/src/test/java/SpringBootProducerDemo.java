import com.hht.test.rabbitmq.TestRabbitmqProducerApplication;
import com.hht.test.rabbitmq.config.RabbitmqConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * <br/>Author hanhaotian
 * <br/>Description : 使用rabbitTemplate发送消息
 * <br/>CreateTime 2020/7/13
 */
@SpringBootTest(classes = TestRabbitmqProducerApplication.class)
@RunWith(SpringRunner.class)
public class SpringBootProducerDemo {
    @Resource
    RabbitTemplate rabbitTemplate;

    @Test
    public void test1() {
        String message = "send email to user hht123456";
        rabbitTemplate.convertAndSend(RabbitmqConfig.EXCHANGE_TOPICS_INFORM, "inform.email", message);
    }
}
