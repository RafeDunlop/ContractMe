Feature:As Kaia, I want to be able to add location to my profile and
  my renovation records so that I can keep track of where they are.
  Scenario: AC1: Given I register to the system, when I am asked to supply my details, then I can
  optionally supply my location.
    Given I am on the register form
    When I click the location toggle switch
    Then I can see the add location input fields


