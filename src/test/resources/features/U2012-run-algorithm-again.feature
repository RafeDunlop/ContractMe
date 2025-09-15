Feature: As Kaia, I want the algorithm to look for a new contractor
  when a contractor assigned to role for a team I own rejects or is removed from the position
  so that my team continues to fill despite changes.

  @authoriseContractor
  Scenario: AC4 - Contractor who rejects an invitation should not be re-invited
    Given A contractor has received an invitation for a role in a team
    When The contractor rejects the invitation
    Then The contractor does not receive any more invitations to join a role on the team

  Scenario: AC5 - Contractor who is removed from a team isn't invited back
    Given that I am on the team details page for a team that I own
    When I remove a contractor from a role
    Then that contractor does not receive any more invitations to join a role on the team