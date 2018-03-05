package gate.util.ant.packager;

import gate.util.BomStrippingInputStreamReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResourceIterator;
import org.apache.tools.ant.util.FileUtils;

/**
 * Class that extracts the list of gazetteer .lst files from a .def.
 * This class extends {@link Path} so it can be used as a nested element
 * within the extraresourcespath of a packagegapp task.
 */
public class GazetteerLists extends DataType implements ResourceCollection {

  /**
   * The gazetteer list definition file (.def).
   */
  private File definition;

  /**
   * The encoding used to read the def file. If null, the platform
   * default encoding will be used.
   */
  private String encoding = null;

  /**
   * The names of the gazetteer lists referenced by the definition.
   */
  private String[] listNames = null;

  /**
   * Set the location of the definition file from which the lists should
   * be extracted. The list definition file is parsed and the .lst files
   * found are added as pathelements to this path.
   *
   * @throws BuildException if an error occurs parsing the definition
   *           file.
   */
  public void setDefinition(File definition) {
    this.definition = definition;
  }

  /**
   * ResourceCollection interface: returns an iterator over the list
   * files.
   */
  public Iterator iterator() {
    load();

    if(listNames.length == 0) {
      return Collections.EMPTY_LIST.iterator();
    }
    else {
      return new FileResourceIterator(definition.getParentFile(), listNames);
    }
  }

  /**
   * ResourceCollection interface: returns true (this collection always
   * exposes only filesystem resources).
   */
  public boolean isFilesystemOnly() {
    return true;
  }

  /**
   * ResourceCollection interface: returns the number of list files
   * referenced by this definition.
   */
  public int size() {
    load();
    return listNames.length;
  }

  /**
   * Parse the definition and populate the array of list names.
   */
  private void load() {
    log("Listing gazetteer lists", Project.MSG_VERBOSE);
    if(definition == null) {
      throw new BuildException(
              "\"definition\" attribute is required for gazetteerlists");
    }
    log("definition file: " + definition, Project.MSG_VERBOSE);

    Set<String> lists = new HashSet<String>();
    File definitionDir = definition.getParentFile();
    try {
      FileInputStream fis = new FileInputStream(definition);
      try {
        BufferedReader in = null;
        if(encoding == null) {
          in = new BomStrippingInputStreamReader(fis);
        }
        else {
          in = new BomStrippingInputStreamReader(fis, encoding);
        }

        String line;
        while((line = in.readLine()) != null) {
          int indexOfColon = line.indexOf(':');
          // Ignore lines that don't include a colon.
          if(indexOfColon > 0) {
            String listFile = line.substring(0, indexOfColon);
            lists.add(listFile);
            log("Found list file " + listFile, Project.MSG_VERBOSE);
          }
        }
      }
      finally {
        fis.close();
      }
    }
    catch(IOException ioe) {
      throw new BuildException("Error reading gazetteer definition file "
              + definition, ioe);
    }

    listNames = lists.toArray(new String[lists.size()]);
  }

  /**
   * Set the encoding used to read the definition file. If this is not
   * set, the platform default encoding is used.
   */
  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

}