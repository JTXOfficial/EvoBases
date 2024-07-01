package me.jtx.evobases.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Global {
    public String todayDate() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return today.format(formatter);
    }
}
