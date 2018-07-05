package application;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Article {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(nullable = false, columnDefinition = "BIGINT")
   private Long id;

   @Column(nullable = false)
   public String content;

   // getter and setters omitted
}
