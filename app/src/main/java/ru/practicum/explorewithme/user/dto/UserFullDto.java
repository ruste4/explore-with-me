package ru.practicum.explorewithme.user.dto;

import lombok.Data;

@Data
public class UserFullDto {

    private Long id;

    private String email;

    private String name;

    private boolean activated;

}
