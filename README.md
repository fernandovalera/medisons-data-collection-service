Data Collection Service
=======================

Responsible for receiving patient data from a socket and outputting that data via GraphQL to the Database Manager (DBM).
The DBM makes patient data accessible to other services and is responsible for storing aggregated data as well.

## Running the Simulator

1. Navigate to the 'data-collection' directory.

2. Build the project.

3. Run the following command:
    - Static files:
        > java -cp build/classes/java/main com.medisons.simulator.SensorSimulator [files path]
    
    - Live files:
        > java -Duser.timezone=Canada/Mountain -cp build/classes/java/main com.medisons.simulator.SensorSimulator [files path] live

## Running the DCS

1. Navigate to the 'data-collection' directory.

2. Run the following command based on your OS:
    - Linux:
        > `./gradlew run`
    
    - Windows:
        > `gradlew.bat run`

## DBM Setup

1. Install MySQL Server 8.+ or MySQL Server 5.7.+
    - The root password should match the password in ConnectionManager.
2. Create signals database:
    > `mysql -uroot -e "CREATE DATABASE signals"`
3. Run baseline database migration:
    > `mysql -uroot signals < database-manager/src/main/resources/db/migration/V1__Create_all_tables.sql`
## Running the DBM

Navigate to the 'database-manager' directory.

Linux:
> `./gradlew appRun`

Windows:
> `gradlew.bat appRun`
