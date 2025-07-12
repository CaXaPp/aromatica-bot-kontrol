package org.example.aromatica.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employee")
public class Employee {

    @Id
    private String username;

    @Column
    private String fullName;

    @Column
    private Integer lateCountUnder20Min;

    @Column
    private Integer totalLateMinutes;

    @Column
    private Integer fineSum;

    @Column
    private boolean disqualified;

    @Column
    private LocalDateTime lastArrival;
}
