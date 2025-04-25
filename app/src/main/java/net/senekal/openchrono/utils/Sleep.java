package net.senekal.openchrono.utils;

import static java.lang.Thread.sleep;

public class Sleep {

   //Sleep in microseconds
    public static boolean ns(int ns) {
        if (ns < 0 || ns > 999_999_999) {
            return false;
        }
        try {
            sleep(0, ns);
            return true;
        } catch (InterruptedException ignored) {
            return false;
        }
    }

    //Sleep in milli Seconds
    public static boolean ms(long ms) {
        if (ms < 0) {
            return false;
        }
        try {
            sleep(ms);
            return true;
        } catch (InterruptedException ignored) {
            return false;
        }
    }

   //Sleep in Seconds
    public static boolean s(long s) {
        if (s < 0) {
            return false;
        }
        try {
            sleep(s * 1000);
            return true;
        } catch (InterruptedException ignored) {
            return false;
        }
    }
}
