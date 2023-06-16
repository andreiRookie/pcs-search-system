public class PageEntry implements Comparable<PageEntry> {
    private final String pdfName;
    private final int page;
    private final int count;

    public PageEntry(String pdfName, int page, int count) {
        this.pdfName = pdfName;
        this.page = page;
        this.count = count;
    }

    @Override
    public int compareTo(PageEntry o) {
        if (this.count != o.count) {
            return Integer.compare(this.count, o.count);
        } else if (this.page != o.page) {
            return Integer.compare(this.page, o.page);
        } else {
            return this.pdfName.compareTo(o.pdfName);
        }
    }

    @Override
    public String toString() {
        return "PageEntry{" +
                "pdf='" + pdfName + '\'' +
                ", page=" + page +
                ", count=" + count +
                '}';
    }

    public String getPdfName() {
        return pdfName;
    }

    public int getPage() {
        return page;
    }

    public int getCount() {
        return count;
    }
}
