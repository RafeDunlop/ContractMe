Feature:As Kaia, I want to be able to add location to my profile and
  my renovation records so that I can keep track of where they are.
  Scenario: AC1: Given I register to the system, when I am asked to supply my details, then I can
  optionally supply my location.
    Given I am on the register form
    When I click the location toggle switch
    Then I am viewing the enter location details form


 Scenario Outline: AC7.1: I supply a suburb, when the suburb contains non valid characters (i.e. characters others than letters,
 hyphen, apostrophe, number, space), then a message tells me that “Suburb contains invalid characters.” and the form is
 not saved.
   Given I am viewing the enter location details form
   And I enter a valid address <address> on the location form on the <page_name> page
   When I enter an invalid suburb
   And I submit the form
   Then I am told that my suburb contains invalid characters

   Examples:
