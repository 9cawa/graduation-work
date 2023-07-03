package ru.altagroup.notificationcenter.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.UUID;

@Entity
@Table(name = "dnd_settings")
@Setter
@Getter
public class Dnd {

    @Id
    @GeneratedValue
    private UUID id;
    @Column(nullable = false)
    private boolean isActive = Boolean.FALSE;
    @Column
    private LocalTime startTime;
    @Column
    private LocalTime endTime;
    @Column
    private String zoneId = ZoneId.of("UTC").toString();
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", referencedColumnName = "id")
    private Recipient recipient;
}
