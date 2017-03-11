package util;

public class Log {
   public static void log() {System.out.println("[LOG]");}
   public static void log(String msg) {System.out.println("[LOG] "+msg);}

   public static void warn() {System.out.println("[WARNING]");}
   public static void warn(String msg) {System.out.println("[WARNING] "+msg);}

   public static void err() {System.err.println("[ERR]");}
   public static void err(String msg) {System.err.println("[ERR] "+msg);}

   public static void fct(int level, String functionName) {
      if (level <= 2)
         System.out.println("{FCT} "+functionName+" ("+level+")");
   }
   public static void fct(int level, String functionName, String msg) {
      fct(level, functionName+" : "+msg);
   }

   public static void displayed(String msg) {/*System.out.println("[WIN] "+msg);*/}
}
