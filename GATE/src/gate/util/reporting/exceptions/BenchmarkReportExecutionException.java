/*
 *  BenchmarkReportExecutionException.java
 *
 *  Copyright (c)  2008-2009, Intelius, Inc.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Chirag Viradiya & Andrew Borthwick, 30/Sep/2009
 *
 *  $Id: BenchmarkReportExecutionException.java,v 1.1 2011/01/13 16:52:16 textmine Exp $
 */

package gate.util.reporting.exceptions;

/**
 * A custom exception thrown for the case where benchmark file is modified while
 * the tool is executing.
 * 
 */
public class BenchmarkReportExecutionException extends RuntimeException {
  public BenchmarkReportExecutionException(String message) {
    super(message);
  }
}