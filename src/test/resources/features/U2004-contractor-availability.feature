@authoriseContractor
Feature: As Bob, I want to be able to change the status of my availability for work so I can be eligible to be added to a new renovation team.

  Scenario: AC1 - Toggle availability on
    Given I am logged in as a contractor
    And I am on the profile page and the job availability option is off
    When I toggle the option to on
    Then when I reload the page my job availability status is saved as available


  Scenario: AC2 - Toggle availability off
    Given I am logged in as a contractor
    And I am on the profile page and the job availability option is on
    When I toggle the option to off
    Then when I reload the page my job availability status is saved as unavailable
