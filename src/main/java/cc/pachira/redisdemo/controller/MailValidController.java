package cc.pachira.redisdemo.controller;

import cc.pachira.redisdemo.plugins.Mail;
import cc.pachira.redisdemo.util.CodeType;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

@RestController
public class MailValidController {
    @Autowired
    private Mail mail;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("/valid")
    public String valid(String mail) {
        boolean valid = this.mail.valid(mail, "xkriss.xyz");
        if (valid)
            return "success";
        else
            return "fail";
    }

    @PostMapping("/register")
    public String register(String mail, String pass, String code) {
        ValueOperations<String, String> operations = redisTemplate.opsForValue();

        if (redisTemplate.hasKey("user:" + mail)) {
            redisTemplate.delete(CodeType.REGISTER.name().toLowerCase() + ":" + mail);
            return "mail exist!";
        }

        if (!code.equals(operations.get(CodeType.REGISTER.name().toLowerCase() + ":" + mail)))
            return "code error!";

        operations.set("user:" + mail, DigestUtils.md5DigestAsHex(pass.getBytes()));
        redisTemplate.delete(CodeType.REGISTER.name().toLowerCase() + ":" + mail);
        return "register ok";
    }

    @PostMapping("/login")
    public String login(String mail, String pass, String code, HttpServletRequest request) {
        ValueOperations<String, String> operations = redisTemplate.opsForValue();

        if (!redisTemplate.hasKey("user:" + mail)) {
            redisTemplate.delete(CodeType.SINGIN.name().toLowerCase() + ":" + mail);
            return "mail not exist!";
        }

        if (!code.equals(operations.get(CodeType.SINGIN.name().toLowerCase() + ":" + mail)))
            return "code error!";

        String s = DigestUtils.md5DigestAsHex((code + "|" + operations.get("user:" + mail)).getBytes());
        if (!pass.equals(s)) {
            return "wrong password!";
        }

        operations.set("log:" + mail, getIpAddr(request) + "-" + System.currentTimeMillis());
        redisTemplate.delete(CodeType.SINGIN.name().toLowerCase() + ":" + mail);
        return "ok";
    }

    private String getIpAddr(HttpServletRequest request) {
        String ipAddress = null;
        try {
            ipAddress = request.getHeader("x-forwarded-for");
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
                if (ipAddress.equals("127.0.0.1")) {
                    // 根据网卡取本机配置的IP
                    InetAddress inet = null;
                    try {
                        inet = InetAddress.getLocalHost();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    ipAddress = inet.getHostAddress();
                }
            }
            // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
            if (ipAddress != null && ipAddress.length() > 15) { // "***.***.***.***".length()
                // = 15
                if (ipAddress.indexOf(",") > 0) {
                    ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
                }
            }
        } catch (Exception e) {
            ipAddress="";
        }
        // ipAddress = this.getRequest().getRemoteAddr();

        return ipAddress;
    }

}
