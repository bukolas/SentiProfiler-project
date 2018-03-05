/**
 * 
 */
package sentiprofiler.ui.vertexinfo;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import sentiprofiler.profile.WordContext;
import sentiprofiler.ui.tagcloud.FontSizeComputationStrategy;
import sentiprofiler.ui.tagcloud.TagCloud;
import sentiprofiler.ui.tagcloud.TagCloudElement;
import sentiprofiler.ui.tagcloud.VisualizeTagCloudDecorator;
import sentiprofiler.ui.tagcloud.impl.JittersMeterHTMLTagCloudDecorator;
import sentiprofiler.ui.tagcloud.impl.LinearFontSizeComputationStrategy;
import sentiprofiler.ui.tagcloud.impl.LogFontSizeComputationStrategy;
import sentiprofiler.ui.tagcloud.impl.TagCloudElementImpl;
import sentiprofiler.ui.tagcloud.impl.TagCloudImpl;


/**
 * Creates word clouds from words appearing in a sentiment class.
 * @author Tuomo
 */
public class WordCloudCreator {
    private TagCloud cloudLinear;
	private static String fontPrefix = "font-size: ";
	private static VisualizeTagCloudDecorator decorator = new JittersMeterHTMLTagCloudDecorator();

	public void makeContextTagCloud(Vector<WordContext> contexts)  {
        Hashtable<String, Integer> stringCounts = new Hashtable<String, Integer>();
		int numSizes = 5;
     
		for(WordContext c : contexts) {
			String word = c.getWord();
				if(!stringCounts.containsKey(word))
					stringCounts.put(word, 1);
				else stringCounts.put(word, stringCounts.get(word) + 1);
			}
		
        List<TagCloudElement> l = new ArrayList<TagCloudElement>();
		Enumeration<String> keys = stringCounts.keys();
		while(keys.hasMoreElements()) {
			String cloudWord = keys.nextElement();
			int couldCount = stringCounts.get(cloudWord);
	        l.add(new TagCloudElementImpl(cloudWord, couldCount));			
		}
        
        FontSizeComputationStrategy strategy = 
            new LinearFontSizeComputationStrategy(numSizes,fontPrefix);
        cloudLinear = new TagCloudImpl(l,strategy);
        strategy = new LogFontSizeComputationStrategy(numSizes,fontPrefix);
        TagCloud cloudLog = new TagCloudImpl(l,strategy);
        //List<TagCloudElement> elements = cloudLinear.getTagCloudElements();
        //String expectedFont = fontPrefix + (numSizes-1);
    }

	public String getHtmlConetnt() {
		return decorator.decorateTagCloud(cloudLinear);
	}


	public int getCloudSize() {
        return cloudLinear.getTagCloudElements().size();	
	}
	
    public TagCloud getCloud() {
		return cloudLinear;
	}	
}
