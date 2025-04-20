package com.timer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public record ConfigRecord(boolean enabled, List<String> regexFilters) {
    public ConfigRecord {
        regexFilters =
                (regexFilters != null)
                        ? new CopyOnWriteArrayList<>(regexFilters)
                        : new CopyOnWriteArrayList<>();
    }
}
