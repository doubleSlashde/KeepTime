// Copyright 2019 doubleSlash Net Business GmbH
//
// This file is part of KeepTime.
// KeepTime is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.

package de.doubleslash.app;

import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler logging uncaught exceptions. Gotta Catch 'Em All
 * 
 * @author nmutter
 */
public class DefaultExceptionHandler implements UncaughtExceptionHandler {

   private static final Logger LOG = LoggerFactory.getLogger(DefaultExceptionHandler.class);

   @Override
   public void uncaughtException(final Thread t, final Throwable e) {
      LOG.error("Uncaught exception on thread '{}'.", t, e);
   }

   /**
    * Registers this class as default uncaught exception handler
    */
   public void register() {
      LOG.debug("Registering uncaught exception handler");
      final UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
      if (defaultUncaughtExceptionHandler != null) {
         LOG.warn("Uncaught exception handler was already set ('{}'). Overwritting.", defaultUncaughtExceptionHandler);
      }
      Thread.setDefaultUncaughtExceptionHandler(this);
   }

}
