package edu.uab.jobs.tokenizer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.charfilter.HTMLStripCharFilter;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.util.Version;

import java.io.Reader;
import java.util.Arrays;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by suraj on 3/28/14.
 */
public class AuthorDocumentAnalyzer extends Analyzer {

    private final Version matchVersion;
    private boolean filterStopWords = false;
    private boolean filterArticles = false;
    private boolean filterPronouns = false;
    private boolean filterConjunctions = false;
    private boolean filterPunctuations = false;
    private boolean filterEmoticons = false;
    private boolean filterPrepositions = false;

    //LIWC word lists
    private static final Set<String> ARTICLES = new TreeSet<String>(Arrays.asList("a", "an", "the", "alot"));
    private static final Set<String> CONJUNCTIONS = new TreeSet<String>(Arrays.asList("also", "although", "and", "as", "although", "because", "but", "cuz", "how", "however", "if", "nor", "or", "otherwise", "plus", "so", "then", "tho", "though", "til", "till", "unless", "until", "when", "whenever", "whereas", "whether", "while", "b\'coz", "either", "neither", "even"));
    private static final Set<String> PREPOSITIONS = new TreeSet<String>(Arrays.asList("about", "above", "across", "after", "against", "ahead", "along", "among", "around", "as", "at", "atop", "away", "before", "behind", "below", "beneath", "beside", "besides", "between", "beyond", "by", "despite", "down", "during", "except", "for", "from", "in", "inside", "insides", "into", "near", "of", "off", "on", "onto", "out", "outside", "over", "plus", "since", "than", "through", "thru", "til ", "till", "to", "toward*", "under", "underneath", "unless", "until", "unto", "up", "upon", "wanna", "with", "within", "without"));
    private static final Set<String> PRONOUNS = new TreeSet<String>(Arrays.asList("anybody","anyone","anything","everybody","everyone","everything","hed","he'd","her","hers","herself","hes","he's","him","himself","his","i","Id","I'd","I'll","Im","I'm","it","itd","it'd","itll","it'll","its","it's","itself","ive","I've","lets","let's","me","mine","my","myself","nobody","oneself","other","others","our","ours","ourselves","she","she'd","she'll","shes","she's","somebody","someone","something","somewhere","stuff","that","thatd","that'd","thatll","that'll","thats","that's","thee","their","them","themselves","these","they","theyd","they'd","theyll","they'll","theyve","they've","thine","thing","this","those","thou","thoust","thy","us","we","we'd","we'll","we're","weve","we've","what","whatever","whats","what's","which","whichever","who","whod","who'd","wholl","who'll","whom","whose","ya","yall","y'all","ye","you","youd","you'd","youll","you'll","your","youre","you're","yours","youve","you've","hed","he'd","her","hers","herself","hes","he's","him","himself","his","i","Id","I'd","I'll","Im","I'm","ive","I've","lets","let's","me","mine","my","myself","oneself","our","ours","ourselves","she","she'd","she'll","shes","she's","thee","their","them","themselves","they","theyd","they'd","theyll","they'll","theyve","they've","thine","thou","thoust","thy","us","we","we'd","we'll","we're","weve","we've","ya","yall","y'all","ye","you","youd","you'd","youll","you'll","your","youre","you're","yours","youve","you've","i","Id","I'd","I'll","Im","I'm","ive","I've","me","mine","my","myself","lets","let's","our","ours","ourselves","us","we","we'd","we'll","we're","weve","we've","thee","thine","thou","thoust","thy","ya","yall","y'all","ye","you","youd","you'd","youll","you'll","your","youre","you're","yours","youve","you've","he","hed","he'd","her","hers","herself","hes","he's","him","himself","his","oneself","she","she'd","she'll","shes","she's","their","them","themselves","they","theyd","they'd","theyll","they'll","theyve","they've","anybody","anyone","anything","everybody","everyone","everything","it","itd","it'd","itll","it'll","its","it's","itself","nobody","other","others","somebody","someone","something","somewhere","stuff","that","thatd","that'd","thatll","that'll","thats","that's","these","thing","this","those","what","whatever","whats","what's","which","whichever","who","whod","who'd","wholl","who'll","whom","whose"));
    private static final Set<String> FUNCTIONWORDS = new TreeSet<String>(Arrays.asList("about", "above", "across", "after", "against", "ahead", "along", "among", "around", "as", "at", "atop", "away", "before", "behind", "below", "beneath", "beside", "besides", "between", "beyond", "by", "despite", "down", "during", "except", "for", "from", "in", "inside", "insides", "into", "near", "of", "off", "on", "onto", "out", "outside", "over", "plus", "since", "than", "through", "thru", "til ", "till", "to", "toward*", "under", "underneath", "unless", "until", "unto", "up", "upon", "wanna", "with", "within", "without"));


    public AuthorDocumentAnalyzer(Version matchVersion) {
        this.matchVersion = matchVersion;
    }

    public void setFilterStopWords(boolean filterStopWords) {
        this.filterStopWords = filterStopWords;
    }

    public void setFilterArticles(boolean filterArticles) {
        this.filterArticles = filterArticles;
    }

    public void setFilterPronouns(boolean filterPronouns) {
        this.filterPronouns = filterPronouns;
    }

    public void setFilterConjunctions(boolean filterConjunctions) {
        this.filterConjunctions = filterConjunctions;
    }

    public void setFilterPrepositions(boolean filterPrepositions) {
        this.filterPrepositions = filterPrepositions;
    }

    public void setFilterEmoticons(boolean filterEmoticons) {
        this.filterEmoticons = filterEmoticons;
    }

    public void setFilterPunctuations(boolean filterPunctuations) {
        this.filterPunctuations = filterPunctuations;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        //remove html tags
        HTMLStripCharFilter stripHTML = new HTMLStripCharFilter(reader);
        Tokenizer source = new WhitespaceTokenizer(matchVersion, stripHTML);
        TokenStream result = new LowerCaseFilter(matchVersion, source);

            return null;
    }
}
