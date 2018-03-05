package abcvtagger.ui.vertexinfo;

import java.awt.BorderLayout;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

/**
 * Panel for viewing HTML documents. Used for word clouds.
 */
public class CloudViewerPanel extends JPanel {
	private StyleSheet styleSheet;
	private HTMLEditorKit kit;
	
	public CloudViewerPanel(PopupInfoDialog parent, WordCloudCreator cloud) {
		JEditorPane jEditorPane = new JEditorPane();
		jEditorPane.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(jEditorPane);
		kit = new HTMLEditorKit();
		jEditorPane.setEditorKit(kit);
		jEditorPane.addHyperlinkListener(new HTMLListener(parent));
		createStylesheet();
		
		Document doc = kit.createDefaultDocument();
		jEditorPane.setDocument(doc);
		jEditorPane.setText(cloud.getHtmlConetnt());
		add(scrollPane, BorderLayout.CENTER);		
	}

	private void createStylesheet() {
		styleSheet = kit.getStyleSheet();
		styleSheet.addRule("body {color:#000; font-family:times; margin: 4px; }");
		styleSheet.addRule("a {text-decoration: none;}");
		styleSheet.addRule("h1 {color: blue;}");
		styleSheet.addRule("h2 {color: #ff0000;}");
		styleSheet.addRule("pre {font : 10px monaco; color : black; background-color : #fafafa; }");
	}
	
    private class HTMLListener implements HyperlinkListener {
    	public HTMLListener(PopupInfoDialog parent) {
    		this.parent = parent;
    	}
    	
    	private PopupInfoDialog parent;

    	public void hyperlinkUpdate(HyperlinkEvent e) {
          if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        	  String targetWord = e.getDescription();
        	  parent.filter(targetWord);
          }
        }
      }

}
