@authoriseUser
Feature: As Lei, I want to be able to filter the tasks on one of my renovation records by state so that I can see only the pertinent tasks.
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

    Scenario: AC2 - Default state filter
        Given that I am viewing one of my renovation records with tasks
        When I view the tasks section
        Then I see the option "All" pre-selected

    Scenario Outline: AC3 - Task state filter
        Given that I am viewing one of my renovation records with tasks
        And I have tasks
            | name | state |
            | "Task 1" | "Not Started" |
        When I select the option <state> to filter tasks by state
        Then The page is reloaded with only <state> tasks shown

        Examples:
            | state         |
            | "Not Started" |
            | "In Progress" |
            | "Blocked"     |
            | "Completed"   |
            | "Cancelled"   |
            | "All"         |

