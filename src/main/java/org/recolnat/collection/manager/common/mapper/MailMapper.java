package org.recolnat.collection.manager.common.mapper;


import org.mapstruct.Mapper;
import org.recolnat.collection.manager.api.domain.Mail;
import org.recolnat.collection.manager.repository.entity.MailJPA;

@Mapper(componentModel = "spring")
public interface MailMapper {

    MailJPA toJPA(Mail mail);
}
