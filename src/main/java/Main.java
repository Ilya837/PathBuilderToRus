import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.*;
import java.io.File;
import java.util.*;

public class Main {

    public static void listFields(PDDocument doc, PDDocument doc2,String newFileName) throws Exception {
        PDDocumentCatalog catalog = doc.getDocumentCatalog();
        PDAcroForm form = catalog.getAcroForm();
        List<PDField> fields = form.getFields();

        PDDocumentCatalog catalog2 = doc2.getDocumentCatalog();
        PDAcroForm form2 = catalog2.getAcroForm();


        for(PDField field: fields) {
            String value = field.getValueAsString();
            String name = field.getFullyQualifiedName();

            PDField field2 = form2.getField(name);
            field2.setValue(value);




            System.out.print(name);
            System.out.print(" = ");
            System.out.print(value);
            System.out.println();
        }

        doc2.save(newFileName);

    }

    public static void main(String[] args) throws Exception {

        //Файл, созданный PathBuilder
        File file = new File(Main.class.getResource("document.pdf").getPath());

        //Файл-шаблон
        File file2 = new File(Main.class.getResource("CharacterListRus.pdf").getPath());
        PDDocument doc = PDDocument.load(file);
        PDDocument doc2 = PDDocument.load(file2);

        // 3-ий параметр - файл, в который будет сохранён итог или который будет создан
        listFields(doc, doc2,Main.class.getResource("/").getPath() + "NewCharacter.pdf");


        System.out.println("Save to " +Main.class.getResource("/").getPath() + "NewCharacter.pdf");

    }

}
