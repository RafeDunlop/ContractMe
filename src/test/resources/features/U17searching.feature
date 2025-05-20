@authoriseUser
Feature: Renovation search functionality
    Scenario: AC5 - Pagination for large numbers of records
        Given I have 20 records
        When I have run a search for "Renovation"
        Then I see a "nextButton" button
        And I see a "page1" button
        And I see a list of 8 records

    Scenario Outline: AC6 - Navigate to page
        Given I have <records> records
        And I have run a search for ""
        And I see a list of 8 records
        And I see pagination numbers for <pages> pages
        When I click on page number <page number>
        Then I see the list of records corresponding to page <page number>
        And Page number <page number> is currently highlighted
        Examples:
            | page number | records | pages |
            | 1           | 10      | 2     |
            | 2           | 20      | 3     |
            | 4           | 50      | 7     |

    Scenario: AC7 - More than 10 pages
        Given I have 100 records
        When I have run a search for ""
        And I click on page number 5
        Then I see a "firstButton" button
        And I see a "lastButton" button
        And I see a "page3" button
        And I see a "page7" button

    Scenario: AC8 - Input page number
        Given I have 100 records
        When I have run a search for ""
        Then I see a "pageSearch" button

    Scenario: AC9 - Page number in range
        Given I have 100 records
        And I have run a search for ""
        When I input page number 5 and confirm my choice
        Then I see the list of records corresponding to page 5
