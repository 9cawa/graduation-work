package ru.altagroup.notificationcenter.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "recipients")
@Setter
@Getter
public class Recipient {
    @Id
    private UUID id;

    @Column
    private String fullName;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private RecipientType type;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String phone;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "recipient")
    private List<Station> stations = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "recipient")
    private List<Notification> notifications = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "recipient")
    @Setter(AccessLevel.NONE)
    private Dnd dnd;

    public void setDnd(Dnd dnd) {
        if (dnd == null) return;
        this.dnd = dnd;
        dnd.setRecipient(this);
    }

    @Setter(AccessLevel.NONE)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "recipient")
    private List<NoticeSetting> noticeSettings = new ArrayList<>();

    public void setNoticeSettings(List<NoticeSetting> noticeSettings) {
        if (noticeSettings.isEmpty()) return;
        this.noticeSettings = noticeSettings;
        noticeSettings.forEach(noticeSetting -> noticeSetting.setRecipient(this));
    }
}
