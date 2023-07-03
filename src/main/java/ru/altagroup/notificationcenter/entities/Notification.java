package ru.altagroup.notificationcenter.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue
    private UUID id;

    @Column
    @Enumerated(value = EnumType.STRING)
    private Channel channel;

    @Column
    private Long timestamp;

    @Column
    private boolean delivered;

    @Column
    private EventType event;

    @Column
    private String text;

    @Column
    @Enumerated(value = EnumType.STRING)
    private Notice notice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", referencedColumnName = "id")
    private Recipient recipient;
}
