@authoriseUser
Feature: Renovation search functionality
    Scenario: AC5 - Pagination for large numbes of records
        Given I have run a search
        When I have 20 records
        Then I should see a "nextButton" element
        And I should see a "prevButton" element

    Scenario Outline: AC6 - Navigate to page
        Given I see a list of <records> records
        And I see pagination numbers for <pages> pages
        When I click on page number <page number>
        Then I see the list of records corresponding to page <page number>
        And Page number <page number> is currently highlighted
        Examples:
            | page number | records | pages |
            | 1           | 10      | 2     |
            | 2           | 20      | 4     |
            | 4           | 50      | 10    |
