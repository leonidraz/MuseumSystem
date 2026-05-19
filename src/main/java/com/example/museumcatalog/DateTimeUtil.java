package com.example.museumcatalog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateTimeUtil {
    public static final DateTimeFormatter DOC_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    public static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    // Получение текущей даты с точностью до секунды
    public static LocalDateTime nowWithSecondPrecision() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    // Форматирование для отображения в UI (с секундами)
    public static String formatForDisplay(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DOC_DATE_FORMATTER) : "— — —";
    }

    // Форматирование только даты (без времени)
    public static String formatDateOnly(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toLocalDate().format(DATE_ONLY_FORMATTER) : "—";
    }
}

