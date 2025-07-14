## Description
Welcome to Home Helper! Home Helper makes organising your renovations easy.
You will be able to create an account, create and manage your renovations,
add tasks to keep track of what needs doing, and browse public renovations.

## Dependencies
- Java >= 17, get the latest openjdk build from [jdk.java.net](https://jdk.java.net/)
or preferably your package manager. You will probably need the full JDK to use
Gradle.
- [Spring Boot](https://spring.io/projects/spring-boot) (including spring boot
mail)
- [Cucumber](https://cucumber.io/) for automated acceptance tests
- [JUnit 5/Jupiter](https://junit.org/junit5/) for automated unit/integration
tests
- [Geoapify](https://www.geoapify.com/) see below for info about API keys

All dependencies, apart from the Java runtime/jdk itself, are managed via
gradle, by following the steps below to run the application locally, all
required dependencies should be pulled in for you.

## How to run
### 1 - Setting up environment variables
If required, contact a maintainer (see contributors below) to gain access
to environment variables such as API keys, otherwise create your own API keys
and an email account and create an `env.properties` file:

```
# env.properties

DB_PASSWORD=password
DB_USERNAME=sa
SPRING_MAIL_PASSWORD=<your email password>
SPRING_MAIL_USERNAME=<your email address>
GEOAPIFY_API_KEY=<your geoapify key>
```
See [Geoapify](https://www.geoapify.com/) for info on the API and how to create
a key.
Note that currently we only support Gmail with the default configuration, but
[application.properties](./src/main/resources/application.properties)
can be modified with email settings
for other email servers.

### 2 - Running the project
From the root directory ...

On Linux:
```
./gradlew bootRun
```

On Windows:
```
gradlew bootRun
```

By default, the application will run on local port 8080 [http://localhost:8080](http://localhost:8080)

### 3 - Using the application
See our deployed production server [here](https://csse-seng302-team200.canterbury.ac.nz/prod/)
or our deployed staging server [here](https://csse-seng302-team200.canterbury.ac.nz/test/).
Note that the staging server is in development and should not be considered
stable.

Known issues are tracked [on GitLab](https://eng-git.canterbury.ac.nz/seng302-2025/team-200/-/issues).
## Default user accounts
The table below has the credentials for default users which can be used for
testing.

| Account Type  | Email                             | Password | Notes                                                                                                                                                                                                                                                                                                       |
|---------------|-----------------------------------|----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| User          | seng302.team200.test@gmail.com    | P4$$word | To test __task pagination__, search for renovation  ["Jack Erskine revamp" on the test server](https://csse-seng302-team200.canterbury.ac.nz/test/renovations/view?id=1) or ["Production Test Record" on the production server](https://csse-seng302-team200.canterbury.ac.nz/prod/renovations/view?id=64)  |
| User          | seng302.team200.test1@gmail.com   | P4$$word | This user has (a renovation with) only 1 task                                                                                                                                                                                                                                                               |

## How to run tests
Ensure that your environment variables are set up correctly to run the tests
in your local environment.

On Linux:
```
./gradlew test
./gradlew integration
./gradlew cucumber
```

On Windows:

```
gradlew test
gradlew integration
gradlew cucumber
```

## Third-Party Software

This project incorporates code from the project [profanity-filter](https://github.com/modernmt/profanity-filter?tab=Apache-2.0-1-ov-file)
This covers the following directories:

src/main/java/profanityFilter
src/resources/profanityFilterResources

These directories contain both original and modified code from this project.
This project includes modifications to files originally licensed under the Apache License 2.0.
Changes were made by [Jack Guard] and [Mason Ott] on [09/05/2025].
Changes were made by [Ryan Hamilton] on [20/05/2025].

Again, the URI for this repository can be found at [profanity-filter](https://github.com/modernmt/profanity-filter?tab=Apache-2.0-1-ov-file)

The license for this project is found at [license](https://github.com/modernmt/profanity-filter/blob/main/LICENSE)
The terms for the license under which this third party software has been used can be found at [Apache-2.0.](https://www.apache.org/licenses/LICENSE-2.0)

## Contributors

- SENG302 teaching team
- Rafe Dunlop
- Abhisekh Chand
- Jack Guard
- Jake Connolly
- Ryan Hamilton
- Sean Reitsma
- Mason Ott

## License Notice
The license for Home Helper can be found at [COPYING](./COPYING)

Home Helper, a web application for managing renovations
Copyright (C) 2025 SENG302 Team 200

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.

## References

- [Spring Boot Docs](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Spring JPA docs](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Thymeleaf Docs](https://www.thymeleaf.org/documentation.html)
- [Learn resources](https://learn.canterbury.ac.nz/course/view.php?id=17797&section=8)
