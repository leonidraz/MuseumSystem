package com.example.museumcatalog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateTimeUtil {
    public static final DateTimeFormatter DOC_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    public static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static final DateTimeFormatter TIME_ONLY_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

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

    // Форматирование только времени
    public static String formatTimeOnly(LocalTime dateTime) {
        return dateTime != null ? dateTime.format(TIME_ONLY_FORMATTER) : "—";
    }

    // Формат для документов:
    // от «25» мая 2026 г.
    public static String formatDocumentDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        LocalDate date = dateTime.toLocalDate();
        String month = switch (date.getMonth()) {
            case JANUARY -> "января";
            case FEBRUARY -> "февраля";
            case MARCH -> "марта";
            case APRIL -> "апреля";
            case MAY -> "мая";
            case JUNE -> "июня";
            case JULY -> "июля";
            case AUGUST -> "августа";
            case SEPTEMBER -> "сентября";
            case OCTOBER -> "октября";
            case NOVEMBER -> "ноября";
            case DECEMBER -> "декабря";
        };

        return String.format("«%02d» %s %d г.", date.getDayOfMonth(), month, date.getYear());
    }
}

