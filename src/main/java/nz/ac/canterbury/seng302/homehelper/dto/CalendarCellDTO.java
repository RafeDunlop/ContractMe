package nz.ac.canterbury.seng302.homehelper.dto;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record CalendarCellDTO(int day, int month, Map<LocalDate, List<RenovationTask>> calendarTasks) {


}
