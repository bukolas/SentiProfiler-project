package censetagger.ui.tagcloud.impl;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import censetagger.ui.tagcloud.TagCloud;
import censetagger.ui.tagcloud.TagCloudElement;
import censetagger.ui.tagcloud.VisualizeTagCloudDecorator;




/**
 * Creates a HTML presentation for words tags for JittersMeter.
 * @author Tuomo Kakkonen
 *
 */
public class JittersMeterHTMLTagCloudDecorator implements VisualizeTagCloudDecorator {
    private static final String HEADER_HTML = "<html>";
    private static final int NUM_TAGS_IN_LINE = 3;
    private Map<String, String> fontMap = null;
    
    public JittersMeterHTMLTagCloudDecorator() {
        getFontMap();
    }
    
    private void getFontMap() {
        this.fontMap = new HashMap<String,String>();
        fontMap.put("font-size: 0", "font-size: 10px");
        fontMap.put("font-size: 1", "font-size: 15px");
        fontMap.put("font-size: 2", "font-size: 20px");
        fontMap.put("font-size: 3", "font-size: 25px");
        fontMap.put("font-size: 4", "font-size: 30px");
    }
    
    public String decorateTagCloud(TagCloud tagCloud) {
        StringWriter sw = new StringWriter();
        List<TagCloudElement> elements = tagCloud.getTagCloudElements();
        sw.append(HEADER_HTML);
        int count = 0;
        for (TagCloudElement tce :  elements) {
            //sw.append("&nbsp;<a style=\""+ fontMap.get(tce.getFontSize())+";\">" );
            sw.append("&nbsp;<a href=\"" + tce.getTagText() +
            		"\" style=\""+ fontMap.get(tce.getFontSize())+";\">" );
        	sw.append(tce.getTagText() +"</a>&nbsp;");
            if(count++ == NUM_TAGS_IN_LINE) {
                count = 0;
                sw.append("<br>" );
            }
        }
        sw.append("</body><br></html>");
        return sw.toString();           
    }
}
