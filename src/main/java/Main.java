import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.interactive.form.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    private static HashMap<String,String> ancestryDic;

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

    private static String translateAncestry(String ancestry){



        String[] a = ancestry.split(" \\(");

        a[0] = ancestryDic.get(a[0]);
        a[1] = ancestryDic.get(a[1].substring(0,a[1].length()-1));


        return a[0] + " \u0028 " + a[1] + " )";
    }

    public static void translate(PDDocument doc,String newCharacterPath) throws IOException {

        PDFont font = PDType0Font.load(doc, new FileInputStream(Main.class.getResource("arial.ttf").getPath()), false);

        PDDocumentCatalog catalog = doc.getDocumentCatalog();
        PDAcroForm form = catalog.getAcroForm();
        List<PDField> fields = form.getFields();

        PDResources dr = form.getDefaultResources();

        COSName fontName = dr.add(font);

        for(PDField field: fields) {

            if (field instanceof PDTextField)
            {
                PDTextField textField = (PDTextField) field;
                String da = textField.getDefaultAppearance();

                // replace font name in default appearance string
                Pattern pattern = Pattern.compile("\\/(\\w+)\\s.*");
                Matcher matcher = pattern.matcher(da);
                if (!matcher.find() || matcher.groupCount() < 2)
                {
                    // oh-oh
                }
                String oldFontName = matcher.group(1);
                da = da.replaceFirst(oldFontName, fontName.getName());

                textField.setDefaultAppearance(da);
            }

            switch (field.getFullyQualifiedName()){

                case("Ancestry_Heritage"):{
                    field.setValue( translateAncestry( field.getValueAsString() ) );

                    break;
                }

            }

        }

        doc.save(newCharacterPath);

    }

    public static void main(String[] args) throws Exception {

        Reader reader = Files.newBufferedReader(Paths.get( Main.class.getResource("Ancestry.json").getPath().substring(1)));

        ancestryDic = new Gson().fromJson(reader,new TypeToken<HashMap<String,String>>(){}.getType());

        String newCharacterPath = Main.class.getResource("/").getPath() + "NewCharacter.pdf";


        //Файл, созданный PathBuilder
        File file = new File(Main.class.getResource("document.pdf").getPath());

        //Файл-шаблон
        File file2 = new File(Main.class.getResource("CharacterListRus.pdf").getPath());
        PDDocument doc = PDDocument.load(file);
        PDDocument doc2 = PDDocument.load(file2);

        // 3-ий параметр - файл, в который будет сохранён итог или который будет создан
        listFields(doc, doc2,newCharacterPath);

        File file3 = new File(newCharacterPath);
        PDDocument doc3 = PDDocument.load(file3);

        translate(doc3, newCharacterPath);


        System.out.println("Save to " +Main.class.getResource("/").getPath() + "NewCharacter.pdf");



    }

}
