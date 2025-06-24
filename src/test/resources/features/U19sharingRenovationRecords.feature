@autoriseUser
Feature: U19 - As Inaya, I want to be able to make my renovation record
  public so that I can share my progress with others.

  Scenario: AC1 - Making renovation public
    Given I am logged in
    When I tick the checkbox labelled make my renovation record public
    Then It should be visible in public search results for all logged in users

  Scenario: AC2 - Making renovation private
    Given I am logged in
    When I untick the checkbox labelled make my renovation record public
    Then It should be invisible in public search results for all logged in users

  Scenario: AC3 - Viewing public renovations sorted by most recently created
    Given I am logged in
    And there are 6 public renovation records
    When I click the browse renovations button
    Then I see pagination metadata with page 1 selected and 1 total pages
    And I see 6 records on the page

  Scenario: AC4 - Pagination shows Prev/Next buttons
    Given I am logged in
    And there are 20 public renovation records
    When I click the browse renovations button
    Then I see pagination metadata with page 1 selected and 2 total pages
    And I see 16 records on the page

  Scenario: AC5.1 - Pagination over 10 pages shows only 2 pages ahead
    Given I am logged in
    And there are 200 public renovation records
    When I click the browse renovations button
    And I click on page 1
    Then I see pagination metadata with page 1 selected and 13 total pages

  Scenario: AC5.2 - First/Last buttons appear when over 10 pages
    Given I am logged in
    And there are 200 public renovation records
    When I click the browse renovations button
    And I click on page 5
    Then I see pagination metadata with page 5 selected and 13 total pages

  Scenario: AC5.3 - First/Last buttons do not show when less than 10 pages
    Given I am logged in
    And there are 20 public renovation records
    When I click the browse renovations button
    Then I see pagination metadata with page 1 selected and 2 total pages

  Scenario: AC6.1 - User can input page to go to when over 10 pages
    Given I am logged in
    And there are 200 public renovation records
    When I input page number 5 and confirm my choice
    Then I see pagination metadata with page 5 selected and 13 total pages
    And I see the list of records corresponding to page 5

  Scenario: AC6.2 - User cannot input page if there are less than 10 pages
    Given I am logged in
    And there are 20 public renovation records
    When I click the browse renovations button
    Then I see pagination metadata with page 1 selected and 2 total pages

  Scenario: AC9 - Viewing renovation record details from search results
    Given I am logged in
    And there are 6 public renovation records
    When I click the browse renovations button
    And I click on a renovation record
    Then I should see the details of that renovation record

  Scenario Outline: AC10 - Back to search results returns me to same search page
    Given I am logged in
    And there are 6 public renovation records
    And I have searched for visibility: <visibility> and search term: <searchTerm> renovation records
    And I click on a renovation record
    When I click the “Back to search results” button
    Then I should see the list of renovation records at the same search page I was on

    Examples:
      | visibility  | searchTerm |
      | "user"      | ""         |
      | "public"    | "the"      |
      | "all"       | "test"     |
