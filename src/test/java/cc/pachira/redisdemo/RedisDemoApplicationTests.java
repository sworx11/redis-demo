package cc.pachira.redisdemo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.context.junit4.SpringRunner;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableScheduling
public class RedisDemoApplicationTests {

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private JavaMailSender mailSender;

    @Test
    public void contextLoads() {
    }

    @Test
    public void testRedis() {
        int count = 80;

        ExecutorService executor = Executors.newCachedThreadPool();

        final CountDownLatch latch = new CountDownLatch(count);

        long start = System.currentTimeMillis();
        long end;
        for (int i = 0; i < count; i++) {
            executor.execute(() -> {
                try {
                    while (redisTemplate.opsForList().size("product") > 0) {
                        redisTemplate.opsForList().rightPop("product");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            end = System.currentTimeMillis();
            executor.shutdown();
        }

        System.out.println(end - start);
    }

    @Test
    public void writeRedis() {
        int count = 20;

        ExecutorService executor = Executors.newCachedThreadPool();

        final CountDownLatch latch = new CountDownLatch(count);

        long start = System.currentTimeMillis();
        long end;
        for (int i= 1; i <= count; i++) {
            final int threadNum = i;
            executor.execute(() -> {
                try {
                    for (int j = 0; j < 2000; j++) {
                        redisTemplate.opsForList().leftPush("product", (threadNum - 1) * 2000  + j);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            end = System.currentTimeMillis();
            executor.shutdown();
        }

        System.out.println(end - start);
    }

    @Test
    @Scheduled(cron = "0/20 * * * * ?")
    public void write() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        stringRedisTemplate.opsForList().leftPush("zero", "message: " + format.format(new Date()));
    }

    @Test
    public void read() {
        while (true) {
            System.out.println(stringRedisTemplate.opsForList().rightPop("zero", 8, TimeUnit.SECONDS));
        }
    }

    @Test
    public void sendCode() throws Exception {
        sendSimpleMail("3121987131@qq.com", "你的验证码已送达，请妥善保管", "你的验证码是：440951");
    }

    public void sendSimpleMail(String to,String subject,String content) throws Exception {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,true);
        helper.setFrom(new InternetAddress("iamchriswu@163.com","芝士音乐","UTF-8"));
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content,true);
        mailSender.send(mimeMessage);
    }
}