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


