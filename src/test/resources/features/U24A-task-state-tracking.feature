@authoriseUser
Feature: Task state tracking
  Scenario: Newly created task state is set to "Not Started"
    Given that i create a task on a renovation record
    When I view the task
    Then then the task is set to "Not Started"
    And I can see the task state

  Scenario Outline: Task state updates and displays correct color
    Given that I am viewing one of my renovation records with tasks
    When I select "<state>" from the task state dropdown
    Then the task state is updated to "<state>"
    And the task state displays the color "<color>"

    Examples:
      | state        | color    |
      | Not Started  | grey     |
      | In Progress  | blue     |
      | Blocked      | red      |
      | Completed    | green    |
      | Cancelled    | darkgrey |