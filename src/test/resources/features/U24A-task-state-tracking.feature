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