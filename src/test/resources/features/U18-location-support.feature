Feature:As Kaia, I want to be able to add location to my profile and
  my renovation records so that I can keep track of where they are.
  Scenario: AC1: Given I register to the system, when I am asked to supply my details, then I can
  optionally supply my location.
    Given I am on the register form
    When I click the location toggle switch
    Then I can see the add location input fields

  Scenario Outline: AC5.1:  on register page to give a location a street address with the street number must be supplied
    Given I am viewing the enter location details form on the register page
    When I leave the address field blank on the location form on the register page
    Then I am told that I must supply an address field



