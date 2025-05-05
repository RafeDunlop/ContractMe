@renovationDelete
Feature: U12 - As Kaia, I want to be able to delete my renovation records so that I can remove records that are no
  longer needed.

  Scenario Outline:
    Given I am an existing user
    And I have an existing renovation
    And I am on the confirmation prompt for deleting a renovation record
    And The renovation record has <number_of_tasks> task(s)
    When I click the "Delete" button
    Then The renovation record is permanently deleted
    Examples:
      | number_of_tasks |
      | 0               |
      | 1               |
      | 10              |