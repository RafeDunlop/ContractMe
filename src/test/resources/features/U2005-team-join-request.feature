Feature: As Bob, I want to be able to receive requests from clients and an email to notify me that I have gotten a request.


  @authoriseContractor
  Scenario: AC1 - Contractor Request Email
    Given There is an available contractor eligible for that role
    When A team request has been created for a renovation which has an available role
    Then The system will automatically send an email to the contractor who is closest to the renovation location

  @authoriseContractor
  Scenario: AC2 - Request Email Links to Renovation
    Given A team request email has been received
    When I click the link contained therein
    Then I am taken to the confirm join team page

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
    Then I am taken to the main page
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