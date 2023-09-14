package de.doubleslash.keeptime.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "AUTHORITIES")
public class Authorities {

   @Id
   @Column(name = "USERNAME")
   private String userName;

   @Column(name = "AUTHORITY")
   private String authority;
}
