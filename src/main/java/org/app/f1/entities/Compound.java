package org.app.f1.entities;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum Compound {
    SOFT,
    MEDIUM,
    HARD,
    INTERMEDIATE,
    WET,
    TEST_UNKNOWN,
    @JsonEnumDefaultValue
    UNKNOWN;
}
