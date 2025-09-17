Feature: As Kaia, I want the algorithm to look for a new contractor
  when a contractor assigned to role for a team I own rejects or is removed from the position
  so that my team continues to fill despite changes.

  ## TODO: Uncomment after merge with blacklist
#  @authoriseContractor
#  Scenario: AC1 - Contractor rejects invitation and next closest contractor is invited
#    Given A contractor has received an invitation for a role in a team
#    And Another eligible contractor exists for that role
#    When The contractor rejects the invitation
#    Then the next closest eligible contractor receives an invitation to join that role
