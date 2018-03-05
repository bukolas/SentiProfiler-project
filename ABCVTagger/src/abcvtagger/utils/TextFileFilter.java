package abcvtagger.utils;

import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.*;

/**
 * Filter that allows only directories and RDF ontology files. 
 * @author Tuomo Kakkonen
 *
 */
public class TextFileFilter extends FileFilter {

    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String extension = Utils.getExtension(f);
        if (extension != null) {
            if (extension.equals("txt"))
                    return true;
             else 
                return false;
        }
        return false;
    }

    public String getDescription() {
        return "Text files";
    }
}
