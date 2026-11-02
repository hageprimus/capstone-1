package com.pluralsight;
import java.io.*;
import java.util.*;
import java.time.*;
public class Ledger {
    private enum MENU {
        MAIN,
        LEDGER,
        REPORT
    }

    //Define menu text
    private static final String MAIN_MENU = """
            Welcome to the ledger application!
            Perform any action below by inputting the corresponding key.
            1 - Open the ledger menu
            2 - Open the report menu
            3 - Add a deposit to the ledger
            4 - Add a debit to the ledger
            0 - EXIT
            """;

    private static final String LEDGER_MENU = """
            View and sort ledger entries here.
            Perform any action below by inputting the corresponding key.
            1 - Generate a report
            2 - Display all entries
            3 - Display only deposits
            4 - Display only debits
            0 - RETURN TO MAIN MENU
            """;

    private static final String REPORT_MENU = """
            Generate reports based on defined conditions.
            Perform any action below by inputting the corresponding key.
            1 - View basic ledger information
            2 - Display all transactions this month, to date.
            3 - Display all of last month's transactions.
            4 - Display all transactions this year, to date.
            5 - Display all of last year's transactions.
            6 - Display all transactions from a given vendor.
            7 - Display all transactions based on user defined conditions.
            0 - RETURN TO MAIN MENU
            """;

    //MAIN
    public static void main(String[] args) throws IOException {
        //Populate ledger
        List<Transaction> ledger = populateLedger();

        //Initialize important menu variables
        MENU menuState = MENU.MAIN;
        String userInput = "";
        int command =  0;
        boolean exitFlag = false;

        //Program loop
        Scanner s = new Scanner(System.in);
        do {
            switch (menuState) {
                case MAIN -> {
                    userInput = getInputFromConsole(MAIN_MENU, s, false);
                    try {
                        command = Integer.parseInt(userInput);
                        switch (command) {
                            case 1 -> menuState = MENU.LEDGER;
                            case 2 -> menuState = MENU.REPORT;
                            case 3 -> addTransaction(false, ledger, s);
                            case 4 -> addTransaction(true, ledger, s);
                            default -> {
                                System.out.println("Closing application.");
                                exitFlag = true;
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Invalid menu input. Please enter a valid option.");
                        e.printStackTrace();
                    }
                }
                case LEDGER -> {
                    userInput = getInputFromConsole(LEDGER_MENU, s, false);
                    try {
                        command = Integer.parseInt(userInput);
                        switch (command) {
                            case 1 -> menuState = MENU.REPORT;
                            case 2 -> displayTransactions(false, ledger);
                            case 3 -> displayTransactions(true, ledger);
                            default -> menuState = MENU.MAIN;
                        }
                    } catch (Exception e) {
                        System.out.println("Invalid menu input. Please enter a valid option.");
                        e.printStackTrace();
                    }
                }
                case REPORT -> {
                    userInput = getInputFromConsole(REPORT_MENU, s, false);
                    try {
                        command = Integer.parseInt(userInput);
                        switch (command) {
                            case 1 -> menuState = MENU.LEDGER;
                            default -> menuState = MENU.MAIN;
                        }
                    } catch (Exception e) {
                        System.out.println("Invalid menu input. Please enter a valid option.");
                        e.printStackTrace();
                    }
                }
            }

        } while (!exitFlag);
        s.close();
    }

    private static void addTransaction(boolean isDebit, List<Transaction> ledger, Scanner s) {
        String dateInput = getInputFromConsole("Enter the date of the transaction (YYYY-MM-DD): ", s, false);
        LocalDate date = LocalDate.parse(dateInput);
        String timeInput = getInputFromConsole("Enter the time of the transaction (HH:MM): ", s, false);
        LocalTime time = LocalTime.parse(timeInput);
        String description = getInputFromConsole("Enter a short description of the transaction: ", s, false);
        String vendor = getInputFromConsole("Enter the name of the vendor the transaction was conducted with: ", s, false);
        String amountInput = getInputFromConsole("Enter the amount of money transferred in the transaction: ", s, false);
        double amount = Double.parseDouble(amountInput);
        Transaction newT = new Transaction(date, time, description, vendor, amount);
        ledger.add(newT);
        //TODO: unfinished, add error catching
    }

    private static void displayTransactions(boolean isDebit, List<Transaction> ledger) {
    }

    //Populate the ledger from the given .csv file
    private static List<Transaction> populateLedger() {
        List<Transaction> ledger = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/ledger.csv"));
            String fileLine = reader.readLine();
            do {
                String[] tokens = fileLine.split("\\|");
                Transaction t = new Transaction(tokens[0], tokens[1], tokens[2], tokens[3], tokens[4]);
                ledger.add(t);
            } while ((fileLine = reader.readLine()) != null);
            reader.close();
        } catch (IOException e) {
            System.out.println("File not found!");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Invalid input detected from file!");
            e.printStackTrace();
        }
        return ledger;
    }

    //General input retrieval method
    private static String getInputFromConsole(String display, Scanner s, boolean clearBuffer) {
        if (clearBuffer) s.nextLine();
        System.out.println(display);
        return s.nextLine().trim();
    }
}
