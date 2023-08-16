package com.nowcoder.community.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
public class MailClient {
    //获取日志信息
    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);
    //加入依赖后使用框架提供的类
    @Resource
    private JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String from;
    //to 发给谁，subject 发送的邮箱标题，content 发送的内容
    public void sendMail(String to, String subject, String content) {
        try {
            //重点在构建消息体message
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            //true的意思是，识别content中html格式的内容
            helper.setText(content, true);
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            logger.error("发送邮件失败:" + e.getMessage());
        }
    }
}
