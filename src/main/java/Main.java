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

    private static String translateAncestry(String ancestry) throws IOException {


        HashMap<String,String> ancestryDic;

        Reader reader = Files.newBufferedReader(Paths.get( Main.class.getResource("Ancestry.json").getPath().substring(1)));

        ancestryDic = new Gson().fromJson(reader,new TypeToken<HashMap<String,String>>(){}.getType());

        String[] a = ancestry.split(" \\(");

        a[0] = ancestryDic.get(a[0]);
        a[1] = ancestryDic.get(a[1].substring(0,a[1].length()-1));


        return a[0] + " \u0028 " + a[1] + " )";
    }

    private  static  String translateWeapon(String weapon) throws IOException {
        HashMap<String,String> weaponDic;

        Reader reader = Files.newBufferedReader(Paths.get( Main.class.getResource("Weapon.json").getPath().substring(1)));

        weaponDic = new Gson().fromJson(reader,new TypeToken<HashMap<String,String>>(){}.getType());

        return weaponDic.get(weapon);
    }
    private  static  String translateWeaponTraits(String weaponTraits) throws IOException {
        HashMap<String,String> weaponTraitsDic;

        Reader reader = Files.newBufferedReader(Paths.get( Main.class.getResource("WeaponTrait.json").getPath().substring(1)));

        weaponTraitsDic = new Gson().fromJson(reader,new TypeToken<HashMap<String,String>>(){}.getType());

        String[] traits = weaponTraits.split(", ");

        for(int i = 0; i< traits.length;i++) traits[i] = weaponTraitsDic.get(traits[i]);

        return String.join(", ", traits);
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

            switch (field.getFullyQualifiedName()) {
                case ("Ancestry_Heritage") -> {
                    field.setValue(translateAncestry(field.getValueAsString()));

                    break;
                }
                case ("Class") -> {

                    HashMap<String, String> classDic;

                    Reader reader = Files.newBufferedReader(Paths.get(Main.class.getResource("Class.json").getPath().substring(1)));

                    classDic = new Gson().fromJson(reader, new TypeToken<HashMap<String, String>>() {
                    }.getType());


                    field.setValue(classDic.get(field.getValueAsString()));
                    break;
                }
                case ("Background") -> {
                    HashMap<String, String> backDic;

                    Reader reader = Files.newBufferedReader(Paths.get(Main.class.getResource("Backgrounds.json").getPath().substring(1)));

                    backDic = new Gson().fromJson(reader, new TypeToken<HashMap<String, String>>() {
                    }.getType());


                    field.setValue(backDic.get(field.getValueAsString()));
                    break;
                }
                case ("Melee1_Name"), ("Melee2_Name"), ("Melee3_Name"), ("Ranged1_Name"), ("Ranged2_Name"), ("Ranged3_Name") -> {
                    field.setValue(translateWeapon(field.getValueAsString()));
                    break;
                }

                case ("Melee1_Traits"), ("Melee2_Traits"), ("Melee3_Traits"), ("Ranged1_Traits"), ("Ranged2_Traits"), ("Ranged3_Traits") ->{
                    field.setValue(translateWeaponTraits(field.getValueAsString()));
                }

            }

        }

        doc.save(newCharacterPath);

    }

    public static void main(String[] args) throws Exception {



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
