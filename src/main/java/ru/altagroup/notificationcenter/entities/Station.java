package ru.altagroup.notificationcenter.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "stations")
@Setter
@Getter
public class Station {
    @Id
    private UUID id;
    @Column
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", referencedColumnName = "id")
    private Recipient recipient;
}
