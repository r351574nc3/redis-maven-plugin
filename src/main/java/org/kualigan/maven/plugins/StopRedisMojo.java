/*
 * Copyright 2001-2005 The Apache Software Foundation.
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
package org.kualigan.maven.plugins;

import io.netty.util.concurrent.DefaultEventExecutorGroup;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;


/**
 * Handles the stop goal that is typically used with any forked execution.
 *
 * @author Leo Przybylski
 */
@Mojo(name = "stop", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST, requiresProject = false)
public class StopRedisMojo  extends AbstractMojo {
    
    @Override
    @SuppressWarnings("unchecked")
    public void execute() throws MojoExecutionException, MojoFailureException {

        DefaultEventExecutorGroup group = (DefaultEventExecutorGroup) getPluginContext().get(StartRedisMojo.REDIS_GROUP_CONTEXT_PROPERTY_NAME);
        
        if(group == null) {
            throw new MojoExecutionException("Redis server is not running");
        }

        getLog().info("Shutting down Redis server...");
        group.shutdownGracefully();
    }
}