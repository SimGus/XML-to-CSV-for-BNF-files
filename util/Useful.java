package util;

public class Useful {
   /*
    * Return a string that is the same string as @str but without the whitespaces (' ', '\t' and '\n') at the beginning and the end
    * Removes also the literal "\\n" and "\\t"
    */
   public static String trim(String str) {
      if (str == null || str.length() <= 0)
         return "";

      int i;
      for (i=0; i<str.length(); i++) {
         if (str.charAt(i) != ' ' && str.charAt(i) != '\t' && str.charAt(i) != '\n') {
            if (i+1 >= str.length())
               break;
            if (str.charAt(i) == '\\' && (str.charAt(i+1) == 'n' || str.charAt(i+1) == 't')) {
               i++;
               continue;
            }
            else
               break;
         }
      }

      int j;
      for (j=str.length()-1; j>=0; j--) {
         if (str.charAt(j) != ' ' && str.charAt(j) != '\t' && str.charAt(j) != '\n') {
            if (j-1 <= 0)
               break;
            if (str.charAt(j-1) == '\\' && (str.charAt(j) == 'n' || str.charAt(j) == 't')) {
               j--;
               continue;
            }
            else
               break;
         }
      }

      if (j<i)
         return "";
      return str.substring(i, j+1);
   }

   /*
    * Returns @true iff @str contains no useful information (e.g. only spaces)
    */
   public static boolean isStringEmpty(String str) {
      if (str == null || str.length() <= 0)
         return true;

      //remove first spaces
      int i = 0;
      char c = str.charAt(0);
      while (c==' ' || c=='\t' || c=='\n') {
         if (++i >= str.length())
            return true;
         c = str.charAt(i);
      }
      // if (str.substring(i).length() <= 0)
      //    return true;//normally never reached

      return false;
   }
}
