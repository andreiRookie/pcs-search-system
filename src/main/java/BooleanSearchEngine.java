import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BooleanSearchEngine implements SearchEngine {

    private Map<String, List<PageEntry>> wordPageEntries = new HashMap<>();

    public BooleanSearchEngine(File pdfsDir) throws IOException {

        List<File> pdfFiles = new ArrayList<>();
        if (pdfsDir.exists() && pdfsDir.isDirectory()) {
            pdfFiles = Stream.of(pdfsDir.listFiles())
                    .filter(file -> !file.isDirectory())
                    .collect(Collectors.toList());
        }

        for (File file : pdfFiles) {
            var doc = new PdfDocument(new PdfReader(file));

            for (var i = 1; i <= doc.getNumberOfPages(); i++) {
                var page = doc.getPage(i);

                var text = PdfTextExtractor.getTextFromPage(page);
                var words = text.split("\\P{IsAlphabetic}+");

                Map<String, Integer> currPageWordFreqs = new HashMap<>();

                for (var word : words) {
                    if (word.isEmpty()) {
                        continue;
                    }
                    word = word.toLowerCase();
                    currPageWordFreqs.put(word, currPageWordFreqs.getOrDefault(word, 0) + 1);
                }

                for (Map.Entry<String, Integer> entry : currPageWordFreqs.entrySet()) {

                    var pageEntry = new PageEntry(file.getName(), i, entry.getValue());

                    var list = new ArrayList<>(wordPageEntries.getOrDefault(entry.getKey(), Collections.emptyList()));
                    list.add(pageEntry);
                    list.sort(Comparator.reverseOrder());

                    wordPageEntries.put(entry.getKey(), list);
                }
            }
        }
    }

    @Override
    public List<PageEntry> search(String word) {
        return wordPageEntries.getOrDefault(word, Collections.emptyList());

    }

    public Map<String, List<PageEntry>> getWordPageEntries() {
        return wordPageEntries;
    }
}
