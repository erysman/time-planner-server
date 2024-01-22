package com.pw.timeplanner.feature.user.api;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

import java.io.Serializable;

@Value
@Builder
public class UserInfoDTO implements Serializable {
    @NotNull
    Boolean isInitialized;

}
