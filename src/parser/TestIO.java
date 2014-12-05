package parser;

/**
 * OOP2
 * Created by V.Batytskyy on 02.04.14
 */



import java.io.*;
import java.util.ArrayList;

/**
 * The parser.TestIO class is the test class for parser.CSVProcessor
 */
public class TestIO {
    public static void main(String[] args) throws IOException, InterruptedException {
        CSVProcessor csvProcessor = new CSVProcessor();
        File serialized = new File("serialized.dat");

        if (serialized.exists() && serialized.isFile()) {
            csvProcessor.deserialize(serialized);
        } else {
            try (BufferedReader br =
                         new BufferedReader(new InputStreamReader(System.in))) {

                // allowed number of misspelling file name
                final int attempts = 3;
                for (int errors = 1; errors <= attempts; errors++) {
                    try {
                        System.out.print("Enter file name: ");
                        // test.csv

                        String fileName = br.readLine();
                        File csvFile = new File(fileName);

                        csvProcessor.load(csvFile);
                        if (csvFile.exists()) break;
                    } catch (IOException e) {
                        if (errors == attempts) throw e;
                        System.out.print("Error: no such file exists. ");
                    }
                }
            }

            System.out.print("\nSerialization process...");
            csvProcessor.serialize(serialized);
            System.out.println("completed");
        }

        // Print initial data
        System.out.println("\nInitial data: ");     // Print initial data
        csvProcessor.printData();


        Thread t = new Thread(() -> {
            try {
                csvProcessor.parse();
                ArrayList<ArrayList<String>> csvFields =
                        csvProcessor.getCsvFields();
                System.out.println("\n Parsed data: \n" + csvFields);
            } catch (CSVParseException e) {
                e.printStackTrace();
            }
        });

        t.start();                  // Start execution of the thread
        t.join();                   // Wait till it finishes

    }
}
