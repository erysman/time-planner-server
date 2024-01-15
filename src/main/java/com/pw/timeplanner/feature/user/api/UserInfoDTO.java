package com.pw.timeplanner.feature.user.api;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;

@Value
@Builder
public class UserInfoDTO implements Serializable {

    Boolean isInitialized;

}
