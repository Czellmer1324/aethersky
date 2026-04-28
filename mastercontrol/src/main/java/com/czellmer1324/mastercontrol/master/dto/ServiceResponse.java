package com.czellmer1324.mastercontrol.master.dto;

public record ServiceResponse(Object response, boolean successful, String reasonForFail) {
}
