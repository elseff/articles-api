package com.elseff.project.web.api.modules.user.dto.mapper;

import com.elseff.project.persistense.User;
import com.elseff.project.web.api.modules.article.dto.ArticleDto;
import com.elseff.project.web.api.modules.auth.dto.AuthRegisterRequest;
import com.elseff.project.web.api.modules.user.dto.UserDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserDtoMapper {

    public UserDto mapUserEntityToDtoForAdmin(User user) {
        return UserDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .country(user.getCountry())
                .registrationDate(user.getRegistrationDate())
                .updatedAt(user.getUpdatedAt())
                .articles(user.getArticles()
                        .stream()
                        .map(articleEntity ->
                                ArticleDto.builder()
                                        .id(articleEntity.getId())
                                        .title(articleEntity.getTitle())
                                        .description(articleEntity.getDescription())
                                        .build())
                        .collect(Collectors.toList()))
                .roles(user.getRoles())
                .build();
    }

    public UserDto mapUserEntityToDtoForUser(User user) {
        return UserDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .country(user.getCountry())
                .articles(user.getArticles()
                        .stream()
                        .map(articleEntity ->
                                ArticleDto.builder()
                                        .id(articleEntity.getId())
                                        .title(articleEntity.getTitle())
                                        .description(articleEntity.getDescription())
                                        .build())
                        .collect(Collectors.toList()))
                .build();
    }

    public UserDto mapUserEntityToSimpleDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    public User mapAuthRequestToUserEntity(AuthRegisterRequest request) {
        return User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .country(request.getCountry())
                .password(request.getPassword())
                .build();

    }

    public List<UserDto> mapListUserEntityToSimpleDto(List<User> users) {
        return users.stream()
                .map(this::mapUserEntityToSimpleDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> mapListUserEntityToDtoForUser(List<User> users) {
        return users.stream()
                .map(this::mapUserEntityToDtoForUser)
                .collect(Collectors.toList());
    }

    public List<UserDto> mapListUserEntityToDtoForAdmin(List<User> users) {
        return users.stream()
                .map(this::mapUserEntityToDtoForAdmin)
                .collect(Collectors.toList());
    }
}
