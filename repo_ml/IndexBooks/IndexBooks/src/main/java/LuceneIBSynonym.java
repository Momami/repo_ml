import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.deeplearning4j.models.word2vec.Word2Vec;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LuceneIBSynonym {
    public static Directory memoryIndex;
    public static SynonymAnalyzer standardAnalyzer;

    public LuceneIBSynonym(RAMDirectory ramDirectory, SynonymAnalyzer analyzer){
        memoryIndex = ramDirectory;
        standardAnalyzer = analyzer;
    }

    public void indexDocument(String title, String author, Long countPage, Long price, String publisher, Long year)
            throws IOException {
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(standardAnalyzer);
        IndexWriter writer = new IndexWriter(memoryIndex, indexWriterConfig);
        Document doc = new Document();

        doc.add(new SortedDocValuesField("title", new BytesRef(title)));
        doc.add(new TextField("title", title, Field.Store.YES));
        doc.add(new TextField("author", author, Field.Store.YES));
        doc.add(new LongPoint("countPage", countPage));
        doc.add(new StoredField("countPage", countPage));
        doc.add(new LongPoint("price", price));
        doc.add(new StoredField("price", price));
        doc.add(new StringField("publisher", publisher, Field.Store.YES));
        doc.add(new LongPoint("year", year));
        doc.add(new StoredField("year", year));


        writer.addDocument(doc);
        writer.close();
    }

    public List<Document> searchIndex(Query query) throws IOException {
        IndexReader indexReader = DirectoryReader.open(memoryIndex);
        IndexSearcher searcher = new IndexSearcher(indexReader);
        TopDocs topDocs = searcher.search(query, 400);
        List<Document> docs = new ArrayList<Document>();
        for (ScoreDoc sDoc : topDocs.scoreDocs){
            docs.add(searcher.doc(sDoc.doc));
        }
        return docs;
    }
}
