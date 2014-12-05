package parser;

/**
 * OOP2
 * Created by V.Batytskyy on 02.04.14
 */

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

/**
 * The parser.CSVProcessor class provides methods for saving and
 * loading a .csv file and serializing/deserializing
 * its list representation
 */
public class CSVProcessor {
    // lines of a .csv file
    private ArrayList<String> lines;

    // fields of a parsed csv file
    private ArrayList<ArrayList<StringBuilder>> csvFields;

    // current line for parsed fields addition
    private ArrayList<StringBuilder> curLine;

    // current field for parsed symbols
    private StringBuilder curField;

    // spaces inside a field(before a comma), that might be added
    private StringBuilder trailSpaces;

    // current symbol for parsing
    private char curSymb;

    private int col;                    // vertical position of the current symbol
    private int row;                    // horizontal position of the current symbol

    /**
     * Create an instance of ArrayList
     */
    public CSVProcessor() {
        col = 0;
        row = 1;
        lines = new ArrayList<String>();
        csvFields = new ArrayList<ArrayList<StringBuilder>>();

        curLine = new ArrayList<StringBuilder>();
        curField = new StringBuilder();
        trailSpaces = new StringBuilder();
    }

    /**
     * Loads the .csv file
     * @param file the file
     * @throws IOException
     */
    public void load(File file) throws IOException {
        try (BufferedReader br =
                     new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) lines.add(line);
        }
    }

    /**
     * Saves the .csv file
     * @param file the file
     * @throws IOException
     */
    public void save(File file) throws IOException {
        try (BufferedWriter bw =
                     new BufferedWriter(new FileWriter(file))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();           // \n
            }
        }
    }

    /**
     * Serializes ArrayList of .csv lines
     * @param file destination
     * @throws IOException
     */
    public void serialize(File file) throws IOException {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(lines);
        }
    }

    /**
     * Deserializes ArrayList of .csv lines
     * @param file source
     * @throws IOException
     */
    public void deserialize(File file) throws IOException {
        try (ObjectInputStream ois =
                     new ObjectInputStream(new FileInputStream(file))) {
            try {
                //noinspection unchecked
                lines = (ArrayList<String>) ois.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Outputs all .csv lines
     */
    public void printData() {
        lines.forEach(System.out::println);
    }

    /**
     * Returns current csvFields
     * @return two-dimensional array of strings
     */
    public ArrayList<ArrayList<String>> getCsvFields() {
        ArrayList<ArrayList<String>> fields = new ArrayList<>();
        for (int i = 0; i < csvFields.size(); i++) {
            fields.add(new ArrayList<>());
            for (int j = 0; j < csvFields.get(i).size(); j++) {
                fields.get(i).add(csvFields.get(i).get(j).toString());
            }
        }

        return fields;
    }

    /**
     * parse lines of the a file
     */
    public void parse() {
        Automate automate = new Automate();
        curSymb = 0;                            // any symbol is appropriate

        for (String line : lines) {
            int i = 0;
            while (i < line.length()) {
                col++;
                if (curSymb == '\n') {
                    automate.nextState(curSymb);                    curSymb = 0;
                } else {
                    curSymb = line.charAt(i++);
                    automate.nextState(curSymb);
                }
            }

            curSymb = '\n';                             // explicitly add new line char
            row++;
            col = -1;
        }

        addNewLine();
    }

    /**
     * Appends curSymb to a curField
     */
    private void appendSymb() {
        curField.append(curSymb);
    }

    /**
     * Appends trailing spaces and tabs to a curField
     */
    private void appendTrailingSpacesToField() {
        curField.append(trailSpaces);
    }

    /**
     * Adds trailing spaces to the trailSpaces string
     */
    private void addTrailingSpacesToString() {
        trailSpaces.append(curSymb);
    }

    /**
     * Clears trailSpaces string
     */
    private void clearTrailingSpaces() {
        if (trailSpaces.length() > 0) {
            trailSpaces.delete(0, trailSpaces.length() - 1);
        }
    }

    /**
     * Adds a curField to a curLine
     */
    private void addNewField() {
        curLine.add(curField);
        curField = new StringBuilder();
    }

    /**
     * Adds new Line to the csvFields
     */
    private void addNewLine() {
        if (curField != null) {
            addNewField();
        }

        if (csvFields.size() > 0) {
            if (curLine.size() > csvFields.get(csvFields.size() - 1).size()) {
                throw new CSVParseException("Too much fields", row - 1);
            } else if (curLine.size() < csvFields.get(csvFields.size() - 1).size()) {
                throw new CSVParseException("Not enough fields", row - 1);
            }
        }

        csvFields.add(curLine);
        curLine = new ArrayList<StringBuilder>();
    }

    /**
     * class Automate describes Automaton for csv file parsing
     */
    private class Automate {
        // map from (State; Symbol) to (State; parser.Action)
        private Map<Pair<State, SymbolClass>, Pair<State, Action>> automate;

        // current state inside the Automaton
        private State curState;

        // list of states
        private ArrayList<State> states;

        //list of actions
        private ArrayList<Action> actions;

        /**
         * creates new automate with states actions and nodes between states
         */
        public Automate() {
            automate = new HashMap<>();
            states = new ArrayList<>();
            actions = new ArrayList<>();

            for (int i = 0; i < 8; i++) {
                states.add(i, new State(i));        // initialize states 0 through 7
            }

            // set initial state
            curState = states.get(0);


            actions.add( () -> {                                   // 0
                appendSymb();
                clearTrailingSpaces();
            });
            actions.add( () -> {                                   // 1
                appendTrailingSpacesToField();
                clearTrailingSpaces();
                appendSymb();
            });
            actions.add( () -> addTrailingSpacesToString() );      // 2
            actions.add( () -> addNewField() );                    // 3
            actions.add( () -> addNewLine() );                     // 4

            actions.add(() -> {                                    // 5 exception
                throw new CSVParseException("Unexpected symbol ' "+ curSymb+ " ' ", row, col);
            });

            actions.add( () -> {                                    // 6, empty action
            });


            put(states.get(0), 'A',  states.get(1), actions.get(0));
            put(states.get(0), ',',  states.get(0), actions.get(3));
            put(states.get(0), '\n', states.get(0), actions.get(4));
            put(states.get(0), '"',  states.get(2), actions.get(6));
            put(states.get(0), ' ',  states.get(6), actions.get(6));

            put(states.get(1), 'A',  states.get(1), actions.get(0));
            put(states.get(1), ',',  states.get(0), actions.get(3));
            put(states.get(1), '\n', states.get(0), actions.get(4));
            put(states.get(1), '"',  states.get(7), actions.get(5));     // exception

            put(states.get(2), 'A',  states.get(2), actions.get(0));
            put(states.get(2), ',',  states.get(2), actions.get(0));
            put(states.get(2), ' ',  states.get(2), actions.get(0));
            put(states.get(2), '\n', states.get(2), actions.get(0));
            put(states.get(2), '"',  states.get(3), actions.get(6));

            put(states.get(3), '"',  states.get(2), actions.get(0));
            put(states.get(3), '\n', states.get(0), actions.get(4));
            put(states.get(3), ',',  states.get(0), actions.get(3));
            put(states.get(3), ' ',  states.get(4), actions.get(6));
            put(states.get(3), 'A',  states.get(7), actions.get(5));     // exception

            put(states.get(4), ' ',  states.get(4), actions.get(6));
            put(states.get(4), 'A',  states.get(7), actions.get(5));     // exception
            put(states.get(4), '"',  states.get(7), actions.get(5));     // exception
            put(states.get(4), '"',  states.get(7), actions.get(5));     // exception
            put(states.get(4), ',',  states.get(0), actions.get(3));
            put(states.get(4), '\n', states.get(0), actions.get(4));

            put(states.get(1), ' ',  states.get(5), actions.get(2));
            put(states.get(5), ' ',  states.get(5), actions.get(2));
            put(states.get(5), 'A',  states.get(0), actions.get(1));
            put(states.get(5), '\n', states.get(0), actions.get(4));
            put(states.get(5), ',',  states.get(0), actions.get(3));
            put(states.get(5), '"',  states.get(7), actions.get(5));    // exception

            put(states.get(6), ' ',  states.get(6), actions.get(6));
            put(states.get(6), 'A',  states.get(1), actions.get(0));
            put(states.get(6), '"',  states.get(2), actions.get(6));
            put(states.get(6), ',',  states.get(0), actions.get(3));
            put(states.get(6), '\n', states.get(0), actions.get(4));
        }

        /**
         * Defines next state from the curState and symbol
         * @param symbol symbol that is used to define state
         */
        public void nextState(char symbol) {
            Pair<State, SymbolClass> key = new Pair<>(curState, (new SymbolClass(symbol)));
            Pair<State, Action> value = automate.get(key);
            curState = value.getFirst();
            value.getSecond().act();
        }

        /**
         * Put new key value pair into the automaton
         * @param startState start state
         * @param symb symbol that defines the next state
         * @param finalState final state
         * @param action action when finalState is reached
         */
        private void put(State startState, char symb,
                         State finalState, Action action) {

            automate.put(new Pair<>(startState, new SymbolClass(symb)),
                    new Pair<State, Action>(finalState, action));
        }

        /**
         * Class pair for pair abstraction
         * @param <F> type of the first element in pair
         * @param <S> type of the second element in pair
         */
        private class Pair<F, S> {
            private F first;    //first member of the pair
            private S second;   //second member of the pair

            public Pair(F first, S second) {
                this.first = first;
                this.second = second;
            }

            public F getFirst() {
                return first;
            }

            public S getSecond() {
                return second;
            }

            @Override
            public int hashCode() {
                return (first.hashCode() + second.hashCode()) / 2;
            }

            @SuppressWarnings("unchecked")
            @Override
            public boolean equals(Object obj) {
                if (! (obj instanceof Pair)) return false;
                if (obj == this) return true;
                return this.first.equals(((Pair<F, S>) obj).first)
                        && this.second.equals(((Pair<F, S>) obj).second);
            }
        }

        /**
         * Class that defines an automaton state
         */
        private class State {
            private int number;
            public State(int number) {
                this.number = number;
            }

            @Override
            public int hashCode() {
                return ((Integer)number).hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                if (! (obj instanceof State)) return false;
                if (obj == this) return true;
                return number == ((State)obj).number;
            }
        }

        /**
         * Class for class of the symbols definition
         */
        private class SymbolClass {
            private int symbClass;

            public SymbolClass(char symbol) {
                if (symbol == ',') symbClass = 'B';
                else if (symbol == '"') symbClass = 'C';
                else if (symbol == ' ' || symbol == '\t') symbClass = 'D';
                else if (symbol == '\n') symbClass = 'E';
                else symbClass = 'A';
            }

            @Override
            public int hashCode() {
                return ((Integer)symbClass).hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                if (! (obj instanceof SymbolClass)) return false;
                if (obj == this) return true;
                return symbClass == ((SymbolClass)obj).symbClass;
            }
        }
    }
}
