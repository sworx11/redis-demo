package cc.pachira.redisdemo;

import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.util.DigestUtils;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Nova {

    private static BlockingQueue<String> queue = new LinkedBlockingQueue<>();


    public static void main(String[] args) throws Exception {
//        new Thread(Nova::write).start();
//        new Thread(Nova::read).start();
        System.out.println(DigestUtils.md5DigestAsHex("159456".getBytes()));
    }

    public static void write() {
        final Random r = new Random();
        while (true) {
            queue.offer("message: " + UUID.randomUUID());
            try {
                Thread.sleep(r.nextInt(2000));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void read() {
        while (true) {
            try {
                System.out.println(queue.poll(1000, TimeUnit.MILLISECONDS));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
