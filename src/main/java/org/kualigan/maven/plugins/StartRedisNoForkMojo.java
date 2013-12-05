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

import redis.server.netty.RedisCommandDecoder;
import redis.server.netty.RedisCommandHandler;
import redis.server.netty.RedisReplyEncoder;
import redis.server.netty.SimpleRedisServer;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


/**
 * Start an in-memory redis server wherever want and whenever you want. This is never forked, so it doesn't obey the <code>redis.forked</code> property.
 *
 * @author Leo Przybylski
 */
@Mojo(name = "start-no-fork", defaultPhase = LifecyclePhase.NONE, requiresProject = false)
public class StartRedisNoForkMojo extends AbstractStartRedisMojo {

    /**
     * Redis server port number.
     */
    @Parameter(property = "redis.port", defaultValue = "6379")
    private int port;

    public void execute()
        throws MojoExecutionException {
        try {
            start(false);
        }
        catch (Exception e) {
            throw new MojoExecutionException("Unable to start redis server", e);
        }
    }

    public void setPort(final int port) {
        this.port = port;
    }
    
    public int getPort() {
        return port;
    }
}

