package dev.lpa;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.zone.ZoneRules;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.time.format.DateTimeFormatter.ofLocalizedDateTime;
import static java.time.format.DateTimeFormatter.ofPattern;

public class Solution {
  
  private record Employee(String name, Locale locale, ZoneId zone) {
    
    public Employee(String name, String locale, String zone) {
      this(name, Locale.forLanguageTag(locale), ZoneId.of(zone));
    }
    
    public Employee(String name, Locale locale, String zone) {
      this(name, locale, ZoneId.of(zone));
    }
    
    String getDateInfo(ZonedDateTime zdt, DateTimeFormatter dtf) {
      return "%s [%s] : %s".formatted(
        name, zone, zdt.format(dtf.localizedBy(locale)));
    }
  }
  
  public static void main(String[] args) {
    
    Locale.setDefault(Locale.US);
    
    Employee jane = new Employee("Jane", Locale.US, "America/New_York");
    Employee joe = new Employee("Joe", "en-AU", "Australia/Sydney");
    
    ZoneRules janesRules = jane.zone.getRules();
    System.out.println(jane + " " + janesRules);
    ZoneRules joesRules = joe.zone.getRules();
    System.out.println(joe + " " + joesRules);
    
    ZonedDateTime janeNow = ZonedDateTime.now(jane.zone);
    ZonedDateTime joeNow = ZonedDateTime.of(janeNow.toLocalDateTime(), joe.zone);
    long hoursBetween = Duration.between(janeNow, joeNow).toHours();
    long minutesBetween = Duration.between(janeNow, joeNow).toMinutesPart();
    long secondsBetween = Duration.between(janeNow, joeNow).toSecondsPart();
    System.out.printf("Jane is " + (hoursBetween >= 0 ? "ahead" : "behind")
                        + " Joe by %d:%02d:%02d hours.%n",
      Math.abs(hoursBetween), Math.abs(minutesBetween), secondsBetween);
    
    System.out.println("Joe in daylight savings? " +
                         joesRules.isDaylightSavings(joeNow.toInstant()) + " " +
                         joesRules.getDaylightSavings(joeNow.toInstant()) + " " +
                         joeNow.format(ofPattern("zzzz z")));
    
    System.out.println("Jane in daylight savings? " +
                         janesRules.isDaylightSavings(joeNow.toInstant()) + " " +
                         janesRules.getDaylightSavings(joeNow.toInstant()) + " " +
                         janeNow.format(ofPattern("zzzz z")));
    
    int days = 10;
    var map = schedule(joe, jane, days);
    DateTimeFormatter dtf = ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.SHORT);
  }
  
  private static Map<LocalDate, List<ZonedDateTime>> schedule(Employee first, Employee second,
                                                              int days) {
    
    Predicate<ZonedDateTime> rules = zdt -> // Predicate can be changed or reused
                                       zdt.getDayOfWeek() != DayOfWeek.SATURDAY
                                         && zdt.getDayOfWeek() != DayOfWeek.SUNDAY
                                         && zdt.getHour() >= 7 && zdt.getHour() < 21;
    
    LocalDate startingDate = LocalDate.now().plusDays(2);
    
    return startingDate.datesUntil(startingDate.plusDays(10 + 1))
             .map(dt -> dt.atStartOfDay(first.zone()))
             .flatMap(dt -> IntStream.range(0, 24).mapToObj(dt::withHour))
             .filter(rules)
             .map(dtz -> dtz.withZoneSameInstant(second.zone()))
             .filter(rules)
             .collect(
               Collectors.groupingBy(ZonedDateTime::toLocalDate,
                 TreeMap::new, Collectors.toList()));
    
  }
}
