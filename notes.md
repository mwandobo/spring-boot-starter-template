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

to add feature in the project run command
    ./scripts-to-run/add-feature.sh --name <feature-name> --plural <prural-indicator>
    forexample
    ./scripts-to-run/add-feature.sh --name department --plural s
    ./scripts-to-run/add-feature.sh --name position --plural s --parent administration

so far we have handled s es ies so far
  

to add property (normal)
        ./scripts-to-run/add-property.sh --feature department --name code --type String --mandatory true
to add property (foreign key)
        ./scripts-to-run/add-property.sh --feature position --name department_id --type Long --mandatory true --reference department --parent administration

./add-property.sh department status String true

import com.bonnysimon.starter.features.approval.entity.ApprovalAction;
import com.bonnysimon.starter.features.approval.entity.ApprovalLevel;
import com.bonnysimon.starter.features.approval.entity.SysApproval;
import com.bonnysimon.starter.features.approval.entity.UserApproval;
import com.bonnysimon.starter.features.approval.enums.ApprovalActionEnum;
import com.bonnysimon.starter.features.approval.repository.ApprovalActionRepository;
import com.bonnysimon.starter.features.approval.repository.ApprovalLevelRepository;
import com.bonnysimon.starter.features.approval.repository.SysApprovalRepository;
import com.bonnysimon.starter.features.approval.repository.UserApprovalRepository;


