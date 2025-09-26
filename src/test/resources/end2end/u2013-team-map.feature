@loginUser
Feature: As Lei, I want to see the location of the renovation and team I'm viewing on a map so I know where they are

  Scenario: AC1 - Viewing renovation location marker on team view
    Given I am viewing a renovation I own
    When The renovation has an address listed
    Then There is a tab where I can see a map with a house icon at the renovation's address