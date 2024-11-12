package dev.lpa;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class Main {
  
  public static void main(String[] args) {
    System.setProperty("user.timezone", "America/New_York");
    Locale.setDefault(Locale.US);
    
    ZoneId zoneNewYork = ZoneId.of("America/New_York");
    ZoneId zoneSydney = ZoneId.of("Australia/Sydney");
    
    ZonedDateTime zdtNow = ZonedDateTime.now().withMinute(0).withSecond(0);
    DateTimeFormatter dtfNY =
      DateTimeFormatter.ofPattern("EEEE, MMMM d yyyy, h:mm a", Locale.US);
    DateTimeFormatter dtfSY =
      DateTimeFormatter.ofPattern("EEEE, MMMM d yyyy, h:mm a",
        Locale.of("en", "AU"));
    
    Stream.iterate(zdtNow, zdt -> zdt.isBefore(zdtNow.plusDays(10)), dt -> dt.plusHours(1))
      .filter(zdt -> isValidTime(zdt, zdtNow, zoneNewYork, zoneSydney))
      .forEach(zdt -> printFormattedDate(zdt, zoneNewYork, zoneSydney, dtfNY, dtfSY));
  }
  
  private static boolean isValidTime(ZonedDateTime zdt, ZonedDateTime zdtNow, ZoneId zoneNewYork,
                                     ZoneId zoneSydney) {
    ZonedDateTime zdtNY = zdt.withZoneSameInstant(zoneNewYork);
    ZonedDateTime zdtSY = zdt.withZoneSameInstant(zoneSydney);
    for (var z : List.of(zdtNY, zdtSY)) {
      if (z.getDayOfYear() == zdtNow.withZoneSameInstant(z.getZone()).getDayOfYear()
            || z.getDayOfWeek() == DayOfWeek.SATURDAY || z.getDayOfWeek() == DayOfWeek.SUNDAY
            || z.getHour() < 7 || z.getHour() > 20
            || (z.getHour() == 20 && (z.getMinute() > 0 || z.getSecond() > 0))) {
        return false;
      }
    }
    return true;
  }
  
  private static void printFormattedDate(ZonedDateTime zdt, ZoneId zoneNewYork, ZoneId zoneSydney
    , DateTimeFormatter dtfNY, DateTimeFormatter dtfSY) {
    ZonedDateTime zdtNY = zdt.withZoneSameInstant(zoneNewYork).withMinute(0);
    ZonedDateTime zdtSY = zdt.withZoneSameInstant(zoneSydney).withMinute(0);
    System.out.println("Jane [" + zdtNY.getZone() + "] : " + zdtNY.format(dtfNY)
                         + " <---> Joe [" + zdtSY.getZone() + "] : " + zdtSY.format(dtfSY));
  }
}