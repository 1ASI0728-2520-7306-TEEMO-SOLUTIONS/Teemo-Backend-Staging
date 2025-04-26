package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.valueobjects.Roles;


import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@With
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Roles name;

    public Role(Roles name) { this.name = name;}

    public String getStringName() { return name.name(); }

    public static Role getDefaultaRole() { return new Role(Roles.ROLE_USER);}

    public static Role toRoleFromName(String name) {
        return new Role(Roles.valueOf(name));
    }

    public static List<Role> validateRoleSet(List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of(getDefaultaRole());
        }
        return  roles;
    }
}