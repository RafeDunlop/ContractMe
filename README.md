## How to run
### 1 - Setting up environment variables
On our [wiki](https://eng-git.canterbury.ac.nz/seng302-2025/team-200/-/wikis/Home), 
find the credentials for our Spring mail service. These variables must be defined before your run the command in
section 2. How you define them is unimportant. however, if you are using intelliJ one way is by editing your run configuration,
run > edit configurations > HomeHelperApplication > modify options > environment variables and entering the variables
using key=pair values seperated by semicolons (;)

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
> You may want to include information here about how to use the application, notably:
> - default user credentials if required
> - link to home/login page
> - disclosing known issues (and workarounds if applicable)
> - ...

## How to run tests
> Once you have some tests written make sure you detail how to run them, especially if there are special requirements.

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


## Todo (Sprint 2)

- Update team name into `build.gradle`
- Set up Gitlab CI server (refer to the student guide on Scrumboard)
- Decide on a LICENSE

## Third-Party Software

This project incorporates code from the project [profanity-filter](https://github.com/modernmt/profanity-filter?tab=Apache-2.0-1-ov-file)
This covers the following directories:

src/main/java/profanityFilter
src/resources/profanityFilterResources

These directories contain both original and modified code from this project.
This project includes modifications to files originally licensed under the Apache License 2.0.
Changes were made by [Jack Guard] and [Mason Ott] on [09/05/2025].

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

## References

- [Spring Boot Docs](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Spring JPA docs](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Thymeleaf Docs](https://www.thymeleaf.org/documentation.html)
- [Learn resources](https://learn.canterbury.ac.nz/course/view.php?id=17797&section=8)
