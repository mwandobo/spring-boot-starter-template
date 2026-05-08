STARTER TEMPLATE

Installation 

    you can use this to start your own project by running .sh command and giving some parameters
    while in root directory to rename it to your desired project name by just running a command

    .\setup.sh <<package_name eg com.mycompany.myapp>> <<MainClassName ag MyApp>>

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


in the project run commands below
To add Features



















package com.bonnysimon.starter.features.user.repository;

import com.bonnysimon.starter.features.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Page<User> findAll(Specification<User> spec, Pageable pageable);

    List<User> findByRoleId(Long id);

    List<User> findByRoleIdIn(List<Long> roleIds);
}



















package com.bonnysimon.starter.features.user.model;
import com.bonnysimon.starter.core.entity.BaseEntity;
import com.bonnysimon.starter.features.role.Role;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User extends BaseEntity {
private String name;
private String email;

    @Column(name = "phone", nullable = true)
    private String phone;
    
    private String password;
    private String otp;
    private Boolean isOtpVerified = false;
    private Boolean isRecoveryRequested = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;
}






















