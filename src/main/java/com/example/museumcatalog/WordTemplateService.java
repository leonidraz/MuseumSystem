package com.example.museumcatalog;

import com.example.museumcatalog.Models.*;
import com.example.museumcatalog.Models.Document;
import com.example.museumcatalog.Storages.EmployeeRepository;
import com.example.museumcatalog.Storages.OwnerRepository;
import org.docx4j.Docx4J;
import org.docx4j.model.datastorage.migration.VariablePrepare;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class WordTemplateService {
    public static void export(FullDocumentData data, Path savePath) throws Exception {
        WordprocessingMLPackage pkg = loadTemplate(data);
        VariablePrepare.prepare(pkg);

        Map<String, String> values = buildValues(data);
        pkg.getMainDocumentPart().variableReplace(values);

        fillTableForType(pkg, data);

        Docx4J.save(pkg, savePath.toFile());
    }

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

    private static Map<String, String> buildValues(FullDocumentData data) {
        Map<String, String> map = new HashMap<>();
        Document d = data.getDocument();

        map.put("docNumber", safe(d.getDocNumber()));
        map.put("conductedDate", DateTimeUtil.formatDocumentDate(d.getConductedDate()));
        map.put("exhibitsCount", String.valueOf(data.getExhibits().size()));

        map.put("employees", formatEmployeesDefault(data.getEmployees()));
        map.put("owner", formatOwnerFioOnly(data.getOwner()));

        buildValuesForType(map, data);

        return map;
    }

    private static void buildValuesForType(Map<String, String> map, FullDocumentData data) {
        String type = data.getDocument().getDocType();

        switch (type) {
            case "Акт ПП на ВХ":
                map.put("employees", formatEmployeesDefault(data.getEmployees()));
                map.put("owner", formatOwnerFull(data.getOwner()));
                break;
            case "Акт на рассмотрение ЭФЗК":
                Employee keeper = findEmployeeByRole(data.getEmployees(), DocumentEmployeeRole.KEEPER);
                map.put("keeper", keeper != null ? formatEmployeeFioOnly(keeper) : "");
                map.put("owner", formatOwnerFioOnly(data.getOwner()));
                map.put("acceptedBy", keeper != null ? formatEmployeeShort(keeper) : "");
                map.put("presentedBy", formatEmployeesParticipant(data.getEmployees()));
                break;
            case "Протокол заседания ЭФЗК":
                EfzkData protocol = data.getEfzkData();
                if (protocol != null) {
                    map.put("period", formatPeriod(protocol.getStartDate(), protocol.getEndDate()));
                    map.put("fund", safe(protocol.getFundName()));
                    map.put("collection", safe(protocol.getCollectionName()));
                }
                Employee chairman = findEmployeeByRole(data.getEmployees(), DocumentEmployeeRole.CHAIRMAN);
                map.put("chairman", chairman != null ? formatEmployeeShort(chairman) : "");
                Employee secretary = findEmployeeByRole(data.getEmployees(), DocumentEmployeeRole.SECRETARY);
                map.put("secretary", secretary != null ? formatEmployeeShort(secretary) : "");
                map.put("employees", formatEmployeesAll(data.getEmployees()));
                map.put("chairmanSignature", chairman != null ? formatEmployeeSignatureLine(chairman) : "");
                map.put("secretarySignature", secretary != null ? formatEmployeeSignatureLine(secretary) : "");
                List<String> membersSignatures = new ArrayList<>();
                for (DocumentEmployeeRelation rel : data.getEmployees()) {
                    if (rel.getRole() == DocumentEmployeeRole.PARTICIPANT) {
                        Employee emp = rel.getEmployee();
                        membersSignatures.add(formatEmployeeSignatureLine(emp));
                    }
                }
                map.put("membersSignatures", String.join("\n", membersSignatures));
                map.put("employees", formatEmployeesAll(data.getEmployees()));
                break;
            case "Акт ПП на ПП":
                map.put("employees", formatEmployeesParticipant(data.getEmployees()));
                map.put("owner", formatOwnerFull(data.getOwner()));
                break;
            case "Договор пожертвования":
                map.put("ownerFio", formatOwnerFioOnly(data.getOwner()));
                map.put("ownerPassport", formatOwnerPassportOnly(data.getOwner()));
                Employee deputy = findEmployeeByRole(data.getEmployees(), DocumentEmployeeRole.DEPUTY_DIRECTOR);
                map.put("museumHead", deputy != null ? formatEmployeeFioOnly(deputy) : "");
                map.put("exhibitList", buildExhibitsListWithDesc(data.getExhibits()));
                map.put("ownerWithAddress", formatOwnerWithAddress(data.getOwner()));
                break;
            case "Акт ПП на ОХ":
                map.put("owner", formatOwnerFull(data.getOwner()));
                Employee acceptedBy = findEmployeeByRole(data.getEmployees(), DocumentEmployeeRole.ACCEPTED_BY);
                map.put("ownerOnlyFio", formatOwnerFioOnly(data.getOwner()));
                map.put("acceptedBy", acceptedBy != null ? formatEmployeeShort(acceptedBy) : "");
                map.put("presentedBy", formatEmployeesParticipant(data.getEmployees()));
                break;
            case "Акт внутримузейной передачи":
                InternalTransferData transfer = data.getInternalTransferData();
                if (transfer != null) {
                    map.put("fromEmployee", buildEmployeeFio(transfer.getFromEmployeeId()));
                    map.put("toEmployee", buildEmployeeFio(transfer.getToEmployeeId()));
                    map.put("transferPurpose", safe(transfer.getTransferPurpose()));
                    map.put("presentedBy", formatEmployeesParticipant(data.getEmployees()));
                }
                break;
            case "Акт ВП на временное хранение":
                TemporaryStorageData issuance = data.getTemporaryStorageData();
                if (issuance != null) {
                    map.put("recipientName", safe(issuance.getReceiverName()));
                    map.put("recipientIdentifier", safe(issuance.getReceiverIdentifier()));
                    map.put("recipientAddress", safe(issuance.getReceiverAddress()));
                    map.put("admissionPurpose", safe(issuance.getAdmissionPurpose()));
                    map.put("presentedBy", formatEmployeesParticipant(data.getEmployees()));
                }
                break;
        }
    }
    private static void fillTableForType(WordprocessingMLPackage pkg, FullDocumentData data) throws Exception {
        String type = data.getDocument().getDocType();
        switch (type) {
            case "Акт ПП на ВХ":
                fillTemporaryStorageTable(pkg, data);
                break;
            case "Акт на рассмотрение ЭФЗК":
                fillEfzkConsiderationTable(pkg, data);
                break;
            case "Протокол заседания ЭФЗК":
                fillEfzkProtocolTable(pkg, data);
                break;
            case "Акт ПП на ПП":
                fillPermanentAcceptanceTable(pkg, data);
                break;
            case "Акт ПП на ОХ":
                fillResponsibleStorageTable(pkg, data);
                break;
            case "Акт внутримузейной передачи":
                fillInternalTransferTable(pkg, data);
                break;
            case "Акт ВП на временное хранение":
                fillIssuanceTable(pkg, data);
                break;
            case "Договор пожертвования":
                // Таблица не заполняется
                break;
            default:
                fillStandardTable(pkg, data);
        }
    }

    private static void fillTemporaryStorageTable(WordprocessingMLPackage pkg, FullDocumentData data) throws Exception {
        fillTable(pkg, data, (row, idx, ex) -> {
            setCell(row, 0, String.valueOf(idx));
            setCell(row, 1, buildNameWithDesc(ex));
            setCell(row, 2, safe(ex.getCondition()));
            setCell(row, 3, buildMaterialInfo(ex));
        });
    }
    private static void fillEfzkConsiderationTable(WordprocessingMLPackage pkg, FullDocumentData data) throws Exception {
        fillTable(pkg, data, (row, idx, ex) -> {
            setCell(row, 0, String.valueOf(idx));
            setCell(row, 1, safe(ex.getName()));
            setCell(row, 2, safe(ex.getCondition()));
            setCell(row, 3, buildMaterialTechnique(ex));
        });
    }
    private static void fillEfzkProtocolTable(WordprocessingMLPackage pkg, FullDocumentData data) throws Exception {
        fillTable(pkg, data, (row, idx, ex) -> {
            setCell(row, 0, String.valueOf(idx));
            setCell(row, 1, safe(ex.getName()));
            setCell(row, 2, safe(ex.getCondition()));
            setCell(row, 3, safe(ex.getMuseumValue()));
            setCell(row, 4, buildOwnerWithAddress(ex));
        });
    }
    private static void fillPermanentAcceptanceTable(WordprocessingMLPackage pkg, FullDocumentData data) throws Exception {
        fillTable(pkg, data, (row, idx, ex) -> {
            setCell(row, 0, String.valueOf(idx));
            setCell(row, 1, buildFullDescription(ex));
            setCell(row, 2, safe(ex.getCondition()));
            setCell(row, 3, safe(ex.getNumberKP()));
        });
    }
    private static void fillResponsibleStorageTable(WordprocessingMLPackage pkg, FullDocumentData data) throws Exception {
        fillTable(pkg, data, (row, idx, ex) -> {
            setCell(row, 0, safe(ex.getNumberKP()));
            setCell(row, 1, safe(ex.getName()));
        });
    }
    private static void fillInternalTransferTable(WordprocessingMLPackage pkg, FullDocumentData data) throws Exception {
        fillTable(pkg, data, (row, idx, ex) -> {
            setCell(row, 0, safe(ex.getNumberKP()));
            setCell(row, 1, safe(ex.getName()));
        });
    }
    private static void fillIssuanceTable(WordprocessingMLPackage pkg, FullDocumentData data) throws Exception {
        fillTable(pkg, data, (row, idx, ex) -> {
            setCell(row, 0, String.valueOf(idx));
            setCell(row, 1, buildNameWithMaterialAndSize(ex));
            setCell(row, 2, safe(ex.getNumberKP()));
            setCell(row, 3, safe(ex.getCondition()));
        });
    }
    private static void fillStandardTable(WordprocessingMLPackage pkg, FullDocumentData data) throws Exception {
        fillTable(pkg, data, (row, idx, ex) -> {
            setCell(row, 0, String.valueOf(idx));
            setCell(row, 1, buildNameWithDesc(ex));
            setCell(row, 2, safe(ex.getCondition()));
            setCell(row, 3, buildMaterialInfo(ex));
        });
    }

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

            while (rows.size() > 1) rows.remove(1);

            int counter = 1;
            for (Exhibit ex : data.getExhibits()) {
                Tr newRow = (Tr) org.docx4j.XmlUtils.deepCopy(templateRow);
                filler.fill(newRow, counter++, ex);
                rows.add(newRow);
            }
        }
    }

    private static String formatOwnerFull(Owner owner) {
        if (owner == null) return "";
        StringBuilder sb = new StringBuilder();

        String fio = String.format("%s %s %s",
                safe(owner.getLastName()),
                safe(owner.getFirstName()),
                safe(owner.getMiddleName())).trim();
        sb.append(fio);

        if (owner.getAddress() != null && !owner.getAddress().isEmpty()) {
            sb.append(", адрес: ").append(owner.getAddress());
        }

        String series = safe(owner.getPassportSeries());
        String number = safe(owner.getPassportNumber());
        if (!series.isEmpty() || !number.isEmpty()) {
            sb.append(", паспортные данные: ").append(series).append(", ").append(number);
        }

        if (owner.getIssuedBy() != null && !owner.getIssuedBy().isEmpty()) {
            sb.append(", ").append(owner.getIssuedBy());
        }

        if (owner.getDateOfIssue() != null) {
            sb.append(", ").append(owner.getDateOfIssue().format(DateTimeUtil.DATE_ONLY_FORMATTER));
        }

        return sb.toString();
    }

    private static String formatOwnerFioOnly(Owner owner) {
        if (owner == null) return "";
        return String.format("%s %s %s",
                safe(owner.getLastName()),
                safe(owner.getFirstName()),
                safe(owner.getMiddleName())).trim();
    }

    private static String formatOwnerWithAddress(Owner owner) {
        if (owner == null) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(formatOwnerFioOnly(owner));
        if (owner.getAddress() != null && !owner.getAddress().isEmpty()) {
            sb.append(", адрес: ").append(owner.getAddress());
        }
        return sb.toString();
    }

    private static String formatOwnerPassportOnly(Owner owner) {
        if (owner == null) return "";
        StringBuilder sb = new StringBuilder();
        String series = safe(owner.getPassportSeries());
        String number = safe(owner.getPassportNumber());
        if (!series.isEmpty() || !number.isEmpty()) {
            sb.append("паспорт: серия ").append(series).append(", номер ").append(number);
        }
        if (owner.getIssuedBy() != null && !owner.getIssuedBy().isEmpty()) {
            if (sb.length() > 0) sb.append("; ");
            sb.append("кем выдан: ").append(owner.getIssuedBy());
        }
        if (owner.getDateOfIssue() != null) {
            if (sb.length() > 0) sb.append("; ");
            sb.append("когда выдан: ").append(owner.getDateOfIssue().format(DateTimeUtil.DATE_ONLY_FORMATTER));
        }
        return sb.toString();
    }

    private static String formatEmployeeFioOnly(Employee emp) {
        if (emp == null) return "";
        String initials = "";
        if (emp.getFirstName() != null && !emp.getFirstName().isEmpty()) {
            initials += emp.getFirstName().charAt(0) + ".";
        }
        if (emp.getMiddleName() != null && !emp.getMiddleName().isEmpty()) {
            initials += emp.getMiddleName().charAt(0) + ".";
        }
        return emp.getLastName() + " " + initials;
    }

    private static String formatEmployeesAll(List<DocumentEmployeeRelation> employees) {
        return employees.stream()
                .map(e -> formatEmployeeShort(e.getEmployee()))
                .collect(Collectors.joining(", "));
    }
    private static String formatEmployeesParticipant(List<DocumentEmployeeRelation> employees) {
        return employees.stream()
                .filter(e -> e.getRole() == DocumentEmployeeRole.PARTICIPANT)
                .map(e -> formatEmployeeShort(e.getEmployee()))
                .collect(Collectors.joining(", "));
    }

    private static String formatEmployeeShort(Employee emp) {
        if (emp == null) return "";
        String initials = "";
        if (emp.getFirstName() != null && !emp.getFirstName().isEmpty()) {
            initials += emp.getFirstName().charAt(0) + ".";
        }
        if (emp.getMiddleName() != null && !emp.getMiddleName().isEmpty()) {
            initials += emp.getMiddleName().charAt(0) + ".";
        }
        return emp.getLastName() + " " + initials + " - " + emp.getPosition();
    }

    private static String formatEmployeesDefault(List<DocumentEmployeeRelation> employees) {
        return employees.stream()
                .map(e -> {
                    Employee emp = e.getEmployee();
                    return String.format("%s %s %s - %s",
                            safe(emp.getLastName()),
                            safe(emp.getFirstName()),
                            safe(emp.getMiddleName()),
                            safe(emp.getPosition()));
                })
                .collect(Collectors.joining(", "));
    }

    private static Employee findEmployeeByRole(List<DocumentEmployeeRelation> employees, DocumentEmployeeRole role) {
        return employees.stream()
                .filter(e -> e.getRole() == role)
                .map(DocumentEmployeeRelation::getEmployee)
                .findFirst()
                .orElse(null);
    }

    private static String formatEmployeeSignatureLine(Employee emp) {
        if (emp == null) return "";
        String position = safe(emp.getPosition());
        String fio = formatEmployeeFioOnly(emp);
        return position + " _______________ " + fio;
    }

    private static String formatPeriod(LocalDate start, LocalDate end) {
        if (start == null && end == null) return "";
        if (start != null && end != null) return "с " + start.format(DateTimeUtil.DATE_ONLY_FORMATTER) + " по " + end.format(DateTimeUtil.DATE_ONLY_FORMATTER);
        if (start != null) return "с " + start.format(DateTimeUtil.DATE_ONLY_FORMATTER);
        return "по " + end.format(DateTimeUtil.DATE_ONLY_FORMATTER);
    }

    private static String buildNameWithDesc(Exhibit ex) {
        String name = safe(ex.getName());
        String desc = safe(ex.getDescription());
        return desc.isEmpty() ? name : name + ", " + desc;
    }

    private static String buildMaterialInfo(Exhibit ex) {
        List<String> parts = new ArrayList<>();
        addIfNotEmpty(parts, safe(ex.getMaterial()));
        addIfNotEmpty(parts, safe(ex.getTechnique()));

        String l = formatDimension(ex.getLength());
        String w = formatDimension(ex.getWidth());
        String h = formatDimension(ex.getHeight());
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

    private static String buildMaterialTechnique(Exhibit ex) {
        List<String> parts = new ArrayList<>();
        addIfNotEmpty(parts, safe(ex.getMaterial()));
        addIfNotEmpty(parts, safe(ex.getTechnique()));
        return String.join(", ", parts);
    }

    private static String buildFullDescription(Exhibit ex) {
        List<String> parts = new ArrayList<>();
        addIfNotEmpty(parts, safe(ex.getName()));
        addIfNotEmpty(parts, safe(ex.getDescription()));
        addIfNotEmpty(parts, safe(ex.getDatingMaterial()));
        addIfNotEmpty(parts, safe(ex.getTechnique()));
        String l = formatDimension(ex.getLength());
        String w = formatDimension(ex.getWidth());
        String h = formatDimension(ex.getHeight());
        if (!l.isEmpty() || !w.isEmpty() || !h.isEmpty()) {
            parts.add(String.format("%s × %s × %s %s", l, w, h, safe(ex.getUnitSizes())));
        }
        addIfNotEmpty(parts, safe(ex.getInscriptions()));
        return String.join(", ", parts);
    }

    private static String buildNameWithMaterialAndSize(Exhibit ex) {
        List<String> parts = new ArrayList<>();
        parts.add(buildNameWithDesc(ex));
        String matInfo = buildMaterialInfo(ex);
        if (!matInfo.isEmpty()) parts.add(matInfo);
        return String.join(", ", parts);
    }

    private static String buildOwnerWithAddress(Exhibit ex) {
        Integer ownerId = ex.getOwnerId();
        if (ownerId == null) {
            return "Владелец не указан";
        }
        Owner foundOwner = null;
        for (Owner owner : OwnerRepository.getOwners()) {
            if (owner.getId() == ownerId) {
                foundOwner = owner;
                break;
            }
        }
        if (foundOwner != null) {
            return foundOwner.getFullFio() + ", " + foundOwner.getAddress();
        } else {
            return "нет";
        }
    }

    private static String buildEmployeeFio(Integer employeeId) {
        if (employeeId == null) {
            return "сотрудник не указан";
        }

        Employee foundEmployee = null;
        for (Employee employee : EmployeeRepository.getActiveEmployees()) {
            if (employee.getId() == employeeId) {
                foundEmployee = employee;
                break;
            }
        }

        if (foundEmployee != null) {
            return foundEmployee.getFullFio() + " - " + foundEmployee.getPosition();
        } else {
            return "нет";
        }
    }

    private static String buildExhibitsListWithDesc(List<Exhibit> exhibits) {
        return exhibits.stream()
                .map(ex -> {
                    String name = safe(ex.getName());
                    String desc = safe(ex.getDescription());
                    return desc.isEmpty() ? name : name + " (" + desc + ")";
                })
                .collect(Collectors.joining(", "));
    }

    private static String formatDimension(Double value) {
        if (value == null) return "";
        String str = String.valueOf(value);
        return str.endsWith(".0") ? str.substring(0, str.length() - 2) : str;
    }

    private static void addIfNotEmpty(List<String> list, String value) {
        if (value != null && !value.isEmpty()) list.add(value);
    }

    private static String safe(String v) {
        return v == null ? "" : v;
    }

    private static void setCell(Tr row, int index, String value) {
        if (row.getContent().size() <= index) return;
        Object cellObj = row.getContent().get(index);
        if (cellObj instanceof jakarta.xml.bind.JAXBElement<?> jaxb) cellObj = jaxb.getValue();
        if (!(cellObj instanceof Tc cell)) return;

        RPr preservedRPr = extractRPr(cell);
        cell.getContent().clear();

        P p = new P();
        String[] lines = safe(value).split("\n");

        for (int i = 0; i < lines.length; i++) {
            R r = new R();
            if (preservedRPr != null) r.setRPr(org.docx4j.XmlUtils.deepCopy(preservedRPr));

            Text t = new Text();
            t.setValue(lines[i]);
            t.setSpace("preserve");
            r.getContent().add(t);

            p.getContent().add(r);

            if (i < lines.length - 1) {
                p.getContent().add(new Br());
            }
        }
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