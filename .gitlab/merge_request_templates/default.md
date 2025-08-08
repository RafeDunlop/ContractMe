# Merge %{source_branch} to %{target_branch}

## Summary 

[Insert summary here]

## Commits

%{all_commits}

%{co_authored_by}

## Before Request Checklist
- [ ] Have you merged into the source branch and resolved all conflicts?
- [ ] Does the pipeline pass?

## Reviewer Checklist
- Code complies with Task DoD
    - [ ] Unit, integration and cucumber tests have been added where applicable
    - [ ] [Manual Testing](https://eng-git.canterbury.ac.nz/seng302-2025/team-200/-/wikis/Manual%20Testing) is complete and covers all testable ACs
    - [ ] Code is readable and has no smells
    - [ ] Docstrings added to public methods and JavaScript functions and is meaningful
    - [ ] TODOs removed
    - [ ] No commented out code
    - [ ] Meets all relevant NFRs:
       - NFR 1. There must be an appropriate amount of sensical data including user accounts to show all
       functionality works, i.e. if an AC requires 10 or more items for pagination, then there must
       be more than 10 items to show the pagination feature works.
       - NFR 2. The product must maintain a consistent and accessible look and feel. Particularly,
       1. Colours and fonts must stay consistent across pages, including colours of buttons.
       2. The app must be responsive to different screen sizes – mobile to desktop.
       3. The app must offer a consistent user experience in terms of interactions with menus,
       buttons, links, or input fields.
       - NFR 3. The product must be both user friendly and fool-proof: users must be supported in their
       tasks by explicitly highlighting all errors or fields that are invalid and helping users to correct
       these mistakes. If there are input mistakes entries/work-done must not be cleared (except
       for passwords)
       - NFR 4. The product must accept all valid characters, included accentuated letters such as macrons
       (e.g., Māori, Müller, ...).
       - NFR 5. When interacting with any highlightable element on the page (e.g., text fields, button),
       pressing tab must move the user to the next element in an ordered manner. For example,
       pressing tab move down fields on a form, but does not move the cursor randomly between
       the different inputs.
