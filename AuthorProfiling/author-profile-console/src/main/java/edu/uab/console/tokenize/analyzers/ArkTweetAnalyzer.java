package edu.uab.console.tokenize.analyzers;

import edu.uab.console.tokenize.ArtTweetTokenizer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.charfilter.HTMLStripCharFilter;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Created by suraj on 4/2/14.
 */
public class ArkTweetAnalyzer extends Analyzer {
    private final Version matchVersion;

    public ArkTweetAnalyzer(Version matchVersion) {
        this.matchVersion = matchVersion;
    }

    @Override

    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        final Tokenizer source = new ArtTweetTokenizer(reader);
        TokenStream result = new LowerCaseFilter(matchVersion, source);
        return new TokenStreamComponents(source, result);

    }

    @Override
    protected Reader initReader(String fieldName, Reader reader) {
        // wrap the Reader in a HTMLStripCharFilter chain.
        return new HTMLStripCharFilter(reader);
    }

    public static void main(String[] args) throws IOException {
        String text[] = new String[2];
        text[0] = " <div class=\"fontRenderer\"><div id=\"originalText_d28fbe21263e1b2b0c78de7214da8be0\">IT IS WHAT IT IS.... :-);-)</div>\n" +
                "                <script type=\"text/javascript\">\n" +
                "                        var filenames = [\"/s/j/class.fontrenderer.js\"];\n" +
                "\n" +
                "                        ComCore.BootLoader.loadJavascriptFiles(filenames, false, function() \n" +
                "                        {\n" +
                "                                new ComCore.FontRenderer(\"_0acd6e735cdf96ddf360ece3e8f5186559306826a70c9fd316fbf7be7b833edc\", \"IT+IS+WHAT+IT+IS\", \"d28fbe21263e1b2b0c78de7214da8be0\");\n" +
                "                        });\n" +
                "                </script></div>";
        text[1] = "<div class=\"fontRenderer\">considering rudimentary elements in cheap massage tables for this approach is frequently positioned on much better platforms free of charge or possibly a minimal demand that lets you adjust the particular seat top along with viewpoint together with quick spins from the lever as a rule of usb your desk must not be any kind of bigger compared to the therapist's fashionable you would like your own stand to be stable throughout affected individual positioning and when your client gets about and becoming over table more bonuses to ensure the cleanliness of one's desk many of us advise utilizing oil proof handles accessible in the particular components portion of the site and vinyl fabric along with leather cleanup liquid for sale in your acrylic area of the site they may be resilient lavish and your visitors will certainly think that they are inside panel involving luxury as they place in these types of dining tables or sofas they are well known for their health along with relaxation positive aspects hydrotherapy kneipp kur balneology and also ion technology are the historical therapies being used inquire extremely matter of factly when they are in pain.being pregnant it's possible to go to the physician and acquire suitable treatment method but also in situations involving nose over crowding as a result of cool or influenza it's possible to locate comfort with many home cures most of the trouble that a professional hostess suffers from in their work has to do with lugging around a sizable and heavy table of course my personal driver dad held no less in which seven or eight satisfies quite a few neckties costume tshirts and of course their flawlessly slick florsheim shoes or boots a new dark brown set as well as a dark colored couple each and every sofa incorporates a circular corner shape that aids to distribute the strain about the covering fabric along with solid wood develop a weathered search which appear homely.after that you can commence transferring further around the back a number of ways by demanding the fingers in the pressure details as well as moving your current knuckles across the spine the apparatus support the most significant place when it comes to launching your own personal day spa he she will likely employ hand along with hands along with palms and biceps and triceps chavutti means base along with leg as well as thirumal implies rub " +
                "\n"+"considering rudimentary elements in cheap massage tables for this approach is frequently positioned on much better platforms free of charge or possibly a minimal demand that lets you adjust the particular seat top along with viewpoint together with quick spins from the lever as a rule of usb your desk must not be any kind of bigger compared to the therapist's fashionable you would like your own stand to be stable throughout affected individual positioning and when your client gets about and becoming over table more bonuses to ensure the cleanliness of one's desk many of us advise utilizing oil proof handles accessible in the particular components portion of the site and vinyl fabric along with leather cleanup liquid for sale in your acrylic area of the site they may be resilient lavish and your visitors will certainly think that they are inside panel involving luxury as they place in these types of dining tables or sofas they are well known for their health along with relaxation positive aspects hydrotherapy kneipp kur balneology and also ion technology are the historical therapies being used inquire extremely matter of factly when they are in pain.being pregnant it's possible to go to the physician and acquire suitable treatment method but also in situations involving nose over crowding as a result of cool or influenza it's possible to locate comfort with many home cures most of the trouble that a professional hostess suffers from in their work has to do with lugging around a sizable and heavy table of course my personal driver dad held no less in which seven or eight satisfies quite a few neckties costume tshirts and of course their flawlessly slick florsheim shoes or boots a new dark brown set as well as a dark colored couple each and every sofa incorporates a circular corner shape that aids to distribute the strain about the covering fabric along with solid wood develop a weathered search which appear homely.after that you can commence transferring further around the back a number of ways by demanding the fingers in the pressure details as well as moving your current knuckles across the spine the apparatus support the most significant place when it comes to launching your own personal day spa he she will likely employ hand along with hands along with palms and biceps and triceps chavutti means base along with leg as well as thirumal implies rub </div>";



        Analyzer analyzer = new ArkTweetAnalyzer(Version.LUCENE_40);

        for (int i = 0; i < 2; i++) {
            System.out.println(i+" loop");
            TokenStream stream = analyzer.tokenStream("text", new StringReader(text[i]));
            CharTermAttribute termAtt = stream.getAttribute(CharTermAttribute.class);
            stream.reset();
            while (stream.incrementToken()) {
                if (termAtt.length() > 0) {
                    System.out.print(termAtt.toString() + " ");
                }
            }
            System.out.println();
            stream.end();
            stream.close();

        }
    }
}
