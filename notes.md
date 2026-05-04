This is the stater template

you can use this to start your own project by running .sh command and giving some parameters

while in root directory to rename it to your desired project name you just run a command

.\setup.sh <<package_name eg com.mycompany.myapp>> <<MainClassName ag MyApp>>


to log just import @Slf4j
then log as below
  log.info("Creating user...");
        log.debug("Debug details here");
        log.warn("Something might be wrong");
        log.error("Something went wrong");
        log.info("User ID: {}", userId);
        log.info("Payload: {}", new ObjectMapper().writeValueAsString(payload));


in the project run commands below
To add Features


to add feature 

    ./scripts-to-run/add-feature.sh --name department --plural s
    ./scripts-to-run/add-feature.sh --name position --plural s --parent administration

so far we have handled s es ies so far
  

to add property (normal)

    ./scripts-to-run/add-simple-property.sh --feature department --name code --type String --mandatory true

to add property (foreign key)

    ./scripts-to-run/add-foreign-property.sh --feature position --name department_id --type Long --mandatory true --reference department --parent administration

to remove a feature

    ./scripts-to-run/remove-feature.sh --feature position --parent administration
