package com.example.apps.auths.entities;

import java.util.List;

import com.example.tfs.entities.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roles")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Role extends BaseEntity {

    private String name;

    @ManyToMany(mappedBy = "roles")
    private List<User> user;

}
