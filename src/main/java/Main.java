import java.io.File;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws Exception {
        BooleanSearchEngine engine = new BooleanSearchEngine(new File("pdfs"));
//        System.out.println(engine.search("бизнес"));
        engine.search("бизнес").forEach(System.out::println);


        for (Map.Entry<String, List<PageEntry>> entry : engine.getWordPageEntries().entrySet()) {

            System.out.println("WORD: " + entry.getKey());
            entry.getValue().forEach(pageEntry -> {
                        System.out.println(pageEntry.getPdfName() + " " + pageEntry.getPage() +": " + pageEntry.getCount());


            }
            );
        }


        // здесь создайте сервер, который отвечал бы на нужные запросы
        // слушать он должен порт 8989
        // отвечать на запросы /{word} -> возвращённое значение метода search(word) в JSON-формате
    }
}