package gui;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.SwingUtilities;

import util.EnFrString;
import util.LogType;
import static util.LogType.*;

public class GUILogs implements Iterable<GUILogs.LogMsg>, Runnable {
   public class LogMsg {
      private EnFrString string;
      private LogType type;

      public LogMsg(EnFrString msg, LogType type) {
         string = msg;
         this.type = type;
      }

      public EnFrString getMsg() {return string;}
      public String getString() {return string.toString();}
      public LogType getType() {return type;}
   }

   protected static final EnFrString defaultLogMsg = new EnFrString("Ready to translate XML file.", "Prêt à traduire un fichier XML.");

   protected ArrayList<LogMsg> messages = new ArrayList<LogMsg>();
   protected Window window;
   protected long lastTimeMs = System.currentTimeMillis();
   protected final long updateDelayMs = 750;//update the logs area not more than once every @updateDelayMs milliseconds
   protected int nbMsgOnLastUpdate = 0;

   //================ Constructor ========================
   public GUILogs(Window window) {
      this.window = window;
      messages.add(new LogMsg(defaultLogMsg, NORMAL));
   }
   public GUILogs(Window window, EnFrString defaultMsg, LogType type) {
      this.window = window;
      messages.add(new LogMsg(defaultMsg, type));
   }
   public GUILogs(Window window, String defaultEnMsg, String defaultFrMsg, LogType type) {
      this.window = window;
      messages.add(new LogMsg(new EnFrString(defaultEnMsg, defaultFrMsg), type));
   }

   //================= Adder ============================
   public void add(LogMsg logMsg) {
      messages.add(logMsg);
   }
   public void add(EnFrString msg, LogType type) {
      messages.add(new LogMsg(msg, type));
   }
   public void add(String enMsg, String frMsg, LogType type) {
      messages.add(new LogMsg(new EnFrString(enMsg, frMsg), type));
   }

   public void clear() {
      messages.clear();
   }

   //=============== Updater ======================
   public void run() {
      long now;
      while (true) {
         now = System.currentTimeMillis();
         if (now - lastTimeMs > updateDelayMs && messages.size() != nbMsgOnLastUpdate) {
            lastTimeMs = now;
            nbMsgOnLastUpdate = messages.size();
            SwingUtilities.invokeLater(new Runnable() {
               @Override
               public void run() {
                  window.updateLogs();
               }
            });
         }
         else {
            try {
               Thread.sleep(updateDelayMs/2);//Don't check too often but don't sleep for too long
            } catch (InterruptedException e) {
               add("The logs updating thread has been interrupted by another thread. Terminating the thread. Logs will not be updated any longer.",
                  "Le thread de mise à jour des messages de sortie a été interrompu par un autre thread. Arrêt du thread. Les messages de sortie ne seront plus mis à jour.",
                  LogType.ERROR);
               SwingUtilities.invokeLater(new Runnable() {
                  @Override
                  public void run() {
                     window.updateLogs();
                  }
               });
               Thread.currentThread().interrupt();//Terminate the thread
            }
         }
      }
   }

   //============ Iterator ===================
   public LogsIterator<LogMsg> iterator() {return new LogsIterator();}
   protected class LogsIterator<LogMsg> implements Iterator {
      private int i = 0;
      public boolean hasNext() {return (i < messages.size());}
      public GUILogs.LogMsg next() {return messages.get(i++);}
      public void remove() {}
   }
}
