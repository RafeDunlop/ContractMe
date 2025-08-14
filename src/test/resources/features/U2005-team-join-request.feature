Feature: As Bob, I want to be able to receive requests from clients and an email to notify me that I have gotten a request.

  @authoriseContractor
  Scenario Outline: AC5 - Pending team request, gives the contractor access to view a renovation
    Given A private renovation exists with a team
    And I am logged in and a contractor
    And my request to join the renovation team is "<status>"
    When I view the renovation
    Then I can view the renovation record

    Examples:
      | status   |
      | pending  |
      | accepted |
