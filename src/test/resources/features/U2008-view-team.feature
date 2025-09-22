Feature: 2008 - As Sarah, I want to see the contractors being assigned to my renovation record
  which I have created a team for so that I know which contractors have been assigned to my team.

  @authoriseUser
  Scenario: AC2 - Users can view their team with contractors assigned to each role.
    Given a contractor is assigned and has accepted a role in the team
    When I click the View Team button
    Then I see the contractor's name and profile picture

  @authoriseUser
  Scenario: AC2.1 - Users can view their team with contractors assigned to each role.
    Given a contractor is assigned and has not accepted a role in the team
    When I click the View Team button
    Then I see the contractor's name and profile picture

  @authoriseUser
  Scenario: AC3 -  Users can view their team with no contractors assigned to each role.
    Given a team has no contractors assigned
    When I click the View Team button
    Then I see a placeholder

  @authoriseContractor
  Scenario: Contractors can view the team they are assigned to.
    Given I am assigned to the team and have accepted a role in the team
    When I click the View Team button
    Then I see the contractor's name and profile picture

  @authoriseContractor
  Scenario: Contractors can view the team they are assigned to but have not accepted.
    Given I am assigned to the team and have not accepted a role in the team
    When I click the View Team button
    Then I see the contractor's name and profile picture

  @authoriseUser
  Scenario: Users can not view teams they do not own
    Given I do not own the team
    When I visit the team page
    Then I get 404 error

  @authoriseUser
  Scenario: Users can not see the view team button on public renovations they do not own
    Given There is a public renovation I do not own
    When I visit the renovation record
    Then I can not see the view team button

  @authoriseContractor
  Scenario: Contractors can not view teams they are not in
    Given I am not assigned to a role in the team
    When I visit the team page
    Then I get 404 error

  @authoriseContractor
  Scenario: Contractors can see the view team button on public renovations they are not in
    Given There is a public renovation I am not assigned to
    When I visit the renovation record
    Then I can not see the view team button