package org.example.aromatica.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.aromatica.model.Arrival;
import org.example.aromatica.model.Employee;
import org.example.aromatica.repository.ArrivalRepository;
import org.example.aromatica.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.time.*;
import java.util.List;
import java.util.TimeZone;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DefaultAttendanceService {

    EmployeeRepository employeeRepository;
    ArrivalRepository arrivalRepository;

    static final ZoneId BISHKEK_ZONE = ZoneId.of("Asia/Bishkek");

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone(BISHKEK_ZONE));
    }

    public String processArrival(String username, String fullName, byte[] photo) {
        ZonedDateTime nowZoned = ZonedDateTime.now(BISHKEK_ZONE);
        LocalDate today = nowZoned.toLocalDate();
        LocalDateTime now = nowZoned.toLocalDateTime();

        LocalTime deadlineTime = LocalTime.of(11, 0);
        LocalDateTime deadline = LocalDateTime.of(today, deadlineTime);

        boolean alreadyArrivedToday = arrivalRepository.existsByUsernameAndArrivalTimeBetween(
                username,
                today.atStartOfDay(),
                today.atTime(23, 59, 59)
        );

        if (alreadyArrivedToday) {
            return "Ты уже зарегистрировал приход сегодня.";
        }

        int lateMinutes = now.isAfter(deadline)
                ? (int) Duration.between(deadline, now).toMinutes()
                : 0;

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
