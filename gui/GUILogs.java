package gui;

import java.util.ArrayList;
import java.util.Iterator;

import util.EnFrString;
import util.LogType;
import static util.LogType.*;

public class GUILogs implements Iterable<GUILogs.LogMsg> {
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

   protected ArrayList<LogMsg> messages = new ArrayList<LogMsg>();

   protected static EnFrString defaultLogMsg = new EnFrString("Ready to translate XML file.", "Prêt à traduire un fichier XML.");

   public GUILogs() {
      messages.add(new LogMsg(defaultLogMsg, NORMAL));
   }
   public GUILogs(EnFrString defaultMsg, LogType type) {
      messages.add(new LogMsg(defaultMsg, type));
   }
   public GUILogs(String defaultEnMsg, String defaultFrMsg, LogType type) {
      messages.add(new LogMsg(new EnFrString(defaultEnMsg, defaultFrMsg), type));
   }

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

   public LogsIterator<LogMsg> iterator() {return new LogsIterator();}
   protected class LogsIterator<LogMsg> implements Iterator {
      private int i = 0;
      public boolean hasNext() {return (i < messages.size());}
      public GUILogs.LogMsg next() {return messages.get(i++);}
      public void remove() {}
   }
}
