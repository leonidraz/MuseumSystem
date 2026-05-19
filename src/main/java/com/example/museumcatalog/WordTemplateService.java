package com.example.museumcatalog;

import com.example.museumcatalog.Models.*;
import com.example.museumcatalog.Models.Document;
import org.docx4j.Docx4J;
import org.docx4j.model.datastorage.migration.VariablePrepare;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class WordTemplateService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    // ================= EXPORT =================

    public static void export(FullDocumentData data, Path savePath) throws Exception {
        WordprocessingMLPackage pkg = loadTemplate(data);
        VariablePrepare.prepare(pkg);

        Map<String, String> values = buildValues(data);
        pkg.getMainDocumentPart().variableReplace(values);

        fillTableForType(pkg, data);

        Docx4J.save(pkg, savePath.toFile());
    }

    // ================= LOAD TEMPLATE =================

    private static WordprocessingMLPackage loadTemplate(FullDocumentData data) throws Exception {
        String template = resolveTemplate(data.getDocument().getDocType());
        String path = "/com/example/museumcatalog/templates/" + template;

        var stream = WordTemplateService.class.getResourceAsStream(path);
        if (stream == null) {
            throw new RuntimeException("Шаблон не найден: " + template);
        }
        return WordprocessingMLPackage.load(stream);
    }

    private static String resolveTemplate(String type) {
        return switch (type) {
            case "Акт ПП на ВХ" -> "temporary_storage.docx";
            case "Акт на рассмотрение ЭФЗК" -> "efzk_consideration.docx";
            case "Протокол заседания ЭФЗК" -> "efzk_protocol.docx";
            case "Акт ПП на ПП" -> "permanent_acceptance.docx";
            case "Договор пожертвования" -> "donation_contract.docx";
            case "Акт ПП на ОХ" -> "responsible_storage.docx";
            case "Акт внутримузейной передачи" -> "internal_transfer.docx";
            case "Акт ВП на временное хранение" -> "issuance_temporary.docx";
            default -> "default.docx";
        };
    }

    // ================= VALUES =================

    private static Map<String, String> buildValues(FullDocumentData data) {
        Map<String, String> map = new HashMap<>();
        Document d = data.getDocument();

        // Общие переменные
        map.put("docNumber", safe(d.getDocNumber()));
        map.put("conductedDate", d.getConductedDate() != null ? d.getConductedDate().format(DATE_FORMAT) : "");
        map.put("exhibitsCount", String.valueOf(data.getExhibits().size()));

        // Владелец (полная строка)
        map.put("owner", d.getOwner());

        // Сотрудники (список ФИО и должностей)
        map.put("employees", formatEmployees(data.getEmployees()));

        // Специфичные переменные по типу документа
        buildValuesForType(map, data);

        return map;
    }

    private static void buildValuesForType(Map<String, String> map, FullDocumentData data) {
        String type = data.getDocument().getDocType();

        switch (type) {
            case "Протокол заседания ЭФЗК":
                EfzkData protocol = data.getEfzkData();
                if (protocol != null) {
                    map.put("period", formatPeriod(protocol.getStartDate(), protocol.getEndDate()));
                    map.put("fund", safe(protocol.getFundName()));
                    map.put("collection", safe(protocol.getCollectionName()));
                }
                break;

            case "Акт внутримузейной передачи":
                InternalTransferData transfer = data.getInternalTransferData();
                if (transfer != null) {
                    map.put("fromEmployee", safe(String.valueOf(transfer.getFromEmployeeId())));
                    map.put("toEmployee", safe(String.valueOf(transfer.getFromEmployeeId())));
                    map.put("transferPurpose", safe(transfer.getTransferPurpose()));
                }
                break;

            case "Акт ВП на временное хранение":
                TemporaryStorageData issuance = data.getTemporaryStorageData();
                if (issuance != null) {
                    map.put("recipientType", safe(issuance.getReceiverType()));
                    map.put("recipientName", safe(issuance.getReceiverName()));
                    map.put("recipientIdentifier", safe(issuance.getReceiverIdentifier()));
                    map.put("recipientAddress", safe(issuance.getReceiverAddress()));
                    map.put("admissionPurpose", safe(issuance.getAdmissionPurpose()));
                }
                break;

            case "Договор пожертвования":
                // Список предметов через запятую
                map.put("exhibitsList", buildExhibitsList(data.getExhibits()));
                break;
        }
    }

    // ================= TABLE FILLING =================

    private static void fillTableForType(WordprocessingMLPackage pkg, FullDocumentData data) throws Exception {
        String type = data.getDocument().getDocType();

        switch (type) {
            case "Акт ПП на ВХ":
            case "Акт на рассмотрение ЭФЗК":
                fillStandardTable(pkg, data); // №, Наименование, Сохранность, Материал/Техника/Размер
                break;

            case "Протокол заседания ЭФЗК":
                fillEfzkProtocolTable(pkg, data); // №, Наименование, Описание, Сохранность, Цель
                break;

            case "Акт ПП на ПП":
                fillPermanentAcceptanceTable(pkg, data); // №, Наименование, КП, Мат/Тех/Размер, Надписи, Сохранность, Учёт
                break;

            case "Акт ПП на ОХ":
                fillResponsibleStorageTable(pkg, data); // №, Учёт, Наименование, Цель
                break;

            case "Акт внутримузейной передачи":
                fillInternalTransferTable(pkg, data); // №, Учёт, Наименование
                break;

            case "Акт ВП на временное хранение":
                fillIssuanceTable(pkg, data); // №, Наименование/Мат/Размер, Учёт, Сохранность, Кол-во
                break;

            case "Договор пожертвования":
                // Таблица не заполняется, используется переменная ${exhibitsList}
                break;

            default:
                fillStandardTable(pkg, data);
        }
    }

    // --- Стандартная таблица (Акт ПП на ВХ, Акт на рассмотрение ЭФЗК) ---
    private static void fillStandardTable(WordprocessingMLPackage pkg, FullDocumentData data) throws Exception {
        fillTable(pkg, data, (row, idx, ex) -> {
            setCell(row, 0, String.valueOf(idx));
            setCell(row, 1, buildNameWithDesc(ex));
            setCell(row, 2, safe(ex.getCondition()));
            setCell(row, 3, buildMaterialInfo(ex));
        });
    }

    // --- Протокол ЭФЗК ---
    private static void fillEfzkProtocolTable(WordprocessingMLPackage pkg, FullDocumentData data) throws Exception {
        fillTable(pkg, data, (row, idx, ex) -> {
            setCell(row, 0, String.valueOf(idx));
            setCell(row, 1, buildNameWithDesc(ex));
            setCell(row, 2, safe(ex.getCondition()));
            setCell(row, 3, safe(ex.getMuseumValue()));
            setCell(row, 4, safe(ex.getOwnerFio()));
        });
    }

    // --- Акт ПП на ПП ---
    private static void fillPermanentAcceptanceTable(WordprocessingMLPackage pkg, FullDocumentData data) throws Exception {
        fillTable(pkg, data, (row, idx, ex) -> {
            setCell(row, 0, String.valueOf(idx));
            setCell(row, 1, buildNameWithDesc(ex));
            setCell(row, 2, safe(ex.getNumberKP()));
            setCell(row, 3, buildMaterialInfo(ex));
            setCell(row, 4, safe(ex.getInscriptions())); // Надписи, клейма
            setCell(row, 5, safe(ex.getCondition()));
            setCell(row, 6, safe(ex.getNumberKP())); // Учётное обозначение
        });
    }

    // --- Акт ПП на ОХ ---
    private static void fillResponsibleStorageTable(WordprocessingMLPackage pkg, FullDocumentData data) throws Exception {
        fillTable(pkg, data, (row, idx, ex) -> {
            setCell(row, 0, safe(ex.getNumberKP()));
            setCell(row, 1, buildNameWithDesc(ex));
        });
    }

    // --- Акт внутримузейной передачи ---
    private static void fillInternalTransferTable(WordprocessingMLPackage pkg, FullDocumentData data) throws Exception {
        fillTable(pkg, data, (row, idx, ex) -> {
            setCell(row, 0, String.valueOf(idx));
            setCell(row, 1, safe(ex.getNumberKP()));
            setCell(row, 2, buildNameWithDesc(ex));
        });
    }

    // --- Акт ВП на временное хранение ---
    private static void fillIssuanceTable(WordprocessingMLPackage pkg, FullDocumentData data) throws Exception {
        fillTable(pkg, data, (row, idx, ex) -> {
            setCell(row, 0, String.valueOf(idx));
            setCell(row, 1, buildNameWithMaterialAndSize(ex));
            setCell(row, 2, safe(ex.getNumberKP()));
            setCell(row, 3, safe(ex.getCondition()));
            setCell(row, 4, "1"); // Количество
        });
    }

    // --- Универсальный метод заполнения таблицы ---
    @FunctionalInterface
    private interface RowFiller {
        void fill(Tr row, int index, Exhibit ex);
    }

    private static void fillTable(WordprocessingMLPackage pkg, FullDocumentData data, RowFiller filler) throws Exception {
        List<Object> tables = pkg.getMainDocumentPart().getJAXBNodesViaXPath("//w:tbl", true);
        if (tables.isEmpty()) return;

        for (Object obj : tables) {
            Tbl table = (obj instanceof Tbl t) ? t : (obj instanceof jakarta.xml.bind.JAXBElement<?> el ? (Tbl) el.getValue() : null);
            if (table == null) continue;

            List<Object> rows = table.getContent();
            if (rows.size() < 2) continue;

            // Поиск строки-шаблона
            Tr templateRow = null;
            for (int i = 1; i < rows.size(); i++) {
                Object r = rows.get(i);
                Object val = (r instanceof jakarta.xml.bind.JAXBElement<?> el) ? el.getValue() : r;
                if (val instanceof Tr tr) {
                    templateRow = tr;
                    break;
                }
            }
            if (templateRow == null) continue;

            // Удаление старых данных
            while (rows.size() > 1) rows.remove(1);

            // Добавление новых строк
            int counter = 1;
            for (Exhibit ex : data.getExhibits()) {
                Tr newRow = (Tr) org.docx4j.XmlUtils.deepCopy(templateRow);
                filler.fill(newRow, counter++, ex);
                rows.add(newRow);
            }
        }
    }

    // ================= HELPERS =================

    private static String buildNameWithDesc(Exhibit ex) {
        String name = safe(ex.getName());
        String desc = safe(ex.getDescription());
        return desc.isEmpty() ? name : name + ", " + desc;
    }

    private static String buildMaterialInfo(Exhibit ex) {
        List<String> parts = new ArrayList<>();
        addIfNotEmpty(parts, safe(ex.getMaterial()));
        addIfNotEmpty(parts, safe(ex.getTechnique()));

        String l = safe(String.valueOf(ex.getLength()));
        String w = safe(String.valueOf(ex.getWidth()));
        String h = safe(String.valueOf(ex.getHeight()));
        if (!l.isEmpty() || !w.isEmpty() || !h.isEmpty()) {
            parts.add(String.format("%s × %s × %s", l, w, h));
        }

        String unit = safe(ex.getUnitSizes());
        if (!unit.isEmpty() && !parts.isEmpty() && parts.get(parts.size() - 1).contains("×")) {
            int last = parts.size() - 1;
            parts.set(last, parts.get(last) + " " + unit);
        } else if (!unit.isEmpty()) {
            parts.add(unit);
        }

        return String.join(", ", parts);
    }

    private static String buildNameWithMaterialAndSize(Exhibit ex) {
        List<String> parts = new ArrayList<>();
        parts.add(buildNameWithDesc(ex));
        String matInfo = buildMaterialInfo(ex);
        if (!matInfo.isEmpty()) parts.add(matInfo);
        return String.join(", ", parts);
    }

    private static String buildExhibitsList(List<Exhibit> exhibits) {
        return exhibits.stream()
                .map(ex -> safe(ex.getName()) + " (1 шт.)")
                .collect(Collectors.joining(", "));
    }

