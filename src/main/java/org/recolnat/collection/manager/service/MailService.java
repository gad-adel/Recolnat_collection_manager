package org.recolnat.collection.manager.service;

import org.recolnat.collection.manager.api.domain.Mail;
import org.recolnat.collection.manager.repository.entity.MailJPA;

import java.util.Optional;

public interface MailService {
    void create(Mail mail);

    void send(MailJPA mail);

    Optional<MailJPA> findFirstPending();
}
