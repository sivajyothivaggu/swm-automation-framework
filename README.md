# SWM Automation Framework

## Overview
Comprehensive test automation framework for SWM application supporting UI, API, and Database testing.

## Tech Stack
- Java 11
- Selenium WebDriver 4.x
- TestNG
- REST Assured
- Maven
- Jenkins

## Project Structure
```
swm-automation-framework/
├── src/main/java/com/swm/
│   ├── core/          # Framework engine
│   ├── ui/            # UI automation layer
│   ├── api/           # API automation layer
│   ├── database/      # Database layer
│   └── testdata/      # Test data management
└── src/test/java/com/swm/tests/
    ├── ui/            # UI tests
    ├── api/           # API tests
    └── integration/   # End-to-end tests
```

## Setup
1. Install Java 11+
2. Install Maven 3.6+
3. Clone repository
4. Run: `mvn clean install`

## Execution
- All tests: `mvn test`
- Specific suite: `mvn test -DsuiteXmlFile=testng.xml`
- Single test: `mvn test -Dtest=LoginTest`

## Reports
Test reports are generated in `reports/` directory.
