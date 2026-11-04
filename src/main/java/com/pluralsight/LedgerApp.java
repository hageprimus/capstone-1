package com.pluralsight;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;

public class LedgerApp {
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
            5 - Reload data from the ledger file
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

    //Define constant for changing the file format the project operates with (for testing/demo purposes)
    private static final String LEDGER_FILE_CSV = "src/main/resources/ledger.csv";
    private static final String LEDGER_FILE_JSON = "src/main/resources/ledger.json";

    //MAIN
    public static void main(String[] args) throws IOException {
        //Populate ledger
        List<Transaction> ledger = populateLedger(LEDGER_FILE_JSON);

        //Initialize important menu variables
        MENU menuState = MENU.MAIN;
        String userInput;
        int command;
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
                            case 3 -> addTransactionToLedger(createTransactionFromConsole(false, s), ledger, LEDGER_FILE_CSV);
                            case 4 -> addTransactionToLedger(createTransactionFromConsole(true, s), ledger, LEDGER_FILE_CSV);
                            case 5 -> {
                                System.out.println("Reloaded ledger.");
                                ledger = populateLedger(LEDGER_FILE_JSON);
                            }
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
     * TODO: add JSON functionality, user defined file?
     */
    private static void addTransactionToLedger(Transaction newT, List<Transaction> ledger, String file) throws IOException {
        String[] output = setFileType(file);
        String type = output[0];
        file = output[1];

        ledger.add(newT);
        try {
            switch (type) {
                case ".csv" -> {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                    for (Transaction t : ledger)
                        writer.write(t.outputToCsv());
                    writer.close();
                }
                case ".json" -> {

                }
            }
        } catch (IOException e) {
            System.out.println("Ledger file not found!");
            e.printStackTrace();
        }
    }

    /**
     * Creates a new transaction based on user input.
     * Will prompt the user to confirm their input and re-enter the information if they deem it is incorrect for any reason.
     */
    private static Transaction createTransactionFromConsole(boolean isDebit, Scanner s) {
        //Starting variables
        boolean complete = false;
        Transaction newT = new Transaction();
        String confirm = """
                Has the transaction above been entered correctly?
                Press Y to confirm. Press N to re-enter the information.
                """;

        //Get transaction
        do {
            String dateInput = getInputFromConsole("Enter the date of the transaction (YYYY-MM-DD): ", s, false);
            LocalDate date = LocalDate.EPOCH;
            try {
                date = LocalDate.parse(dateInput);
            } catch (Exception e) {
                System.out.println("Invalid date input received! Defaulting date value.");
                e.printStackTrace();
            }
            String timeInput = getInputFromConsole("Enter the time of the transaction (HH:MM): ", s, false);
            LocalTime time = LocalTime.MIDNIGHT;
            try {
                //Assign the current second value to any time the user inputs to keep the data neat
                time = LocalTime.parse(timeInput).withSecond(LocalTime.now().getSecond());
            } catch (Exception e) {
                System.out.println("Invalid time input received! Defaulting time value.");
                e.printStackTrace();
            }
            String description = getInputFromConsole("Enter a short description of the transaction: ", s, false);
            String vendor = getInputFromConsole("Enter the name of the vendor the transaction was conducted with: ", s, false);
            String amountInput = getInputFromConsole("Enter the amount of money transferred in the transaction: ", s, false);
            double amount = 0;
            try {
                amount = Double.parseDouble(amountInput) * (isDebit ? -1 : 1);
            } catch (Exception e) {
                System.out.println("Invalid value input received! Defaulting value to $0.");
                e.printStackTrace();
            }

            //Assign values to new transaction object
            newT.setDate(date);
            newT.setTime(time);
            newT.setDescription(description);
            newT.setVendor(vendor);
            newT.setAmount(amount);

            //Confirm with user if we can proceed
            System.out.println(newT.outputToConsole());
            String userConfirm = getInputFromConsole(confirm, s, false);
            complete = userConfirm.equalsIgnoreCase("y");
        } while (!complete);
        return newT;
    }

    /**
     * Display a list of transactions based on pre-defined parameters in the program.
     * Each version of the method can display transactions based on debit vs deposit, the date,
     * or the vendor the transaction was conducted with.
     */
    private static void displayTransactions(int displayArg, List<Transaction> ledger) {
        List<Transaction> results = new ArrayList<>();
        ledger.sort(Comparator.comparing(Transaction::getAmount));
        for (Transaction t: ledger) {
            if (displayArg * t.getAmount() >= 0)
                results.add(t);
        }
        ledger.sort(Comparator.comparing(Transaction::getDate));
        printListOfTransactions(results);
    }
    private static void displayTransactions(LocalDate startDate, LocalDate endDate, List<Transaction> ledger) {
        List<Transaction> results = new ArrayList<>();
        for (Transaction t : ledger) {
            if ((t.getDate().isAfter(startDate) || t.getDate().isEqual(startDate)) && (t.getDate().isBefore(endDate) || t.getDate().isEqual(endDate)))
                results.add(t);
        }
        printListOfTransactions(results);
    }
    private static void displayTransactions(String vendorName, List<Transaction> ledger) {
        List<Transaction> results = new ArrayList<>();
        ledger.sort(Comparator.comparing(Transaction::getVendor));
        for (Transaction t : ledger) {
            if (t.getVendor().equals(vendorName))
                results.add(t);
        }
        ledger.sort(Comparator.comparing(Transaction::getDate));
        printListOfTransactions(results);
    }

    /**
     * Accepts an array of strings containing user defined parameters to search the given transaction list.
     * Outputs the results of the search to the console automatically.
     */
    private static void searchTransactions(String[] userSearch, List<Transaction> ledger) {
        //Create results list
        List<Transaction> results = new ArrayList<>();

        //Parse user input
        LocalDate startDate = !userSearch[0].isBlank() ? LocalDate.parse(userSearch[0]) : LocalDate.EPOCH;
        LocalDate endDate = !userSearch[1].isBlank() ? LocalDate.parse(userSearch[1]) : LocalDate.EPOCH;
        String description = userSearch[2];
        String vendor = userSearch[3];
        String[] amountSplit = !userSearch[4].isBlank() ? userSearch[4].trim().split("\\|") : new String[]{"0"};
        double[] range = new double[amountSplit.length];
        for (var i = 0; i < amountSplit.length; i++) {
            range[i] = Double.parseDouble(amountSplit[i]);
        }
        Arrays.sort(range);

        //Find matches for the necessary flags
        for (Transaction t : ledger) {
            boolean[] match = new boolean[4];
            Arrays.fill(match, false);
            ledger.sort(Comparator.comparing(Transaction::getDate));
            if ((startDate == LocalDate.EPOCH && endDate == LocalDate.EPOCH) || ((t.getDate().isAfter(startDate) || t.getDate().isEqual(startDate)) && (t.getDate().isBefore(endDate) || t.getDate().isEqual(endDate))))
                match[0] = true;
            ledger.sort(Comparator.comparing(Transaction::getDescription));
            if (description.isBlank() || t.getDescription().equalsIgnoreCase(description))
                match[1] = true;
            ledger.sort(Comparator.comparing(Transaction::getVendor));
            if (vendor.isBlank() || t.getVendor().equalsIgnoreCase(vendor))
                match[2] = true;
            ledger.sort(Comparator.comparing(Transaction::getAmount));
            if ((range[0] == 0 && range[range.length-1] == 0) || (t.getAmount() >= range[0] && t.getAmount() <= range[range.length-1]))
                match[3] = true;
            ledger.sort(Comparator.comparing(Transaction::getDate));

            //Add any transaction objects that fully match to the results output
            int matchCount = 0;
            for (boolean m : match)
                if (m) matchCount++;
            if (matchCount >= match.length)
                results.add(t);
        }
        printListOfTransactions(results);
    }

    /**
     * Prompts the user for 5 search parameters to conduct a custom search of the database.
     */
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
                Enter the end date of the range to search for.
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
                You can define a range to search for by inputting 2 values in the format [LOW|HIGH].
                Press ENTER to leave this field blank.
                """;
        String amountInput = getInputFromConsole(amountPrompt, s, false);
        output[4] = amountInput;
        return output;
    }
    /**
     * Prints a given list of transactions in a neat, color coded format.
     */
    private static void printListOfTransactions(List<Transaction> print) {
        System.out.printf("%10s|%8s|%50s|%35s|%10s\n", "DATE", "TIME", "DESCRIPTION", "VENDOR", "AMOUNT");
        for (Transaction t : print)
            System.out.println(t.outputToConsole());
    }

    /**
     * Populate the ledger from the given CSV or JSON file.
     */
    private static List<Transaction> populateLedger(String file) throws IOException {
        List<Transaction> ledger = new ArrayList<>();
        String[] output = setFileType(file);
        String type = output[0];
        file = output[1];
        //Parse provided file
        try {
            switch (type) {
                case ".csv" -> {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    reader.readLine(); //skip header line
                    String fileLine = reader.readLine();
                    int lineCount = 1;
                    do {
                        String[] tokens = fileLine.split("\\|");
                        if (tokens.length == 5) {
                            Transaction t = new Transaction(tokens[0], tokens[1], tokens[2], tokens[3], tokens[4]);
                            ledger.add(t);
                        } else {
                            System.out.println("Line " + lineCount + "does not parse correctly! Skipping this entry.");
                        }
                        lineCount++;
                    } while ((fileLine = reader.readLine()) != null);
                    reader.close();
                }
                case ".json" -> {
                    JsonReader jsonReader = new JsonReader(new FileReader(file));
                    jsonReader.beginArray();
                    while (jsonReader.hasNext()) {
                        jsonReader.beginObject();
                        Transaction newT = new Transaction();
                        while (jsonReader.hasNext()) {
                            String propertyName = jsonReader.nextName();
                            switch (propertyName) {
                                case "localDate" -> newT.setDate(LocalDate.parse(jsonReader.nextString()));
                                case "localTime" -> newT.setTime(LocalTime.parse(jsonReader.nextString()));
                                case "description" -> newT.setDescription(jsonReader.nextString());
                                case "vendor" -> newT.setVendor(jsonReader.nextString());
                                case "amount" -> newT.setAmount(jsonReader.nextDouble());
                                default -> jsonReader.skipValue();
                            }
                        }
                        ledger.add(newT);
                        jsonReader.endObject();
                    }
                    jsonReader.endArray();
                    jsonReader.close();
                }
            }
        } catch (IOException e) {
            System.out.println("File not found!");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Invalid input detected from file!");
            e.printStackTrace();
        }
        return ledger;
    }

    private static String[] setFileType(String file) {
        //Figure out which file type we're using
        String type = "";
        if (file.substring(file.length() - 4).equalsIgnoreCase(".csv")) {
            type = ".csv";
        } else if (file.substring(file.length() - 5).equalsIgnoreCase(".json")) {
            type = ".json";
        } else {
            System.out.println("Invalid file type passed to ledger! Loading provided .csv file by default.");
            type = ".csv";
            file = LEDGER_FILE_CSV;
        }
        return new String[]{type, file};
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
