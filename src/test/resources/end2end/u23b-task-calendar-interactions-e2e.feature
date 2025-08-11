@loginUser
Feature: As Kaia, I want to be able to create and edit tasks directly from the calendar view.

  Scenario: AC1: Open create task form
    Given I navigate to the renovation
    When I double click an empty space on a day
    Then I see the add task form
    And the due date is set to the date I clicked

  Scenario: AC2
    Given I have a renovation with a task due tomorrow
    And I navigate to the renovation
    When I double click on the task in the calendar
    Then I see the edit task page