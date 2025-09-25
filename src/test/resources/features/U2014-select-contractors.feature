Feature: As Lei, I want to browse for contractors with a particular skill by location on a map so that I can customize
  which contractors are sent invites to join my team.

  @authoriseUser
  Scenario: AC4 - View profile pictures on the map
    Given I have a team with an unfilled "ARCHITECTURE" role for a renovation at 0, 0
    And There are contractors
      | first_name | last_name | email                    | available | latitude | longitude | skill        |
      | Bobby      | Tables    | bobby@contractor.nz      | true      | 0.123    | 0.123     | ARCHITECTURE |
      | Sean       | Bean      | seanbean@unavailable.net | false     | 0.12     | 0.14      | ARCHITECTURE |
      | Stephen    | Strange   | stephen@strange.com      | true      | -15.03   | 48.82     | ARCHITECTURE |
      | Tony       | Stark     | tony@stark.org           | true      | 0.1      | 0.1       | PLUMBING     |
    When I view the map to invite a contractor for the "ARCHITECTURE" role
    Then I see a map showing the profile pictures of
      | bobby@contractor.nz |