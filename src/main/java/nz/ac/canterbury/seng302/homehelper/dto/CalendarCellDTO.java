package nz.ac.canterbury.seng302.homehelper.dto;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import java.time.LocalDate;
import java.util.List;

public record CalendarCellDTO(LocalDate day, List<RenovationTask> calendarTasks) {


}
