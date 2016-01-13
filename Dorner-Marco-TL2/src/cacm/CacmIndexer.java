package cacm;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

//Indexing
public class CacmIndexer {

	// FIELD NAMES:
	// internal ID (.I)
	public static final String ID = "docid";
	// title of the entry (.T)
	public static final String TITLE = "title";
	// abstract/content (.W)
	public static final String CONTENT = "content";

	// analyzer
	public Analyzer analyzer = null;

	// index writer
	public IndexWriter writer;

	// determines which analyzer should be used
	public static final boolean USE_STANDARD_ANALYZER = false;

	// constructor
	public CacmIndexer(String indexDir, Analyzer analyzer) throws IOException {
		Directory dir = FSDirectory.open(new File(indexDir).toPath());

		this.analyzer = analyzer;

		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

		writer = new IndexWriter(dir, iwc); // 3 modified
	}

	// main method for testing
	public static void main(String[] args) throws Exception {

		String indexDir = null;// 1
		String dataDir = "data/cacm.all"; // 2

		Analyzer analyzer = null;

		if (USE_STANDARD_ANALYZER) {
			indexDir = "idx_cacm_std";
			analyzer = new StandardAnalyzer();
		} else {// use MyStemAnalyzer
			indexDir = "idx_cacm_my";
			analyzer = new MyStemAnalyzer();
		}

		long start = System.currentTimeMillis();
		CacmIndexer indexer = new CacmIndexer(indexDir, analyzer);
		int numIndexed;
		try {
			numIndexed = indexer.index(dataDir);
		} finally {
			indexer.close();
		}
		long end = System.currentTimeMillis();

		System.out.println("Indexing " + numIndexed + " files took " + (end - start) + " milliseconds");
	}

	// as before, nothing new :-)
	public int index(String dataDir) throws Exception {

		File f = new File(dataDir);
		indexFile(f);

		return writer.numDocs(); // 5
	}

	// as before, nothing new :-)
	public void close() throws IOException {
		writer.close(); // 4
	}

	// Do the indexing! (see exercise 4.1)
	public void indexFile(File file) throws Exception {

		System.out.println("Indexing " + file.getCanonicalPath());

		String path = file.getAbsolutePath();
		String id = "-1";
		String abst = "";
		String title = "";

		Boolean isTitle = false;
		Boolean isContent = false;
		List<String> content = new ArrayList<>();
		List<Document> docList = new ArrayList<>();

		content = Files.readAllLines(Paths.get(path));

		for (String line : content) {

			if (!line.startsWith(".")) {
				if (isTitle == true) {
					title += line;
				} else if (isContent == true) {
					abst += " " + line;
					// System.out.println(line);
				}
			}

			if (line.startsWith(".I")) {
				createDoc(id, title, abst);
				title = "";
				abst = "";
				isTitle = false;
				isContent = false;
				id = line.substring(2).trim();
				// System.out.println(id);

			} else if (line.startsWith(".T")) {
				title = line.substring(2).trim();
				isTitle = true;
				isContent = false;
				// System.out.println(title);
			} else if (line.startsWith(".W")) {
				abst = line.substring(2).trim();

				isContent = true;
				isTitle = false;
				// System.out.println(abst);

			} else if (line.startsWith(".")) {
				isTitle = false;
				isContent = false;
			}

		}
		createDoc(id, title, abst);
	}

	private void createDoc(String id, String title, String abst) {
		Document doc = new Document();
		if (!id.equals("-1")) {

			doc.add(new StringField(ID, id, Field.Store.YES));
			doc.add(new TextField(CONTENT, new StringReader(abst)));
			doc.add(new TextField(TITLE, new StringReader(title)));

			try {
				writer.addDocument(doc);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
