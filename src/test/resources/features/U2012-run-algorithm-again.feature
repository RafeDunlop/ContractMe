Feature: As Kaia, I want the algorithm to look for a new contractor
  when a contractor assigned to role for a team I own rejects or is removed from the position
  so that my team continues to fill despite changes.

  @authoriseContractor
  Scenario: AC1 - Contractor rejects invitation and next closest contractor is invited
    Given A contractor has received an invitation for a role in a team
    And Another eligible contractor exists for that role
    When The contractor rejects the invitation
    Then The next closest eligible contractor receives an invitation to join that role

  @authoriseContractor
  Scenario: AC3 - Rerun algorithm on teams with empty roles every 10 minutes
    Given That i have a team with a role that no contractor is eligible to fill
    When A contractor becomes eligible to fill the role
    Then An email invitation is sent to that contractor after no more than 10 minutes


  @authoriseContractor
  Scenario: AC4 - Contractor who rejects an invitation should not be re-invited
    Given A contractor has received an invitation for a role in a team
    When The contractor rejects the invitation
    Then The contractor does not receive any more invitations to join a role on the team

  @authoriseContractor
  Scenario: AC5 - Contractor who is removed from a team isn't invited back
    Given That I am own a team with a contractor who has accepted
    When I remove a contractor from a role
    Then That contractor does not receive any more invitations to join a role on the team


