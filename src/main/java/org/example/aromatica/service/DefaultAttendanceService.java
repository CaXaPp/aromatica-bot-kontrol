package org.example.aromatica.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.aromatica.model.Arrival;
import org.example.aromatica.model.Employee;
import org.example.aromatica.repository.ArrivalRepository;
import org.example.aromatica.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DefaultAttendanceService {
    EmployeeRepository employeeRepository;
    ArrivalRepository arrivalRepository;

    public String processArrival(String username, String fullName, byte[] photo) {
        LocalDateTime now = LocalDateTime.now();
        LocalTime deadline = LocalTime.of(11, 0);
        LocalDateTime targetTime = LocalDateTime.of(LocalDate.now(), deadline);

        boolean alreadyArrivedToday = arrivalRepository.existsByUsernameAndArrivalTimeBetween(
                username,
                LocalDate.now().atStartOfDay(),
                LocalDate.now().atTime(23, 59, 59)
        );

        if (alreadyArrivedToday) {
            return "Ты уже зарегистрировал приход сегодня.";
        }

        int lateMinutes = (int) Duration.between(targetTime, now).toMinutes();
        lateMinutes = Math.max(0, lateMinutes);

        Arrival arrival = new Arrival();
        arrival.setUsername(username);
        arrival.setArrivalTime(now);
        arrival.setLateMinutes(lateMinutes);
        arrival.setPhoto(photo);
        arrivalRepository.save(arrival);

        Employee emp = employeeRepository.findById(username).orElseGet(() -> {
            Employee e = new Employee();
            e.setUsername(username);
            e.setFullName(fullName);
            e.setLateCountUnder20Min(0);
            e.setTotalLateMinutes(0);
            e.setFineSum(0);
            e.setDisqualified(false);
            return e;
        });

        emp.setLastArrival(now);

        if (lateMinutes == 0) {
            employeeRepository.save(emp);
            return "Ты красавчик, пришёл вовремя!";
        } else if (lateMinutes <= 20 && emp.getLateCountUnder20Min() < 2) {
            emp.setLateCountUnder20Min(emp.getLateCountUnder20Min() + 1);
            emp.setTotalLateMinutes(emp.getTotalLateMinutes() + lateMinutes);
            int fine = (lateMinutes / 15) * 200;
            emp.setFineSum(emp.getFineSum() + fine);
            employeeRepository.save(emp);
            int remainingChances = 2 - emp.getLateCountUnder20Min();
            return String.format("Ты немного опоздал (%d мин), но у тебя ещё есть шанс! Осталось шансов: %d", lateMinutes, remainingChances);
        } else {
            emp.setDisqualified(true);
            emp.setTotalLateMinutes(emp.getTotalLateMinutes() + lateMinutes);
            int fine = (lateMinutes / 15) * 200;
            emp.setFineSum(emp.getFineSum() + fine);
            employeeRepository.save(emp);
            return String.format("Ты выбыл из гонки за бонус. Опоздание %d мин. Штраф %d сом.", lateMinutes, fine);
        }

    }


    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee getEmployee(String username) {
        return employeeRepository.findById(username).orElse(null);
    }

    public void resetAll() {
        employeeRepository.deleteAll();
        arrivalRepository.deleteAll();
    }

    @Transactional
    public List<Arrival> getArrivalHistory(String username) {
        return arrivalRepository.findAllByUsernameOrderByArrivalTimeAsc(username);
    }
}



