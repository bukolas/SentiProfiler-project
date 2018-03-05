/*
 *  Searcher.java
 *
 *  Niraj Aswani, 19/March/07
 *
 *  $Id: Searcher.java,v 1.1 2011/01/13 16:52:15 textmine Exp $
 */
package gate.creole.annic;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Searcher interface.
 * @author niraj
 *
 */
public interface Searcher {

  /**
   * Search method that allows searching
   * 
   * @param query
   * @param numberOfPatterns
   * @param patternWindow
   * @return
   * @throws SearchException
   */
  public boolean search(String query, Map<String, Object> parameters) throws SearchException;

  /**
   * Query to search
   * 
   * @return
   */
  public String getQuery();

  /**
   * Return the next numberOfHits -1 indicates all
   * 
   * @return
   */
  public Hit[] next(int numberOfHits) throws SearchException;

  
  /**
   * Returns the Map containing all possible values of AnnotationTypes
   * and Feature Values for each of this annotationType.  This call must only be invoked
   * after a call to the getIndexedAnnotationSetNames(String indexLocation) method.
   * Otherwise this method doesn't guranttee the correct results.
   * The results obtained has the following format.
   * Key: CorpusName;AnnotationSetName;AnnotationType
   * Value: respective features
   * @return
   */
  public Map<String, List<String>> getAnnotationTypesMap();

  
  /**
   * Returns an containing names of the indexed annotation sets
   *    * Each entry has the following format:
   * <p>corpusName;annotationSetName</p>
   * where, the corpusName is the name of the corpus the annotationSetName belongs to.
   * @param indexLocation
   * @return
   * @throws SearchException
   */
  public String[] getIndexedAnnotationSetNames() throws SearchException;
  
  /**
   * Returns the recently set parameters
   * 
   * @return
   */
  public Map<String, Object> getParameters();

  /**
   * This method can be used for exporting results
   * 
   * @param outputFile
   */
  public void exportResults(File outputFile);

  /**
   * return the last seen hits once again
   * 
   * @return
   */
  public Hit[] getHits();

  /**
   * @see StatsCalculator#freq(String, String, String, String, String)
   */
  public int freq(String corpusToSearchIn,
          String annotationSetToSearchIn, String annotationType,
          String featureName, String value) throws SearchException;

  /**
   * @see StatsCalculator#freq(String, String, String, String, String)
   */
  public int freq(String corpusToSearchIn,
          String annotationSetToSearchIn, String annotationType)
          throws SearchException;

  /**
   * @see StatsCalculator#freq(String, String, String, String, String)
   */
  public int freq(String corpusToSearchIn,
          String annotationSetToSearchIn, String annotationType,
          String featureName) throws SearchException;

  /**
   * @see StatsCalculator#freq(List<Hit>, String, String, String, boolean, boolean)
   */
  public int freq(List<Hit> patternsToSearchIn,
          String annotationType, String feature, String value,
          boolean inMatchedSpan, boolean inContext) throws SearchException;

  /**
   * @see StatsCalculator#freq(List<Hit>, String, String, String, boolean, boolean)
   */
  public int freq(List<Hit> patternsToSearchIn,
          String annotationType, boolean inMatchedSpan, boolean inContext) throws SearchException;

  /**
   * @see StatsCalculator#freq(List<Hit>, String, String, boolean, boolean)
   */
  public Map<String, Integer> freqForAllValues(
          List<Hit> patternsToSearchIn, String annotationType,
          String feature, boolean inMatchedSpan, boolean inContext)
          throws SearchException;

}
