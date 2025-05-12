Feature: U19 - As Inaya, I want to be able to make my renovation record
  public so that I can share my progress with other.

   Scenario Outline: AC1 Making renovation public
     Given I am logged in
     When I tick the checkbox labelled make my renovation record public
     Then It should be visible in public search results for all logged in users

   Scenario Outline: AC2 Making renovation private
     Given I am logged in
     When I untick the checkbox labelled make my renovation record public
     Then It should be invisible in public search results for all logged in users

  Scenario Outline: AC3 Viewing public renovations sorted by most recently created
    Given I am logged in
    And there are 6 public renovation records
    When I click the browse renovations button
    Then I should be on the renovation search page
    And I should see a list of public renovation records
    And the renovation records should be sorted by most recent creation date first


  Scenario: AC4 Pagination show Prev/Next buttons
    Given I am logged in
    And there are 20 public renovation records
    When I click the browse renovations button
    And there is at least 2 pages
    Then I should see a "Prev" element
    And I should see a "Next" element

  Scenario: AC5.1 Pagination when over 10 pages can only see 2 pages ahead of current page.
    Given I am logged in
    And there are 150 public renovation records
    When I click the browse renovations button
    And there is at least 10 pages
    Then I should see a "1" element
    And I should see a "2" element
    And I should see a "3" element

  Scenario: AC5.2 Pagination over 10 pages so there are first/last buttons
    Given I am logged in
    And there are 150 public renovation records
    When I click the browse renovations button
    And there is at least 10 pages
    Then I should see a "First" element
    And I should see a "Last" element


  Scenario: AC6 User can input page to go to
    Given I am logged in
    And there are 150 public renovation records
    When I click the browse renovations button
    And there is at least 10 pages
    Then I should see a "pageSearch" element


  Scenario: AC9 Viewing renovation record details from search results
    Given I am logged in
    And there are 6 public renovation records
    When I click the browse renovations button
    And I click on a renovation record
    Then I should see the details of that renovation record

  Scenario: AC10 Back to search results returns me to the same search page
    Given I am logged in
    And I have searched for visibility: <visibility> and search term: <searchTerm> renovation records
    And I click on a renovation record
    When I click the “Back to search results” button
    Then I should see the list of renovation records at the same search page I was on
    Examples:
          | visibility  | searchTerm |
          |    "user"   |    ""      |
          |   "public"  |   "the"    |
          |    "all"    |   "test"   |
