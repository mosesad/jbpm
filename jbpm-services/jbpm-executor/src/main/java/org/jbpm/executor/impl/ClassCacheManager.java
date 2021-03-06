/*
 * Copyright 2013 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.executor.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.kie.internal.executor.api.Command;
import org.kie.internal.executor.api.CommandCallback;
import org.kie.internal.executor.api.CommandContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple cache to keep classes of commands and callback to not attempt to load them every time.
 *
 */
@ApplicationScoped
public class ClassCacheManager {
    
    private static final Logger logger = LoggerFactory.getLogger(ClassCacheManager.class);
    
    private final Map<String, Command> commandCache = new HashMap<String, Command>();
    private final Map<String, CommandCallback> callbackCache = new HashMap<String, CommandCallback>();  

    /**
     * Finds command by FQCN and if not found loads the class and store the <code>Class</code> instance in
     * the cache.
     * @param name - fully qualified class name of the command
     * @return initialized class instance
     */
    public Command findCommand(String name) {
        synchronized (commandCache) {
            
                if (!commandCache.containsKey(name)) {
                    try {
                        Command commandInstance = (Command) Class.forName(name).newInstance();
                        commandCache.put(name, commandInstance);
                    } catch (Exception ex) {
                        throw new IllegalArgumentException("Unknown Command implemenation with name '" + name + "'");
                    }

                }

       
        }
        return commandCache.get(name);
    }

    /**
     * Finds command callback by FQCN and if not found loads the class and store the <code>Class</code> instance in
     * the cache.
     * @param name - fully qualified class name of the command callback
     * @return initialized class instance
     */
    public CommandCallback findCommandCallback(String name) {
        synchronized (callbackCache) {
            
                    if (!callbackCache.containsKey(name)) {
                        try {
                            CommandCallback commandCallbackInstance = (CommandCallback) Class.forName(name).newInstance();
                            callbackCache.put(name, commandCallbackInstance);
                        } catch (Exception ex) {
                            throw new IllegalArgumentException("Unknown Command implemenation with name '" + name + "'");
                        }

                    }

        }
        return callbackCache.get(name);
    }

    /**
     * Builds completely initialized list of callbacks for given context.
     * @param ctx contextual data given by execution service
     * @return
     */
    public List<CommandCallback> buildCommandCallback(CommandContext ctx) {
        List<CommandCallback> callbackList = new ArrayList<CommandCallback>();
        if (ctx != null && ctx.getData("callbacks") != null) {
            logger.debug("Callback: {}", ctx.getData("callbacks"));
            String[] callbacksArray = ((String) ctx.getData("callbacks")).split(",");;
            List<String> callbacks = (List<String>) Arrays.asList(callbacksArray);
            for (String callbackName : callbacks) {
                CommandCallback handler = findCommandCallback(callbackName);
                callbackList.add(handler);
            }
        }
        return callbackList;
    }

}
