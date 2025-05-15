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
