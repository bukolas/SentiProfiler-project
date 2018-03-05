/**
 *  DataForLearning.java
 * 
 *  Yaoyong Li 21/01/2008
 *
 *  $Id: ActiveLearningSetting.java,v 1.1 2011/01/14 08:36:34 textmine Exp $
 */
package gate.learning;
/** Store the settings for active learning, by reading the settings 
 * from configuration file.
 */
public class ActiveLearningSetting {
  /** Number of tokens used for selecting the document. */
  int numTokensSelect;
  /** Constructor with the number of documents. */
  public ActiveLearningSetting() {
    this.numTokensSelect = 3;
  }
}
