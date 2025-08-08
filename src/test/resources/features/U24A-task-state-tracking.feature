@authoriseUser
Feature: AC1 - Task state tracking
  Scenario: Newly created task state is set to "Not Started"
    Given that i create a task on a renovation record
    When I view the task
    Then then the task is set to "Not Started"
    And I can see the task state

  Scenario Outline: AC2 - Task state is updated with color change
    Given that I am viewing one of my renovation records with tasks
    When I update the task state "<state>"
    Then the task state is set to state "<state>"

    Examples:
      | state        |
      | NOT_STARTED  |
      | IN_PROGRESS  |
      | BLOCKED      |
      | COMPLETED    |
      | CANCELLED    |

  Scenario Outline: AC3 - Task state is reflected with correct highlight colour in calendar
    Given that I am viewing one of my renovation records with tasks
    And the task has the state "<state>" and is due today
    When I view the task in the calendar
    Then the task should be highlighted with the colour "<colour>" corresponding to the it's state

    Examples:
      | state        | colour |
      | NOT_STARTED  | #c2cad1 |
      | IN_PROGRESS  | #1982c4 |
      | BLOCKED      | #ffca3a |
      | COMPLETED    | #8ac926 |
      | CANCELLED    | #ff595e |