//    private static String formatOwner(Owner owner) {
//        if (owner == null) return "";
//        StringBuilder sb = new StringBuilder();
//        sb.append(safe(owner.getLastName())).append(" ")
//                .append(safe(owner.getFirstName())).append(" ")
//                .append(safe(owner.getMiddleName()));
//        if (owner.getAddress() != null && !owner.getAddress().isEmpty()) {
//            sb.append(", адрес: ").append(owner.getAddress());
//        }
//        if (owner.getPassportSeries() != null && owner.getPassportNumber() != null) {
//            sb.append(", паспорт: ").append(owner.getPassportSeries()).append(" ").append(owner.getPassportNumber());
//            if (owner.getIssuedBy() != null) sb.append(", выдан: ").append(owner.getIssuedBy());
//            if (owner.getDateOfIssue() != null) sb.append(", дата выдачи: ").append(owner.getDateOfIssue().format(DATE_FORMAT));
//        }
//        return sb.toString();
//    }

    private static String formatEmployees(List<Employee> employees) {
        return employees.stream()
                .map(e -> safe(e.getLastName()) + " " + safe(e.getFirstName()) + " " + safe(e.getMiddleName()) + " (" + safe(e.getPosition()) + ")")
                .collect(Collectors.joining(", "));
    }

    private static String formatPeriod(java.time.LocalDate start, java.time.LocalDate end) {
        if (start == null && end == null) return "";
        if (start != null && end != null) return start.format(DATE_FORMAT) + " – " + end.format(DATE_FORMAT);
        if (start != null) return "с " + start.format(DATE_FORMAT);
        return "по " + end.format(DATE_FORMAT);
    }

    private static void addIfNotEmpty(List<String> list, String value) {
        if (value != null && !value.isEmpty()) list.add(value);
    }

    private static String safe(String v) {
        return v == null ? "" : v;
    }

    // ================= CELL =================

    private static void setCell(Tr row, int index, String value) {
        if (row.getContent().size() <= index) return;
        Object cellObj = row.getContent().get(index);
        if (cellObj instanceof jakarta.xml.bind.JAXBElement<?> jaxb) cellObj = jaxb.getValue();
        if (!(cellObj instanceof Tc cell)) return;

        RPr preservedRPr = extractRPr(cell);
        cell.getContent().clear();

        P p = new P();
        R r = new R();
        if (preservedRPr != null) r.setRPr(org.docx4j.XmlUtils.deepCopy(preservedRPr));

        Text t = new Text();
        t.setValue(value);
        t.setSpace("preserve");

        r.getContent().add(t);
        p.getContent().add(r);
        cell.getContent().add(p);
    }

    private static RPr extractRPr(Tc cell) {
        for (Object cc : cell.getContent()) {
            Object pObj = (cc instanceof jakarta.xml.bind.JAXBElement<?> j) ? j.getValue() : cc;
            if (pObj instanceof P p) {
                for (Object pc : p.getContent()) {
                    Object rObj = (pc instanceof jakarta.xml.bind.JAXBElement<?> j) ? j.getValue() : pc;
                    if (rObj instanceof R r && r.getRPr() != null) return r.getRPr();
                }
            }
        }
        return null;
    }
}