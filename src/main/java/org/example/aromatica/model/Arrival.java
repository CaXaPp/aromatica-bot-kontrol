package org.example.aromatica.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "arrival")
public class Arrival {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String username;

    LocalDateTime arrivalTime;

    Integer lateMinutes;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "photo")
    private byte[] photo;


}
