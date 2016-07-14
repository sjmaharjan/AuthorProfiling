package edu.uab.console.tokenize;

import edu.uab.jobs.tokenizer.Twokenize;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;


import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by suraj on 4/2/14.
 */
public class ArtTweetTokenizer extends Tokenizer {
    private int charsRead; // length of the input
    private String inStr;
    private boolean started;
    private int inLen;

    private PositionIncrementAttribute posIncr;
    private CharTermAttribute termAtt;
    List<String> tokens;
    Iterator<String> tokensItr;

    public ArtTweetTokenizer(Reader input) {
        super(input);
        posIncr = addAttribute(PositionIncrementAttribute.class);
        termAtt = addAttribute(CharTermAttribute.class);


    }

    @Override
    public boolean incrementToken() throws IOException {
        if (!started) {
            started = true;
            char[] chars = new char[1024];
            charsRead = -1;
            StringBuilder builder = new StringBuilder();
            do {
                charsRead = input.read(chars, 0, chars.length);
                //if we have valid chars, append them to end of string.
                if (charsRead > 0)
                    builder.append(chars, 0, charsRead);
            } while (charsRead > 0);

//            while (charsRead < chars.length) {
//                int inc = input.read(chars, charsRead, chars.length - charsRead);
//                System.out.println("--->Inc size" + inc);
//                if (inc == -1) {
//                    break;
//                }
//                charsRead += inc;
//                System.out.println("---->charRead size" + charsRead);
//            }
            inStr = builder.toString().trim();
            inLen = inStr.length();
            if (inLen == 0) {
                return false;
            }
            tokens = Twokenize.tokenize(inStr);
            tokensItr = tokens.iterator();
        }

        if (!tokensItr.hasNext())
            return false;
        else {
            String nextToken = tokensItr.next();
            termAtt.setEmpty().append(nextToken);
            return true;
        }


    }

    @Override
    public void reset() throws IOException {
        super.reset();
        started = false;

    }
}
