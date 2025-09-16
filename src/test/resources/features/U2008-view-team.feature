Feature: 2008 - As Sarah, I want to see the contractors being assigned to my renovation record
  which I have created a team for so that I know which contractors have been assigned to my team.

  @authoriseUser
  Scenario: AC2 - Users can view their team with contractors assigned to each role.
    Given a contractor is assigned to a role in a team
    When I click the View Team button
    Then I see the contractor's name and profile picture

  @authoriseUser
  Scenario: AC3 -  Users can view their team with no contractors assigned to each role.
    Given a team has no contractors assigned
    When I click the View Team button
    Then I see a placeholder