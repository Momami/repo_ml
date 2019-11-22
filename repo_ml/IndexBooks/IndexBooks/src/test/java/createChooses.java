import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.RAMDirectory;
import org.bytedeco.javacpp.presets.opencv_core;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.print.Doc;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

public class createChooses {
    public static final String FILE_NAME = "C:\\Users\\Rabbit\\IdeaProjects\\indexBooks\\readertown.json";


    public static void termQuery(LuceneIBSynonym index, String author, String filename) throws IOException {
        Term term = new Term("author", author);
        Query q = new TermQuery(term);
        List<Document> docs = index.searchIndex(q);
        for (Document doc : docs) {
            System.out.println(doc.get("title") + " == " + doc.get("author"));
        }
        writeJsonFile(author, filename, docs);
    }

    public static void termQueryTitle(LuceneIBSynonym index, String title, String filename) throws IOException {
        Term term = new Term("title", title);
        Query q = new TermQuery(term);
        List<Document> docs = index.searchIndex(q);
        for (Document doc : docs) {
            System.out.println(doc.get("title") + " == " + doc.get("author"));
        }
        writeJsonFile(title, filename, docs);
    }

    public static void termQueryPublisher(LuceneIBSynonym index, String publ, String filename) throws IOException {
        Term term = new Term("publisher", publ);
        Query q = new TermQuery(term);
        List<Document> docs = index.searchIndex(q);
        for (Document doc : docs) {
            System.out.println(doc.get("title") + " == " + doc.get("author") + " == " + doc.get("publisher"));
        }
        writeJsonFile(publ, filename, docs);
    }

    public static void termQueryNew(LuceneIBSynonym index, String word, String filename) throws IOException {
        Query q = new TermQuery(new Term("publisher", word));
        Query q2 = new TermQuery(new Term("title", word));
        Query q3 = new TermQuery(new Term("author", word));
        BooleanQuery booleanQuery = new BooleanQuery.Builder()
                .add(q, BooleanClause.Occur.SHOULD)
                .add(q2, BooleanClause.Occur.SHOULD)
                .add(q3, BooleanClause.Occur.SHOULD)
                .build();
        List<Document> docs = index.searchIndex(booleanQuery);
        for (Document doc : docs) {
            System.out.println(doc.get("title") + " == " + doc.get("author") + " == " + doc.get("publisher"));
        }
        writeJsonFile(word, filename, docs);
    }

    /*public static void termQuerySyn(LuceneIBSynonym index, String title, String filename) throws IOException {
        Collection<String> syn = index.getModel().wordsNearest(title, 20);
        Term term = new Term("title", title);
        String[] tmp = Arrays.stream(syn.toArray()).toArray(String[]::new);
        Term[] terms = new Term[tmp.length + 1];
        int i = 1;
        terms[0] = term;
        for (String elem : tmp){
            terms[i] = new Term ("title", elem);
            i++;
        }
        SynonymQuery synon = new SynonymQuery(terms);
        List<Document> docs = index.searchIndex(synon);
        for (Document doc : docs) {
            System.out.println(doc.get("title") + " == " + doc.get("author"));
        }
        writeJsonFile(title, filename, docs);
    }*/

    public static void writeJsonFile(String term, String filename, List<Document> docs){
        JSONObject mainObj = new JSONObject();
        mainObj.put("query", term);
        JSONArray messages = new JSONArray();
        for (Document doc : docs){
            JSONObject object = new JSONObject();
            object.put("title", doc.get("title"));
            object.put("author",  doc.get("author"));
            object.put("publisher", doc.get("publisher"));
            messages.add(object);
        }
        mainObj.put("answer", messages);
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(mainObj.toJSONString());
            writer.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void termQueryNumber(LuceneIndexBook index, int start, int end) throws IOException {
        Query query = LongPoint.newRangeQuery("price", start, end);
        List<Document> docs = index.searchIndex(query);
        for (Document doc : docs) {
            System.out.println(doc.get("title") + " == " + doc.get("author") + " == " + doc.get("price"));
        }
    }

    public static void main(String[] args) {
        try {
            LuceneIBSynonym luceneIndexBook = new LuceneIBSynonym(new RAMDirectory(), new SynonymAnalyzer());
            ParserJsonWithInfo.parserDataForIndex(FILE_NAME, luceneIndexBook);
            while(true){
                System.out.println("Выберите действие:\n1 - Поиск по автору\n" +
                        "2 - Поиск по названию\n3 - Поиск по цене\n4 - Поиск по синонимам\n0 - Выход");
                Scanner inp = new Scanner (System.in);
                String input = inp.nextLine();
                if (input.equals("1")){
                    System.out.print("Введите автора: ");
                    String aut = inp.nextLine();
                    System.out.print("Введите имя файла: ");
                    String filename = inp.nextLine();
                    System.out.println("Поиск по автору \"" + aut + "\":\n");
                    createChooses.termQuery(luceneIndexBook, aut.toLowerCase(), filename);
                }
                else if (input.equals("2")){
                    System.out.print("Введите название: ");
                    String name = inp.nextLine();
                    System.out.print("Введите имя файла: ");
                    String filename = inp.nextLine();
                    System.out.println("Поиск по названию \"" + name + "\":\n");
                    createChooses.termQueryTitle(luceneIndexBook, name.toLowerCase(), filename);
                }
                else if (input.equals("3")){
                    System.out.print("Введите издательство: ");
                    String publisher = inp.nextLine();
                    System.out.print("Введите имя файла: ");
                    String filename = inp.nextLine();
                    System.out.println("\nПоиск по издательству \"" + publisher + "\":\n");
                    createChooses.termQueryPublisher(luceneIndexBook, publisher, filename);
                }
                else if (input.equals("4")) {
                    System.out.print("Введите название: ");
                    String name = inp.nextLine();
                    System.out.print("Введите имя файла: ");
                    String filename = inp.nextLine();
                    System.out.println("Поиск по синонимам \"" + name + "\":\n");
                    createChooses.termQueryTitle(luceneIndexBook, name.toLowerCase(), filename);
                }
                else if (input.equals("9")) {
                    System.out.print("Введите слово: ");
                    String name = inp.nextLine();
                    System.out.print("Введите имя файла: ");
                    String filename = inp.nextLine();
                    System.out.println("Поиск\"" + name + "\":\n");
                    createChooses.termQueryNew(luceneIndexBook, name.toLowerCase(), filename);
                }
                else if (input.equals("0")){
                    break;
                }
            }

        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("Error");
        }
    }
}
