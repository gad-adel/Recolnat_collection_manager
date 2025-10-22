package org.recolnat.collection.manager.service.impl;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.recolnat.collection.manager.api.domain.Mail;
import org.recolnat.collection.manager.api.domain.enums.MailStatusEnum;
import org.recolnat.collection.manager.common.mapper.MailMapper;
import org.recolnat.collection.manager.repository.entity.MailJPA;
import org.recolnat.collection.manager.repository.jpa.MailJPARepository;
import org.recolnat.collection.manager.service.MailService;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailServiceImpl implements MailService {

    private final MailJPARepository mailJPARepository;

    private final MailMapper mailMapper;

    private final JavaMailSender mailSender;

    @Override
    public void create(Mail mail) {
        MailJPA mailJPA = mailMapper.toJPA(mail);
        mailJPARepository.save(mailJPA);
    }

    @Transactional
    public Optional<MailJPA> findFirstPending() {
        var mailOpt = mailJPARepository.findFirstPending();
        if (mailOpt.isPresent()) {
            mailOpt.get().setState(MailStatusEnum.SENDING);
            return Optional.of(mailJPARepository.save(mailOpt.get()));
        }
        return mailOpt;
    }

    @Override
    public void send(MailJPA mail) {
        final MimeMessage message;
        try {
            message = mailSender.createMimeMessage();
            message.setFrom(new InternetAddress(mail.getFrom()));

            final var helper = new MimeMessageHelper(message, true);
            helper.setSubject(mail.getSubject());
            helper.setTo(new InternetAddress(mail.getTo()));
            if (mail.getCc() != null) {
                // TODO DTH à prévoir
            }

            if (mail.getBcc() != null) {
                // TODO DTH à prévoir
            }

            final var multipart = new MimeMultipart();
            final var body = new MimeBodyPart();
            body.setContent(mail.getContent(), "text/html; charset=utf-8");
            multipart.addBodyPart(body);
            message.setContent(multipart);

            mailSender.send(message);
            mail.setState(MailStatusEnum.SENT);
            mailJPARepository.save(mail);
        } catch (final Exception e) {
            // TODO DTH retry
            log.error("Impossible d'envoyer le mail pour {} : ", mail.getId(), e);
            mail.setState(MailStatusEnum.ERROR);
            mailJPARepository.save(mail);
        }
    }
}
