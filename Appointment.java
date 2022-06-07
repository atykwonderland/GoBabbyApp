package GoBabbyApp;
import java.sql.* ;

public class Appointment {
    private final int row;
    private final int apptid;
    private final Time time;
    private final char pri;
    private final String name;
    private final String hcardID;
    private final int pregnum;
    private final int parentid;

    public Appointment(int r, int a, Time t, boolean p, String n, String h, int pn, int pid) {
        this.row = r;
        this.apptid = a;
        this.time = t;

        if (p) {
            this.pri = 'P';
        } else {
            this.pri = 'B';
        }

        this.name = n;
        this.hcardID = h;
        this.pregnum = pn;
        this.parentid = pid;
    }

    public String printAppt() {
        return String.format("%d: %s %c %s %s\n",
                this.row, this.time.toString(), this.pri, this.name, this.hcardID);
    }

    public int getApptID() {
        return this.apptid;
    }

    public String getNameID() {
        return this.name + " " + this.hcardID;
    }

    public int getPregNum() {
        return this.pregnum;
    }

    public int getParentID() {
        return this.parentid;
    }
}