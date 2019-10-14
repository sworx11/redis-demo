package cc.pachira.redisdemo.controller;

import cc.pachira.redisdemo.plugins.Mail;
import cc.pachira.redisdemo.util.CodeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/code")
public class CodeController {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private Mail mail;

    @PostMapping("/register")
    public String sendRegisterCode(String mail) {
        sendCode(CodeType.REGISTER, mail);
        return "ok";
    }

    @PostMapping("/signin")
    public String sendSignInCode(String mail) {
        if (!redisTemplate.hasKey("user:" + mail))
            return "no such mail!";

        sendCode(CodeType.SINGIN, mail);
        return "ok";
    }

    @PostMapping("/repass")
    public String sendRePassCode(String mail) {
        if (!redisTemplate.hasKey("user:" + mail))
            return "no such mail!";

        sendCode(CodeType.REPASS, mail);
        return "ok";
    }

    private void sendCode(CodeType type, String mail) {
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        redisTemplate.opsForValue().set(type.name().toLowerCase() + ":" + mail, code, 1800, TimeUnit.SECONDS);
        this.mail.sendMail(mail, "你的验证码已送达，请妥善保管", "你的验证码是：" + code + "。有效期30分钟，请尽快使用！");
    }
}
