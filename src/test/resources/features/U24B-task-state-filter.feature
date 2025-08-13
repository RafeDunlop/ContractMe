@authoriseUser
Feature: U24B - As Lei, I want to be able to filter the tasks on one of my renovation records by state so that I can see only the pertinent tasks.
    Scenario Outline: AC1 - State filter option
        Given that I am viewing one of my renovation records with tasks
        When I view the tasks section
        Then I can select option <state> to filter tasks by task state

        Examples:
            | state         |
            | "Not Started" |
            | "In Progress" |
            | "Blocked"     |
            | "Completed"   |
            | "Cancelled"   |
            | "All"         |

    Scenario Outline: AC3 - Task state filter
        Given that I am viewing one of my renovation records with tasks
        And I have tasks
            | name     | state         |
            | "Task 1" | NOT_STARTED |
            | "Task 2" | NOT_STARTED |
            | "Task 3" | IN_PROGRESS |
            | "Task 4" | BLOCKED    |
            | "Task 5" | COMPLETED   |
            | "Task 6" | CANCELLED   |
        When I select the option <state> to filter tasks by state
        Then The page is reloaded with only the <num_tasks> tasks shown

        Examples:
            | state         | num_tasks |
            | "notStarted" | 2         |
            | "inProgress" | 1         |
            | "blocked"     | 1         |
            | "completed"   | 1         |
            | "cancelled"   | 1         |
            | "all"         | 6         |

