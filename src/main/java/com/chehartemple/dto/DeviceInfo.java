package com.chehartemple.dto;

import lombok.Data;

@Data
public class DeviceInfo {
    private String deviceId;
    private String deviceName;
    private String deviceModel;
    private String osName;
    private String osVersion;
    private String appVersion;
}
