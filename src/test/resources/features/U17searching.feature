@authoriseUser
Feature: Renovation search functionality

  Scenario: AC5 - Pagination for large numbers of records
    Given I have 20 records
    When I have run a search for "Renovation"
    Then I see pagination metadata with 2 total pages and page 1 selected
    And I see 16 records

  Scenario Outline: AC6 - Navigate to page
    Given I have <records> records
    And I have run a search for ""
    And I see pagination metadata with <pages> total pages and page 1 selected
    When I click on page number <page number>
    Then I see pagination metadata with <pages> total pages and page <page number> selected
    And I see the list of records corresponding to page <page number>

    Examples:
      | page number | records | pages |
      | 1           | 10      | 1     |
      | 2          | 20      | 2     |
      | 3           | 64      | 4     |

  Scenario: AC7 - More than 10 pages
    Given I have 100 records
    When I have run a search for ""
    And I click on page number 5
    Then I see pagination metadata with 7 total pages and page 5 selected

  Scenario: AC8 - Input page number
    Given I have 100 records
    When I have run a search for ""
    Then I see pagination metadata with 7 total pages and page 1 selected

  Scenario: AC9 - Page number in range
    Given I have 100 records
    And I have run a search for ""
    When I input page number 5 and confirm my choice
    Then I see pagination metadata with 7 total pages and page 5 selected
    And I see the list of records corresponding to page 5
