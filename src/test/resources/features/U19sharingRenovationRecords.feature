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
