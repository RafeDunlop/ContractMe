@loginUser
Feature: As Kaia, I want to be able to create and edit tasks directly from the calendar view.

  Scenario: AC2
    Given I have a renovation with a task due tomorrow
    And I navigate to the renovation
    When I double click on the task in the calendar
    Then I see the edit task page