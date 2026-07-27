package org.app.f1.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaceResponse(

        @JsonProperty("driver")
        DriverResponse driver,

        @JsonProperty("pace")
        Double pace
) {
}
