package application.common;

import java.net.URL;

public class Resources {

   public enum RESOURCE {
      /** LAYOUTS **/
      // main
      FXML_VIEW_LAYOUT("ViewLayout.fxml"),
      FXML_PROJECT_LAYOUT("ProjectDetailLayout.fxml"),

      // icon
      ICON_MAIN("icon.png"),

      ;
      String resourceLocation;

      private RESOURCE(final String resourceLocation) {
         this.resourceLocation = resourceLocation;
      }

      public String getResourceLocation() {
         return resourceLocation;
      }
   }

   public static URL getResource(final RESOURCE resource) {
      return ClassLoader.getSystemResource(resource.getResourceLocation());
   }
}
