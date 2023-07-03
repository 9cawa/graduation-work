package ru.altagroup.notificationcenter.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.temporal.ChronoUnit;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum NoticeFrequency {
    HOURLY(ChronoUnit.HOURS), DAILY(ChronoUnit.DAYS), WEEKLY(ChronoUnit.WEEKS), MONTHLY(ChronoUnit.MONTHS), NEVER;

    private ChronoUnit chronoUnit;
}
