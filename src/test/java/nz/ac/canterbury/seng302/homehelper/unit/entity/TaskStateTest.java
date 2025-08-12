package nz.ac.canterbury.seng302.homehelper.unit.entity;

import nz.ac.canterbury.seng302.homehelper.entity.TaskState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TaskStateTest {


    @ParameterizedTest
    @ValueSource(strings = {"notStarted", "inProgress", "blocked", "completed", "cancelled"})
    void testGetTaskStateFromCamelCase_validState_returnsTaskState(String state) {
        Assertions.assertDoesNotThrow(() -> TaskState.fromCamelCaseName(state));
    }

    @ParameterizedTest
    @ValueSource(strings = {"all", "sldkfsdl", "", "   "})
    void testGetTaskStateFromCamelCase_invalidState_throwsException(String state) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> TaskState.fromCamelCaseName(state));
    }
}
