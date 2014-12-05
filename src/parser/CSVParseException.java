package parser;

/**
 * OOP2
 * Created by V.Batytskyy on 02.04.14
 */


/**
 * parser.CSVParseException describes parse exception
 */
class CSVParseException extends RuntimeException {
    /**
     * Constructor for the exception
     * @param err explicit message as the reason of exception
     * @param line line, where the exception occurred
     * @param col column, where the exception occurred
     */
    public CSVParseException(String err, int line, int col) {
        super(err + " at line " + line + ", column " + col);
    }

    /**
     * Constructor for the exception with unspecified col
     * @param err explicit message as the reason of exception
     * @param line line, where the exception occurred
     */
    public CSVParseException(String err, int line) {
        super(err + " in line " + line);
    }
}
