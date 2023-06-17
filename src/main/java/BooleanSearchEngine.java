import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class BooleanSearchEngine implements SearchEngine {

    private Map<String, List<PageEntry>> wordPageEntries = new HashMap<>();

    public BooleanSearchEngine(File pdfsDir) throws IOException {

        List<File> pdfFiles = getPdfFiles(pdfsDir);

        for (File file : pdfFiles) {
            var doc = new PdfDocument(new PdfReader(file));

            for (var i = 1; i <= doc.getNumberOfPages(); i++) {
                var page = doc.getPage(i);

                Map<String, Integer> currPageWordsFreq = getPageWordsFreq(page);

                for (Map.Entry<String, Integer> entry : currPageWordsFreq.entrySet()) {

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

    private List<File> getPdfFiles(File pdfsDir) throws IOException {
        List<File> pdfFiles = new ArrayList<>();

        if (pdfsDir.exists() && pdfsDir.isDirectory()) {
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(pdfsDir.getAbsolutePath()))) {
                for (Path path : dirStream) {
                    if (!Files.isDirectory((path))) {
                        pdfFiles.add(path.toFile());
                    }
                }
            }
        }
        return pdfFiles;
    }

    private Map<String, Integer> getPageWordsFreq(PdfPage page) {
        var text = PdfTextExtractor.getTextFromPage(page);
        var words = text.split("\\P{IsAlphabetic}+");

        Map<String, Integer> wordsFreq = new HashMap<>();

        for (var word : words) {
            if (word.isEmpty()) {
                continue;
            }
            word = word.toLowerCase();
            wordsFreq.put(word, wordsFreq.getOrDefault(word, 0) + 1);
        }
        return wordsFreq;
    }

    public Map<String, List<PageEntry>> getWordPageEntries() {
        return wordPageEntries;
    }
}
