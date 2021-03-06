/* Generated By:JavaCC: Do not edit this line. ParseCpslConstants.java */
package gate.jape.parser;

public interface ParseCpslConstants {

  int EOF = 0;
  int space = 1;
  int spaces = 2;
  int newline = 3;
  int digits = 4;
  int letter = 5;
  int letterOrUnderscore = 6;
  int letters = 7;
  int lettersAndDigits = 8;
  int letterOrDigitOrDash = 9;
  int lettersAndDigitsAndDashes = 10;
  int multiphase = 11;
  int phases = 12;
  int path = 13;
  int phasesWhiteSpace = 14;
  int phasesSingleLineCStyleComment = 15;
  int phasesSingleLineCpslStyleComment = 16;
  int phasesCommentStart = 17;
  int phasesCommentChars = 18;
  int phasesCommentEnd = 19;
  int javaimport = 20;
  int phase = 21;
  int input = 22;
  int option = 23;
  int rule = 24;
  int macro = 25;
  int template = 26;
  int priority = 27;
  int pling = 28;
  int kleeneOp = 29;
  int attrOp = 30;
  int metaPropOp = 31;
  int integer = 32;
  int string = 44;
  int bool = 45;
  int ident = 46;
  int floatingPoint = 47;
  int exponent = 48;
  int colon = 49;
  int semicolon = 50;
  int period = 51;
  int bar = 52;
  int comma = 53;
  int leftBrace = 54;
  int rightBrace = 55;
  int leftBracket = 56;
  int rightBracket = 57;
  int leftSquare = 58;
  int rightSquare = 59;
  int assign = 60;
  int colonplus = 61;
  int whiteSpace = 62;
  int singleLineCStyleComment = 63;
  int singleLineCpslStyleComment = 64;
  int commentStart = 65;
  int commentChars = 66;
  int commentEnd = 67;
  int other = 68;

  int DEFAULT = 0;
  int IN_PHASES = 1;
  int PHASES_WITHIN_COMMENT = 2;
  int IN_STRING = 3;
  int WITHIN_COMMENT = 4;

  String[] tokenImage = {
    "<EOF>",
    "<space>",
    "<spaces>",
    "<newline>",
    "<digits>",
    "<letter>",
    "<letterOrUnderscore>",
    "<letters>",
    "<lettersAndDigits>",
    "<letterOrDigitOrDash>",
    "<lettersAndDigitsAndDashes>",
    "\"Multiphase:\"",
    "\"Phases:\"",
    "<path>",
    "<phasesWhiteSpace>",
    "<phasesSingleLineCStyleComment>",
    "<phasesSingleLineCpslStyleComment>",
    "<phasesCommentStart>",
    "<phasesCommentChars>",
    "<phasesCommentEnd>",
    "\"Imports:\"",
    "\"Phase:\"",
    "\"Input:\"",
    "\"Options:\"",
    "\"Rule:\"",
    "\"Macro:\"",
    "\"Template:\"",
    "\"Priority:\"",
    "\"!\"",
    "<kleeneOp>",
    "<attrOp>",
    "\"@\"",
    "<integer>",
    "\"\\\"\"",
    "\"\\\\n\"",
    "\"\\\\r\"",
    "\"\\\\t\"",
    "\"\\\\b\"",
    "\"\\\\f\"",
    "\"\\\\\\\"\"",
    "\"\\\\\\\'\"",
    "\"\\\\\\\\\"",
    "<token of kind 42>",
    "<token of kind 43>",
    "\"\\\"\"",
    "<bool>",
    "<ident>",
    "<floatingPoint>",
    "<exponent>",
    "\":\"",
    "\";\"",
    "\".\"",
    "\"|\"",
    "\",\"",
    "\"{\"",
    "\"}\"",
    "\"(\"",
    "\")\"",
    "\"[\"",
    "\"]\"",
    "\"=\"",
    "\":+\"",
    "<whiteSpace>",
    "<singleLineCStyleComment>",
    "<singleLineCpslStyleComment>",
    "<commentStart>",
    "<commentChars>",
    "<commentEnd>",
    "<other>",
    "\"-->\"",
  };

}
