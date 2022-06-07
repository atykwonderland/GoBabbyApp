package GoBabbyApp;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

class GoBabbyApp {
    private final static String enterID = "\nPlease enter your practitioner id [E] to exit:";
    private final static String enterApptDate = "\nPlease enter the date for appointment list [E] to exit:";
    private final static String enterApptNum = "Enter the appointment number that you would like to work on. \n[E] to exit [D] to go back to another date :";
    private final static String enterMenuChoice = "\n1. Review notes\n2. Review tests\n3. Add a note\n4. Prescribe a test\n5. Go back to the appointments.\n\nEnter your choice:";
    private final static String enterNote = "\nPlease type your observation:";
    private final static String enterTestType = "\nPlease enter the type of test:";

    public static void main(String[] args) throws SQLException {
        // DB connection stuff
        try { DriverManager.registerDriver(new com.ibm.db2.jcc.DB2Driver()); }
        catch (Exception cnfe){ System.out.println("Class not found"); }

        String url = "jdbc:db2://winter2022-comp421.cs.mcgill.ca:50000/cs421";
        String your_userid;
        String your_password;
        if((your_userid = System.getenv("SOCSUSER")) == null)
        {
            System.err.println("Error!! do not have a username to connect to the database!");
            System.exit(1);
        }
        if((your_password = System.getenv("SOCSPASSWD")) == null)
        {
            System.err.println("Error!! do not have a password to connect to the database!");
            System.exit(1);
        }
        Connection con = DriverManager.getConnection(url,your_userid,your_password);
        Statement statement = con.createStatement();

        // Where everything starts

        boolean login = false;
        boolean exit = false;
        Scanner input = new Scanner(System.in);

        int pracid = -1;
        String sqlString, inputString;
        ResultSet rs;

        // 1. midwife login
        while(!login) {
            System.out.println(enterID);
            inputString = input.nextLine();
            if (inputString.equals("E")) {
                exit = true;
                break;
            } else {
                pracid = Integer.parseInt(inputString);
            }
            sqlString = "SELECT pracid from Midwife WHERE pracid = " + pracid;
            rs = JDBC.query(statement, sqlString);
            assert rs != null;
            if (!rs.next()) {
                System.err.println("Error!! id is not in the database!");
            } else {
                login = true;
            }
        }

        boolean back = false;
        ArrayList<Appointment> resultAppointments = new ArrayList<>(); //TODO: make sure to clear this after a new date is chosen
        Appointment curr;
        String date;
        String todayDatetime = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
        String todayDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date());

