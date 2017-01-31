package util;

public class EnFrString {
   private static int currentLanguage = 0;

   private String[] strings = new String[2];

   public static void setCurrentLanguage(String language) {
      switch (language) {
         case "English":
            currentLanguage = 0;
            break;
         case "French":
            currentLanguage = 1;
            break;
         default:
            throw new IllegalArgumentException("Tried to set language to an unavailable language.");
      }
   }

   public static String getCurrentLanguage() {
      switch (currentLanguage) {
         case 0:
            return "English";
         case 1:
            return "French";
      }
      return null;//unreachable
   }

   public EnFrString(String enString, String frString) {
      if (enString == null || frString == null)
         throw new IllegalArgumentException("Tried to initiate a 2 language string without storing any strings.");
      strings[0] = enString;
      strings[1] = frString;
   }

   public String getStrings(String language) {
      switch (language) {
         case "English":
            return strings[0];
         case "French":
            return strings[1];
         default:
            throw new IllegalArgumentException("Tried to get a string in an unavailable language.");
      }
   }

   public String toString() {
      return strings[currentLanguage];
   }
}
