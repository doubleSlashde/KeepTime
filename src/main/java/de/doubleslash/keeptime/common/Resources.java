package de.doubleslash.keeptime.common;

import java.net.URL;

public class Resources {

   private Resources() {
      throw new IllegalStateException("Utility class");
   }

   public enum RESOURCE {
      /** FONTS **/
	  FONT_BOLD("/font/OpenSans-Bold.ttf"),
      FONT_SEMI_BOLD("/font/OpenSans-SemiBold.ttf"),
      FONT_REGULAR("/font/OpenSans-Regular.ttf"),
	   
      /** LAYOUTS **/
      // main
      FXML_VIEW_LAYOUT("/layouts/ViewLayout.fxml"),
      FXML_PROJECT_LAYOUT("/layouts/ProjectDetailLayout.fxml"),
      FXML_SETTINGS("/layouts/settings.fxml"),
      FXML_VIEW_POPUP_LAYOUT("/layouts/ViewLayoutPopup.fxml"),
      FXML_REPORT("/layouts/report.fxml"),

      // icon
      ICON_MAIN("/icons/icon.png"),

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
      return Resources.class.getResource(resource.getResourceLocation());
   }
}