        if (!exit) {
            // main loop
            loop:
            while (true) {
                // if we didn't come back from the menu, ask for new date
                if (!back) {
                    // 2. appointment date
                    System.out.println(enterApptDate);
                    inputString = input.nextLine();
                    if (inputString.equals("E")) {
                        break;
                    } else {
                        date = inputString;
                    }
                    sqlString = "WITH Appts AS ( SELECT apptID, datetime, pregNum, parentID FROM Appointment"
                            + " WHERE pracID = " + pracid
                            + " AND DATE(datetime) = '" + date + "')"
                            + " SELECT Appts.apptID, TIME(Appts.datetime) time, X.pri, X.name, X.hcardID, Appts.pregNum, Appts.parentID"
                            + " FROM Appts JOIN (SELECT A.pri, M.name, M.hcardID, A.pregNum, A.parentID"
                            + " FROM AssignedMidwife A, Parents P, Mother M"
                            + " WHERE A.pracID = " + pracid
                            + " AND A.parentID = P.parentID AND P.hcardID = M.hcardID)X"
                            + " ON Appts.pregNum = X.pregNum AND Appts.parentID = X.parentID"
                            + " ORDER BY time";
                    rs = JDBC.query(statement, sqlString);

                    assert rs != null;
                    if (!rs.next()) {
                        System.out.println("No appointments for date: " + date);
                        continue;
                    } else {
                        System.out.println();
                        do {
                            curr = new Appointment(rs.getRow(), rs.getInt("apptID"), rs.getTime("time"), rs.getBoolean("pri"), rs.getString("name"), rs.getString("hcardID"), rs.getInt("pregNum"), rs.getInt("parentID"));
                            resultAppointments.add(curr);
                            System.out.println(curr.printAppt());
                        } while (rs.next());
                    }
                } else {
                    // otherwise reprint previous date appointments
                    for (Appointment resultAppointment : resultAppointments) {
                        System.out.println(resultAppointment.printAppt());
                    }
                    back = false;
                }

                // 3. Choose an appointment to view
                int row;
                System.out.println(enterApptNum);
                inputString = input.nextLine();
                if (inputString.equals("E")) {
                    break;
                } else if (inputString.equals("D")) {
                    continue;
                } else {
                    row = Integer.parseInt(inputString);
                    curr = resultAppointments.get(row - 1);
                }

                // 4. Menu for appointment
                System.out.println("For " + curr.getNameID());
                System.out.println(enterMenuChoice);
                int menu = input.nextInt();
                input.nextLine(); //throw away the \n not consumed by nextInt()
                while (menu != 5) {

                    switch (menu) {

                        // notes for this pregnancy
                        case 1:
                            sqlString = "SELECT datetime, note FROM Observation"
                                    + " WHERE apptID IN ("
                                    + " SELECT apptID FROM Appointment"
                                    + " WHERE pracID = " + pracid
                                    + " AND pregNum = " + curr.getPregNum()
                                    + " AND parentID = " + curr.getParentID() + ")"
                                    + " ORDER BY datetime DESC";
                            rs = JDBC.query(statement, sqlString);
                            assert rs != null;
                            if (!rs.next()) {
                                System.out.println("No notes for pregnancy");
                            } else {
                                do {
                                    System.out.println(rs.getTimestamp("datetime") + "\t" + rs.getString("note"));
                                } while (rs.next());
                            }
                        break;

                        // tests for the mother during this pregnancy
                        case 2:
                            sqlString = "SELECT DATE(datePresc) date, tType, tResult FROM Test"
                                    + " WHERE pregNum = " + curr.getPregNum()
                                    + " AND parentID = " + curr.getParentID()
                                    + " AND pracID = " + pracid
                                    + " AND testID NOT IN (SELECT testID FROM TestBaby)"
                                    + " ORDER BY date DESC";
                            rs = JDBC.query(statement, sqlString);
                            assert rs != null;
                            if (!rs.next()) {
                                System.out.println("No tests for pregnancy");
                            } else {
                                do {
                                    String result = rs.getString("tResult");
                                    if (rs.wasNull()) {
                                        result = "PENDING";
                                    }
                                    System.out.println(rs.getDate("date") + " [" + rs.getString("tType") + "] " + result);
                                } while (rs.next());
                            }
                        break;

                        // add a note
                        case 3:
                            System.out.println(enterNote);
                            inputString = input.nextLine();
                            sqlString = "INSERT INTO Observation(datetime,note,apptid)"
                                    + " VALUES ('"
                                    + todayDatetime + "', '"
                                    + inputString + "',"
                                    + curr.getApptID() + ")";
                            JDBC.insert(statement, sqlString);
                        break;

                        // prescribe a test
                        case 4:
                            System.out.println(enterTestType);
                            inputString = input.nextLine();
                            sqlString = "INSERT INTO Test(ttype,datepresc,datesampl,pracid,pregnum,parentid)"
                                    + " VALUES ('"
                                    + inputString + "', '"
                                    + todayDate + "', '"
                                    + todayDate + "', "
                                    + pracid + ","
                                    + curr.getPregNum() + ","
                                    + curr.getParentID() + ")";
                            JDBC.insert(statement, sqlString);
                        break;
                    }
                    System.out.println("\nFor " + curr.getNameID());
                    System.out.println(enterMenuChoice);
                    menu = input.nextInt();
                    input.nextLine(); //throw away the \n not consumed by nextInt()
                }
                // go back to appointments list for entered date
                back = true;
            }
        }

        // Finally but importantly close the statement and connection
        statement.close();
        con.close();
    }
}