Feature: Task state tracking
  Scenario: Newly created task state is set to "Not Started"
    Given that i create a task on a renovation record
    When I view the task
    Then then the task is set to "Not Started"
    And I can see the task state