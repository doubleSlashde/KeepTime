package application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.fxml.FXML;

@Component
public class UIController {

   @Autowired
   private ArticleRepository articleRepository;

   // constructor injection also works
   // code here to use the injected articleRepository
   @FXML
   private void initialize() {
      final Article article = new Article();
      article.content = "Hey you";
      System.out.println("HASDASD " + articleRepository.count());
      articleRepository.saveAndFlush(article);

      System.out.println("HASDASD " + articleRepository.count());
   }
}