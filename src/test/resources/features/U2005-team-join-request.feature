Feature: As Bob, I want to be able to receive requests from clients and an email to notify me that I have gotten a request.

  @authoriseContractor
  Scenario: AC3.1 - accept team request
    Given A private renovation exists with a team
    And I am logged in and a contractor
    And my request to join the renovation team is "pending"
    When I click the "accept" button
    Then I am taken to the renovation page
    And I am in the team

  @authoriseContractor
  Scenario: AC3.2 - accept team request when already accepted
    Given A private renovation exists with a team
    And I am logged in and a contractor
    And my request to join the renovation team is "accepted"
    When I click the "accept" button
    Then I am shown an error page displaying "Unable to accept invitation, link is no longer valid."

  @authoriseContractor
  Scenario: AC3.3 - accept team request when not part of team request
    Given A private renovation exists with a team
    And I am logged in and a contractor
    When I click the "accept" button
    Then I am shown an error page displaying "Unable to accept invitation, link is no longer valid."

  @authoriseContractor
  Scenario: AC4.1 - decline team request
    Given A private renovation exists with a team
    And I am logged in and a contractor
    And my request to join the renovation team is "pending"
    When I click the "decline" button
    Then I am taken to the view requests page
    And I am not in the team

  @authoriseContractor
  Scenario: AC4.2 - decline team request when already accepted
    Given A private renovation exists with a team
    And I am logged in and a contractor
    And my request to join the renovation team is "accepted"
    When I click the "decline" button
    Then I am shown an error page displaying "Unable to decline invitation, link is no longer valid."

  @authoriseContractor
  Scenario: AC4.3 - decline team request when not part of team
    Given A private renovation exists with a team
    And I am logged in and a contractor
    When I click the "decline" button
    Then I am shown an error page displaying "Unable to decline invitation, link is no longer valid."

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

