Feature: As Bob, I want to be able to receive requests from clients and an email to notify me that I have gotten a request.

  @authoriseContractor
  Scenario: AC1 - Contractor Request Email
    Given A team request has been created for a renovation which has an available role
    When There is an available contractor eligible for that role
    Then The system will automatically send an email to the contractor who is closest to the renovation location

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
