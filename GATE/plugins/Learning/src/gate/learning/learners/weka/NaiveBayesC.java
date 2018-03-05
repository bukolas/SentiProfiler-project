/*
 *  NaiveBayesC.java
 * 
 *  Yaoyong Li 22/03/2007
 *
 *  $Id: NaiveBayesC.java,v 1.1 2011/01/14 08:36:36 textmine Exp $
 */
package gate.learning.learners.weka;

import weka.classifiers.bayes.NaiveBayes;
/**
 * Naive Nayes classifier from Weka.
 */
public class NaiveBayesC extends WekaLearner {
  /** serialVersionUID for Serializable class*/
  private static final long serialVersionUID = 1L;
  /** Constructor.*/
  public NaiveBayesC() {
    wekaCl = new NaiveBayes();
    learnerName = "NaiveBayes";
  }
  /** Get the parameters of the Naive Bayes (do nothing). */
  public void getParametersFromOptionsLine(String options) {
    
  }
}
