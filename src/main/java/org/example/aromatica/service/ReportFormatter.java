package org.example.aromatica.service;

import org.example.aromatica.model.Arrival;
import org.example.aromatica.model.Employee;

import java.util.List;

public class ReportFormatter {

    public static String formatReport(List<Employee> employees, DefaultAttendanceService service) {
        StringBuilder sb = new StringBuilder();

        for (Employee e : employees) {
            sb.append("Имя: @").append(e.getUsername()).append("\n");

            boolean isDisqualified = Boolean.TRUE.equals(e.isDisqualified());
            sb.append("Статус: ").append(isDisqualified ? "Выбыл" : "В гонке").append("\n");

            int usedChances = e.getLateCountUnder20Min() != null ? e.getLateCountUnder20Min() : 0;
            int remainingChances = isDisqualified ? 0 : Math.max(0, 2 - usedChances);
            sb.append("Осталось шансов: ").append(remainingChances).append("\n");

            sb.append("Сумма штрафов: ")
                    .append(e.getFineSum() != null && e.getFineSum() > 0 ? e.getFineSum() + " сом" : "нет штрафов").append("\n");

            List<Arrival> arrivals = service.getArrivalHistory(e.getUsername());
            if (arrivals.isEmpty()) {
                sb.append("История: нет данных\n");
            } else {
                sb.append("История:\n");
                for (Arrival arrival : arrivals) {
                    String status = arrival.getLateMinutes() == 0 ? "вовремя" : arrival.getLateMinutes() + " мин опоздание";
                    int fine = (arrival.getLateMinutes() / 15) * 200;

                    sb.append("- ")
                            .append(arrival.getArrivalTime().toLocalDate())
                            .append(" — пришел в ")
                            .append(arrival.getArrivalTime().toLocalTime().withSecond(0).withNano(0))
                            .append(" (").append(status);

                    if (fine > 0) {
                        sb.append(", штраф ").append(fine).append(" сом");
                    }
                    sb.append(")\n");
                }
            }

            sb.append("\n");
        }

        return sb.toString();
    }
}
