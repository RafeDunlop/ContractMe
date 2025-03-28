Feature: U7 - As Sarah, I want to be able to change my password, so that I can keep my account secured with a new password in case my password gets leaked

    Scenario: AC2 - Inputted old password must match user's old password
        Given I am on the change password form
        And I enter <old_password> that does not match my current password <current_password>
        When I click the “Submit” button
        Then An error message tells me “Your old password is incorrect"
        And The password stays as <current_password>
        Example:
            | old_password | current_password |
            | Test123! | Test1234! |
            | abcdefG99? | abcdefG99! |

    Scenario: AC3 - Retyped new password must match new password.
        Given I am on the change password form
        And I enter <new_password> into "new" and enter <retype_new_password> into "retype new password" with the current password <current_password>
        When I click the “Submit” button
        Then An error message tells me “The new passwords do not match”
        And The password stays as <current_password>
        Example:
        | new_password | retype_new_password | current_password |
        | Test123! | Test1234! | Password123! |
        | abcdefG99? | abcdefG99! | HelloWorld99? |

    Scenario: AC4 - New password must be a "strong password".
        Given I am on the change password form
        And   I enter a weak password <weak_password> with the current password <current_password>
        When  I click the “Submit” button
        Then  An error message tells me “Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.”
        And   The password stays as <current_password>
        Example:
        | weak_password | current_password |
        | Password | Password123! |
        | test123! | HelloWorld99! |

