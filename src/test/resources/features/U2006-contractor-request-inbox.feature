Feature: U2006 - As Bob, I want to be able to see all the requests which I have received to participate in a renovation team
  and accept or decline them

  @authoriseContractor
  Scenario: AC1.1 - Contractors can view their team requests on the inbox page
    Given I have received a team request from another user
    When I click the view requests button in the navigation page
    Then I am taken to the contractor team request inbox where I can see requests from clients

  @authoriseUser
  Scenario: AC1.2 - Users can't view the team request inbox page
    When I click the view requests button in the navigation page
    Then I am redirected to the main page

  @authoriseContractor
  Scenario: AC2 - Request form on the inbox
    Given I have received a team request from another user
    When I click the view requests button in the navigation page
    And I click a request from a client
    Then I am taken to a form where I can see the details of my role, the renovation, and the client
    And I can see buttons to accept or decline the request

  @authoriseContractor
  Scenario: AC5 - Have not received any requests message
    Given I have not received any requests
    When I click the view requests button in the navigation page
    Then I see a message telling me that I have not received any requests yet
