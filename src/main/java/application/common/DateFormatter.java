package application.common;

public class DateFormatter {

   public static String secondsToHHMMSS(final long currentWorkSeconds) {
      final int hours = (int) (currentWorkSeconds / 3600);
      final int minutes = (int) ((currentWorkSeconds % 3600) / 60);

      final int sec = (int) (((currentWorkSeconds % 3600) % 60));

      final String a = (hours > 9 ? hours : "0" + hours) + ":" + (minutes > 9 ? minutes : "0" + minutes) + ":"
            + (sec > 9 ? sec : "0" + sec);
      return a;
   }

}
