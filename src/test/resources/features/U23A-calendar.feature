@authoriseUser
Feature: U23A - As Kaia, I want to see a calendar displaying my upcoming tasks, so I know which ones need to be prioritised.

  Scenario: AC1 - Display calendar on renovation records
    Given I have a renovation record
    When I navigate to my renovation record
    Then I see a calendar for the current month

  Scenario: AC5 - Highlight the current date on calendar
    Given I have a renovation record
    When I navigate to my renovation record
    And I see a calendar for the current month
    Then today's date is highlighted