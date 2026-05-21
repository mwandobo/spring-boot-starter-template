STARTER TEMPLATE

Installation 

    you can use this to start your own project by running .sh command and giving some parameters
    while in root directory to rename it to your desired project name by just running a command

    .\setup.sh <<package_name eg com.mycompany.myapp>> <<MainClassName ag MyApp>>

to add feature 
Remember to use camelCase incase of naming eg assetCategory

    ./scripts-to-run/add-feature.sh --name department --plural s
    ./scripts-to-run/add-feature.sh --name position --plural s --parent administration

    feature name can be as follow
        normal name like
            user
        other names can be like 
            newUser
            new_user
            new user
    parent name (where a feature can reside)
        normal name like
            management
        other names can be like 
            newManagement
            new_management
            new management

so far we have handled s es ies so far
  

to add property (normal)

    ./scripts-to-run/add-simple-property.sh --feature department --name code --type String --mandatory true


    # Simple
    ./scripts-to-run/add-simple-property.sh --feature department --name code --type String
    
    # With complex names
    ./scripts-to-run/add-simple-property.sh --feature newDepartment --name basicSalary --type BigDecimal
    
    # With parent
    ./scripts-to-run/add-simple-property.sh --feature position --name monthlySalary --type BigDecimal --parent administration
    
    # With spaces
    ./scripts-to-run/add-simple-property.sh --feature "new department" --name dateOfJoining --type LocalDate --parent hr



to add property (foreign key)

    ./scripts-to-run/add-foreign-property.sh --feature position --name department_id --type Long --mandatory true --reference department --parent administration

to remove a feature

    ./scripts-to-run/remove-feature.sh --feature position --parent administration

to remove a property

    ./scripts-to-run/remove-property.sh --feature department --name code --parent administration

to remove a foreign property

    ./scripts-to-run/remove-foreign-property.sh --feature position --name department_id --reference department --parent administration


to log just import @Slf4j

    then log as below
    log.info("Creating user...");
    log.debug("Debug details here");
    log.warn("Something might be wrong");
    log.error("Something went wrong");
    log.info("User ID: {}", userId);
    log.info("Payload: {}", new ObjectMapper().writeValueAsString(payload));


    to run a gradle project 
    ./gradlew bootRun






















