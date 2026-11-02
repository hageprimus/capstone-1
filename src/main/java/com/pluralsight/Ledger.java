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

    //Define useful date constants
    private static final LocalDate FIRST_OF_THIS_MONTH = LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), 1);
    private static final LocalDate FIRST_OF_LAST_MONTH = LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonthValue()-1, 1);
    private static final LocalDate FIRST_OF_THIS_YEAR = LocalDate.of(LocalDate.now().getYear(), Month.JANUARY, 1);
    private static final LocalDate FIRST_OF_LAST_YEAR = LocalDate.of(LocalDate.now().getYear()-1, Month.JANUARY, 1);

    //Define constant for how many user-search parameters there are
    private static final int SEARCH_PARAMETERS = 5;

    //MAIN
    public static void main(String[] args) throws IOException {
        //Populate ledger
        List<Transaction> ledger = populateLedger(true);

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
                            case 3 -> addTransactionToLedger(createTransactionFromConsole(false, s), ledger);
                            case 4 -> addTransactionToLedger(createTransactionFromConsole(true, s), ledger);
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
                            case 2 -> displayTransactions(0, ledger);
                            case 3 -> displayTransactions(1, ledger);
                            case 4 -> displayTransactions(-1, ledger);
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
                            case 2 -> displayTransactions(FIRST_OF_THIS_MONTH, LocalDate.now(), ledger);
                            case 3 -> displayTransactions(FIRST_OF_LAST_MONTH, FIRST_OF_THIS_MONTH.minusDays(1), ledger);
                            case 4 -> displayTransactions(FIRST_OF_THIS_YEAR, LocalDate.now(), ledger);
                            case 5 -> displayTransactions(FIRST_OF_LAST_YEAR, FIRST_OF_THIS_YEAR.minusDays(1), ledger);
                            case 6 -> displayTransactions(getInputFromConsole("Enter the name of the vendor to search for: ", s, false), ledger);
                            case 7 -> searchTransactions(userDefinedSearch(ledger, s), ledger);
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

    /**
     * Add the given transaction to the internal ledger list, as well as updating the given file.
     * TODO: add json functionality, user defined file?
     */
    private static void addTransactionToLedger(Transaction newT, List<Transaction> ledger) throws IOException {
        ledger.add(newT);
        BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/ledger.csv", true));
        System.out.println(newT.outputToCsv());
        writer.write(newT.outputToCsv());
        writer.close();
        //TODO: unfinished, add error catching
    }

    private static Transaction createTransactionFromConsole(boolean isDebit, Scanner s) {
        String dateInput = getInputFromConsole("Enter the date of the transaction (YYYY-MM-DD): ", s, false);
        LocalDate date = LocalDate.parse(dateInput);
        String timeInput = getInputFromConsole("Enter the time of the transaction (HH:MM): ", s, false);
        LocalTime time = LocalTime.parse(timeInput);
        String description = getInputFromConsole("Enter a short description of the transaction: ", s, false);
        String vendor = getInputFromConsole("Enter the name of the vendor the transaction was conducted with: ", s, false);
        String amountInput = getInputFromConsole("Enter the amount of money transferred in the transaction: ", s, false);
        double amount = Double.parseDouble(amountInput) * (isDebit ? -1 : 1);
        return new Transaction(date, time, description, vendor, amount);
        //TODO: unfinished, add error catching, account for user input variability
    }

    /**
     * Display a list of transactions based on pre-defined parameters in the program.
     * Each version of the method can display transactions based on debit vs deposit, the date,
     * or the vendor the transaction was conducted with.
     */
    private static void displayTransactions(int displayArg, List<Transaction> ledger) {
        for (Transaction t: ledger) {
            if (displayArg * t.getAmount() >= 0)
                System.out.println(t.reportInfo());
        }
    }
    private static void displayTransactions(LocalDate startDate, LocalDate endDate, List<Transaction> ledger) {
        List<Transaction> results = new ArrayList<>();
        ledger.sort(Comparator.comparing(Transaction::getDate));
        for (Transaction t : ledger) {
            if ((t.getDate().isAfter(startDate) || t.getDate().isEqual(startDate)) && (t.getDate().isBefore(endDate) || t.getDate().isEqual(endDate)))
                results.add(t);
        }
        for (Transaction t : results) {
            System.out.println(t.reportInfo());
        }
    }
    private static void displayTransactions(String vendorName, List<Transaction> ledger) {
        List<Transaction> results = new ArrayList<>();
        ledger.sort(Comparator.comparing(Transaction::getVendor));
        for (Transaction t : ledger) {
            if (t.getVendor().equals(vendorName))
                results.add(t);
        }
        for (Transaction t : results) {
            System.out.println(t.reportInfo());
        }
    }

    private static void searchTransactions(String[] userSearch, List<Transaction> ledger) {
        //Define lists of indexes
        List<Integer> dateMatch = new ArrayList<>();
        List<Integer> descMatch = new ArrayList<>();
        List<Integer> vendMatch = new ArrayList<>();
        List<Integer> amntMatch = new ArrayList<>();

        //Set search flags based on what the user inputted
        boolean[] searchFlags = new boolean[SEARCH_PARAMETERS];
        Arrays.fill(searchFlags, false);
        for (var i = 0; i < SEARCH_PARAMETERS; i++) {
            if (!userSearch[i].isBlank()) {
                searchFlags[i] = true;
            }
        }

        //Search each parameter individually and store the indices of matches
        for (var i = 0; i < SEARCH_PARAMETERS; i++) {
            switch (i) {
                case 0, 1 -> {
                    if (searchFlags[i]) {
                        ledger.sort(Comparator.comparing(Transaction::getDate));
                        LocalDate startDate = !userSearch[0].isBlank() ? LocalDate.parse(userSearch[0]) : ledger.get(0).getDate();
                        LocalDate endDate = !userSearch[1].isBlank() ? LocalDate.parse(userSearch[1]) : LocalDate.now();
                        for (var j = 0; j < ledger.size(); j++) {
                            Transaction t = ledger.get(j);
                            if ((t.getDate().isAfter(startDate) || t.getDate().isEqual(startDate)) && (t.getDate().isBefore(endDate) || t.getDate().isEqual(endDate)))
                                dateMatch.add(j);
                        }
                    }
                }
                case 2 -> {
                    if (searchFlags[i]) {
                        ledger.sort(Comparator.comparing(Transaction::getDescription));
                        for (var j = 0; j < ledger.size(); j++) {
                            Transaction t = ledger.get(j);
                            if (t.getDescription().equals(userSearch[i]))
                                descMatch.add(j);
                        }
                    }
                }
                case 3 -> {
                    if (searchFlags[i]) {
                        ledger.sort(Comparator.comparing(Transaction::getVendor));
                        for (var j = 0; j < ledger.size(); j++) {
                            Transaction t = ledger.get(j);
                            if (t.getVendor().equals(userSearch[i]))
                                vendMatch.add(j);
                        }
                    }
                }
                case 4 -> {
                    if (searchFlags[i]) {
                        ledger.sort(Comparator.comparing(Transaction::getAmount));
                        String[] splitInput = userSearch[i].trim().split("-");
                        if (splitInput.length > 1) {
                            double[] range = new double[splitInput.length];
                            for (var j = 0; j < splitInput.length; j++) {
                                range[j] = Double.parseDouble(splitInput[j]);
                            }
                            Arrays.sort(range);
                            for (var x = 0; x < ledger.size(); x++) {
                                Transaction t = ledger.get(x);
                                if (t.getAmount() >= range[0] && t.getAmount() <= range[1])
                                    amntMatch.add(x);
                            }
                        } else {
                            double exactAmount = Double.parseDouble(splitInput[0]);
                            for (var j = 0; j < ledger.size(); j++) {
                                Transaction t = ledger.get(j);
                                if (t.getAmount() == exactAmount)
                                    amntMatch.add(j);
                            }
                        }
                    }
                }
            }

            //Iterate through the original list again, and create a new list of transactions that match in every necessary field
        }
    }

    private static String[] userDefinedSearch(List<Transaction> ledger, Scanner s) {
        String[] output = new String[SEARCH_PARAMETERS];
        String startDatePrompt =
                """
                Enter the start date of the range to search for.
                Press ENTER to leave this field blank.
                If an end date is entered with this field blank,
                the oldest transaction will be used as the start date.
                """;
        String startDateInput = getInputFromConsole(startDatePrompt, s, false);
        output[0] = startDateInput;
        String endDatePrompt =
                """
                Enter the start date of the range to search for.
                Press ENTER to leave this field blank.
                If an start date is entered with this field blank,
                the current date will be used as the end date.
                """;
        String endDateInput = getInputFromConsole(endDatePrompt, s, false);
        output[1] = endDateInput;
        String descriptionPrompt =
                """
                Enter the description field of the transaction you are searching for.
                Press ENTER to leave this field blank.
                The actual description entered must EXACTLY match the text of the one in the database!
                The input does not need to be case-sensitive.
                """;
        String descriptionInput = getInputFromConsole(descriptionPrompt, s, false);
        output[2] = descriptionInput;
        String vendorPrompt =
                """
                Enter the vendor field of the transaction you are searching for.
                Press ENTER to leave this field blank.
                The actual vendor name entered must EXACTLY match the text of the one in the database!
                The input does not need to be case-sensitive.
                """;
        String vendorInput = getInputFromConsole(vendorPrompt, s, false);
        output[3] = vendorInput;
        String amountPrompt =
                """
                Enter the actual value of the transaction you are searching for.
                You can define a range to search for by inputting 2 values in the format [LOW - HIGH}.
                Press ENTER to leave this field blank.
                """;
        String amountInput = getInputFromConsole(amountPrompt, s, false);
        output[4] = amountInput;
        return output;
    }

    /**
     * Populate the ledger from the given .csv file.
     * TODO: add user defined file functionality, .json parsing?
     */
    private static List<Transaction> populateLedger(boolean skip1st) throws IOException {
        List<Transaction> ledger = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/ledger.csv"));
            if (skip1st) reader.readLine(); //skip header line if flagged
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

    /**
     * General input retrieval method
     */
    private static String getInputFromConsole(String display, Scanner s, boolean clearBuffer) {
        if (clearBuffer) s.nextLine();
        System.out.println(display);
        return s.nextLine().trim();
    }
}
