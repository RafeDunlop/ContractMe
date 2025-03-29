Feature: Sample Feature to verify cucumber and pipeline setup
    Scenario: Verify cucumber setup
        Given I have a cucumber setup
        When I run the tests
        Then I should see the results in the console
