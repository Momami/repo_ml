import org.apache.commons.lang.ArrayUtils;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.ru.RussianLightStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.util.CharsRef;
import org.apache.lucene.util.CharsRefBuilder;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;


public class SynonymAnalyzer extends Analyzer {
    private SynonymMap myMap;
    public static final CharArraySet RUS_STOP_WORDS_SET;

    public SynonymAnalyzer(){
        createSynMap();
    }


    public void createSynMap(){
        try {
            SynonymMap.Builder builder = new SynonymMap.Builder(true);
            FileReader file = new FileReader("C:\\Users\\Rabbit\\IdeaProjects\\indexBooks\\synonym1.txt");
            BufferedReader reader = new BufferedReader(file);
            // считаем сначала первую строку
            String line = reader.readLine();
            while (line != null) {
                String[] masStr = line.split("\\|");
                String[] tmp = masStr[1].split(" ");
                if (tmp.length > 0) {
                    for (String elem : tmp){
                        builder.add(new CharsRef(masStr[0]), new CharsRef(elem), true);
                        builder.add(new CharsRef(elem), new CharsRef(masStr[0]), true);
                    }
                }
                line = reader.readLine();
            }
            myMap = builder.build();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    protected TokenStreamComponents createComponents(String s) {
        Tokenizer source = new StandardTokenizer(); // токинайзер
        TokenStream filter = new StandardFilter(source);
        filter = new LowerCaseFilter(filter);
        filter = new StopFilter(filter, RUS_STOP_WORDS_SET);
        filter = new RussianLightStemFilter(filter);
        filter = new SynonymGraphFilter(filter, myMap, true);
        return new TokenStreamComponents(source, filter);
    }

    static {
        List<String> stopWords = Arrays.asList("и", "как", "но", "для", "если", "в", "на", "это", "эти", "нет", "не",
                "из", "от", "или", "такой", "чтобы", "этот", "этих", "когда", "тогда", "так", "вот", "ох", "до",
                "эх", "ах", "с", "со", "где", "бы", "кто", "зачем", "а", "да", "за");
        CharArraySet stopSet = new CharArraySet(stopWords, false);
        RUS_STOP_WORDS_SET = CharArraySet.unmodifiableSet(stopSet);
    }

}