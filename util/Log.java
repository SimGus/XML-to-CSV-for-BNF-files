package util;

public class Log {
   public static void log() {System.out.println("[LOG]");}
   public static void log(String msg) {System.out.println("[LOG] "+msg);}

   public static void warn() {System.out.println("[WARNING]");}
   public static void warn(String msg) {System.out.println("[WARNING] "+msg);}

   public static void err() {System.err.println("[ERR]");}
   public static void err(String msg) {System.err.println("[ERR] "+msg);}
}
