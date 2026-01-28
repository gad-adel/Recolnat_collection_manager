package org.recolnat.collection.manager.common.mapper;

import io.recolnat.model.UserDashboardDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.recolnat.collection.manager.connector.api.domain.User;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = {UUID.class})
public interface UserMapper {

    @Mapping(target = "id", source = "uid")
    @Mapping(target = "name", expression = "java(user.getLastName() + \" \" + user.getFirstName())")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "role", source = "role.name")
    UserDashboardDTO toUserDashboardDTO(User user);

    List<UserDashboardDTO> toUsersDashboardDTO(List<User> users);
}
