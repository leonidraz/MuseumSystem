package com.example.museumcatalog;

import com.example.museumcatalog.Models.Exhibit;
import org.docx4j.wml.Drawing;
import org.docx4j.Docx4J;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.model.datastorage.migration.VariablePrepare;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExhibitCardExport {
    public static void export(Exhibit e, File outputFile) throws Exception {

        WordprocessingMLPackage template =
                WordprocessingMLPackage.load(
                        ExhibitCardExport.class
                                .getResourceAsStream("/com/example/museumcatalog/templates/exhibit_card.docx")
                );

        VariablePrepare.prepare(template);

        Map<String, String> vars = new HashMap<>();

        vars.put("kpNumber", nvl(e.getNumberKP()));
        vars.put("name", nvl(e.getName()));
        vars.put("description", nvl(e.getDescription()));
        vars.put("location", nvl(e.getLocation()));
        vars.put("arrivalDate", nvl(String.valueOf(e.getArrivalDate())));
        vars.put("source", nvl(e.getSource()));
        vars.put("material", nvl(e.getMaterial()));
        vars.put("technique", nvl(e.getTechnique()));
        vars.put("sizeWeight", buildSizeAndWeight(e));
        vars.put("marks", nvl(e.getInscriptions()));
        vars.put("placeOfProduction", nvl(e.getPlaceOfProduction()));
        vars.put("productionTime", nvl(e.getProductionTime()));
        vars.put("condition", nvl(e.getCondition()));
        vars.put("conditionDetails", nvl(e.getConditionDetails()));
        vars.put("publication", nvl(e.getPublication()));
        vars.put("usage", nvl(e.getUsage()));
        vars.put("value", nvl(e.getMuseumValue()));

        vars.put("date", java.time.LocalDate.now().toString());
        vars.put("compiler", Service.getCurrentUser().getShortFio());

        replaceVariables(template, vars);
        String imagePath = System.getProperty("user.dir")
                + File.separator + "images"
                + File.separator + e.getPhoto();

        replaceImage(template, imagePath);

        Docx4J.save(template, outputFile);
    }

    private static void replaceImage(WordprocessingMLPackage pkg, String imagePath) throws Exception {
        if (imagePath == null || imagePath.isBlank()) return;

        File file = new File(imagePath);
        if (!file.exists()) return;

        byte[] bytes;
        try (FileInputStream fis = new FileInputStream(file)) {
            bytes = fis.readAllBytes();
        }
        BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(pkg, bytes);
        Inline inline = imagePart.createImageInline(file.getName(), "Exhibit Photo", 0, 1, false);

        long maxW = 6L * 360000L;
        long maxH = 8L * 360000L;

        inline.getExtent().setCx(maxW);
        inline.getExtent().setCy(maxH);

        List<Object> drawings = pkg.getMainDocumentPart().getJAXBNodesViaXPath("//w:drawing", true);

        if (drawings.isEmpty()) return;

        Object obj = drawings.get(0);

        if (obj instanceof jakarta.xml.bind.JAXBElement<?> el) {
            obj = el.getValue();
        }

        if (obj instanceof Drawing drawing) {
            drawing.getAnchorOrInline().clear();
            drawing.getAnchorOrInline().add(inline);
        }
    }

    private static String buildSizeAndWeight(Exhibit e) {
        StringBuilder sb = new StringBuilder();

        String l = format(e.getLength());
        String w = format(e.getWidth());
        String h = format(e.getHeight());

        sb.append(l).append(" × ").append(w).append(" × ").append(h);

        if (e.getUnitSizes() != null && !e.getUnitSizes().isBlank()) {
            sb.append(" ").append(e.getUnitSizes());
        }
        if (e.getWeight() != null) {
            if (!sb.isEmpty()) sb.append("; ");
            sb.append(format(e.getWeight()));
            if (e.getUnitWeight() != null && !e.getUnitWeight().isBlank()) {
                sb.append(" ").append(e.getUnitWeight());
            }
        }
        return sb.toString();
    }

    private static String format(Double v) {
        if (v == null) return "—";
        String s = String.valueOf(v);
        return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
    }

    private static void replaceVariables(WordprocessingMLPackage template,
                                         Map<String, String> vars) throws Exception {
        var mainPart = template.getMainDocumentPart();
        mainPart.variableReplace(vars);
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}