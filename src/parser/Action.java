package parser;

/**
 * OOP2
 * Created by V.Batytskyy on 02.04.14
 */


/**
 * The parser.Action interface is the interface for automate action
 */
interface Action {
    /**
     * Defines an action for a pass in the automate
     * @throws CSVParseException error occurred during parsing
     */
    void act() throws CSVParseException;
}
