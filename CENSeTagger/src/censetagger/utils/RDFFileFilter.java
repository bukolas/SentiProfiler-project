package censetagger.utils;

import java.io.File;
import javax.swing.filechooser.*;

/**
 * Filter that allows only directories and RDF ontology files. 
 * @author Tuomo Kakkonen
 *
 */
public class RDFFileFilter extends FileFilter {

    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String extension = Utils.getExtension(f);
        if (extension != null) {
            if (extension.equals("rdf"))
                    return true;
             else 
                return false;
        }
        return false;
    }

    public String getDescription() {
        return "RDF files";
    }
}
