/*
 *  LearnerSettings.java
 * 
 *  Yaoyong Li 22/03/2007
 *
 *  $Id: LearnerSettings.java,v 1.1 2011/01/14 08:36:34 textmine Exp $
 */
package gate.learning;

/**
 * The name and options of a learner, specified in the configuration file.
 */
public class LearnerSettings {
  /** The implementation name of the learner. */
  String learnerName;
  /** Nick name of the learner. */
  String learnerNickName;
  /** The parameters of the learners. */
  String paramsOfLearning = null;
  /** The executable command of training. */
  String executableTraining;
  /** The executable command of testing. */
  String executableTesting;

  public String getLearnerName() {
    return this.learnerName;
  }

  public String getLearnerNickName() {
    return this.learnerNickName;
  }
}
