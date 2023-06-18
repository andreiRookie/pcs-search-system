import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class BooleanMultiSearchEngine implements MultiSearchEngine {

    private final Map<String, List<PageEntry>> wordPageEntries = new HashMap<>();

    private final List<String> stopWords;

    public BooleanMultiSearchEngine(File pdfsDir, File stopWordsFile) throws IOException {

        stopWords = getStopWordsFromFile(stopWordsFile);

        List<File> pdfFiles = getPdfFiles(pdfsDir);

        for (File file : pdfFiles) {
            var doc = new PdfDocument(new PdfReader(file));

            for (var i = 1; i <= doc.getNumberOfPages(); i++) {
                var page = doc.getPage(i);

                Map<String, Integer> currPageWordsFreq = getPageWordsFreqWithoutStopWords(page);

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
    public List<PageEntry> search(String text) {
        var words = text.split("\\P{IsAlphabetic}+");

        List<List<PageEntry>> summaryList = new ArrayList<>();
        for (var word : words) {
            var list = wordPageEntries.getOrDefault(word, Collections.emptyList());
            summaryList.add(list);
        }

        var summaryPageEntriesList = summaryList.stream()
                .flatMap(list -> list.stream())
                .collect(Collectors.toList());

        Set<String> pdfNames = new HashSet<>();
        Set<Integer> pageNumbers = new HashSet<>();

        for (PageEntry entry : summaryPageEntriesList) {
            pdfNames.add(entry.getPdfName());
            pageNumbers.add(entry.getPage());
        }

        List<PageEntry> resultList = new ArrayList<>();

        var count = 0;
        for (String name : pdfNames) {
            for (int page : pageNumbers) {
                for (PageEntry entry : summaryPageEntriesList) {
                    if (entry.getPdfName().equals(name) && entry.getPage() == page) {
                        count += entry.getCount();
                    }
                }
                if (count != 0) {
                    resultList.add(new PageEntry(name, page, count));
                    count = 0;
                }
            }
        }
        resultList.sort(Comparator.reverseOrder());

        return resultList;
    }

    private List<String> getStopWordsFromFile(File file) {
        List<String> list = new ArrayList<>();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {

            String nextLine;
            while ((nextLine = bufferedReader.readLine()) != null) {
                list.add(nextLine.toLowerCase().trim());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    private List<File> getPdfFiles(File pdfsDir) {
        List<File> pdfFiles = new ArrayList<>();

        if (pdfsDir.exists() && pdfsDir.isDirectory()) {
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(pdfsDir.getAbsolutePath()))) {
                for (Path path : dirStream) {
                    if (!Files.isDirectory((path))) {
                        pdfFiles.add(path.toFile());
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return pdfFiles;
    }

    private Map<String, Integer> getPageWordsFreqWithoutStopWords(PdfPage page) {
        var text = PdfTextExtractor.getTextFromPage(page);
        var words = text.split("\\P{IsAlphabetic}+");

        Map<String, Integer> wordsFreq = new HashMap<>();

        for (var word : words) {
            if (word.isEmpty()) {
                continue;
            }
            word = word.toLowerCase();

            var isNotStopWord = true;
            for (var stopWord : stopWords) {
                if (word.equals(stopWord)) {
                    isNotStopWord = false;
                    break;
                }
            }

            if (isNotStopWord) {
                wordsFreq.put(word, wordsFreq.getOrDefault(word, 0) + 1);
            }
        }
        return wordsFreq;
    }

    public List<String> getStopWords() {
        return stopWords;
    }

    public Map<String, List<PageEntry>> getWordPageEntries() {
        return wordPageEntries;
    }
}
