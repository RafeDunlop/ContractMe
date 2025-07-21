@authoriseUser
Feature: U23A - As Kaia, I want to see a calendar displaying my upcoming tasks, so I know which ones need to be prioritised.

  Scenario: AC1.1 - Display calendar on renovation records I own
    Given I have a renovation record
    When I navigate to my renovation record
    Then I see a calendar for the current month

  Scenario: AC1.2 - Calender doesn't display on public records I dont own
    Given I have a public renovation record
    When second user views my private renovation record
    Then they do not see a calendar

  Scenario Outline: AC3 - Month navigation buttons
    Given I have a renovation record
    When I navigate to my renovation record
    And calendar year is <year> and month is <month>
    Then I see a button for the previous and next month
        Examples:
          | year  | month  |
          | 2000  | 1      |
          | 5000  | 12     |
          | 1     | 5      |
          | 2025  | 7      |
          | -1023 | 2      |
          | 0     | 4      |
          | 45    | 13     |
          | 2020  | 0      |
          | 10000 | -20    |

  Scenario Outline: AC4.1 - Month previous button functionality
    Given I have a renovation record
    When I navigate to my renovation record
    And calendar year is <year> and month is <month>
    And I click the previous month button
    Then I see a calendar for the previous month
        Examples:
          | year  | month  |
          | 2000  | 1      |
          | 5000  | 12     |
          | 1     | 5      |
          | 2025  | 7      |
          | -1023 | 2      |
          | 0     | 4      |
          | 45    | 13     |
          | 2020  | 0      |
          | 10000 | -20    |

  Scenario Outline: AC4.2 - Month next button functionality
    Given I have a renovation record
    When I navigate to my renovation record
    And calendar year is <year> and month is <month>
    And I click the next month button
    Then I see a calendar for the next month
        Examples:
          | year  | month  |
          | 2000  | 1      |
          | 5000  | 12     |
          | 1     | 5      |
          | 2025  | 7      |
          | -1023 | 2      |
          | 0     | 4      |
          | 45    | 13     |
          | 2020  | 0      |
          | 10000 | -20    |

  Scenario: AC5 - Highlight the current date on calendar
    Given I have a renovation record
    When I navigate to my renovation record
    And I see a calendar for the current month
    Then today's date is highlighted