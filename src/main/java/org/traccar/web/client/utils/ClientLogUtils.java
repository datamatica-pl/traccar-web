/*
 * Copyright 2018 Datamatica
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.web.client.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jan Usarek
 */
public class ClientLogUtils {

    private final static String GENERAL_LOG_NAME = "generalClientLogger";
    
    public static void logExceptionGwtCompatible(Level logLevel, Throwable e) {
        String exceptionString = GENERAL_LOG_NAME + ": ";
        exceptionString += e.toString() + "\n";
        Throwable cause = e.getCause();
        while (cause != null) {
            exceptionString += cause.toString() + "\n";
            cause = cause.getCause();
        }
        exceptionString += GENERAL_LOG_NAME + ": end of trace";
        
        Logger logger = Logger.getLogger(GENERAL_LOG_NAME);
        logger.log(logLevel, exceptionString);
    }
    
    public static void logSevere(String message) {
        Logger logger = Logger.getLogger(GENERAL_LOG_NAME);
        logger.log(Level.SEVERE, message);
    }

}
