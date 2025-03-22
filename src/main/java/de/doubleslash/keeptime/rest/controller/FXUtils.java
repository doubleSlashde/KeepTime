package de.doubleslash.keeptime.rest.controller;

import javafx.application.Platform;

import java.util.concurrent.CompletableFuture;

public class FXUtils {
   public static void runInFxThreadAndWait(Runnable runnable) {
      CompletableFuture<Void> future = new CompletableFuture<>();

      Platform.runLater(() -> {
         try {
            runnable.run();
            future.complete(null);
         } catch (Exception e) {
            future.completeExceptionally(e);
         }
      });

      // Wait for the result (blocking for simplicity; adjust as needed for async handling)
      try {
         future.get(); // This blocks until the CompletableFuture is completed
      } catch (Exception e) {
         throw new RuntimeException("Error processing request", e);
      }
   }
}
