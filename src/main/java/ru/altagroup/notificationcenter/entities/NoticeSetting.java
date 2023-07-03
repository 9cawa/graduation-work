package ru.altagroup.notificationcenter.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "notice_settings")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NoticeSetting {

    public NoticeSetting(Notice notice) {
        this.notice = notice;
    }

    @Id
    @GeneratedValue
    private UUID id;

    @Column
    @Enumerated(value = EnumType.STRING)
    private Notice notice;

    @Column(nullable = false)
    private Boolean byEmail = Boolean.TRUE;

    @Column(nullable = false)
    private Boolean bySms = Boolean.FALSE;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NoticeFrequency frequency = NoticeFrequency.DAILY;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", referencedColumnName = "id")
    private Recipient recipient;
}
