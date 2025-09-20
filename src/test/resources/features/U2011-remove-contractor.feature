Feature: U2011 - As Lei, I want to remove a contractor from my renovation team so that I can get another contractor that fits my needs better

    @authoriseUser
    Scenario: AC2 - Contractor removal
        Given The "U2011 Renovation" renovation has a team with contractor "jimi@test.nz" assigned
        And Contractor "jimi@test.nz" has accepted the role in the team for "U2011 Renovation"
        And I am on the team details page for the "U2011" renovation
        When I confirm that I want to remove the contractor "jimi@test.nz"
        Then The contractor "jimi@test.nz" is removed from the team
