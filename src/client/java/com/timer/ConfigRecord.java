package com.timer;

import java.util.ArrayList;
import java.util.List;

public record ConfigRecord(boolean enabled, List<String> regexFilters) {
    public ConfigRecord {
        regexFilters = (regexFilters != null) ? new ArrayList<>(regexFilters) : new ArrayList<>();
    }
}
