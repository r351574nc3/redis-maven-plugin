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
 * Abstract class for different types of redis startup executions. Common functionality exists here. See other 
 * Mojos for the types of executions available.
 *
 * @author Leo Przybylski
 */
public abstract class AbstractStartRedisMojo extends AbstractMojo {
    public static final String REDIS_GROUP_CONTEXT_PROPERTY_NAME = StartRedisMojo.class.getName()
        + File.pathSeparator + "redis";

    /**
     * Main mojo execution loop. Implementing classes should override this for the template method pattern.
     */
    public abstract void execute() throws MojoExecutionException;

    /**
     * Start the redis server
     *
     * @param isForked is a {@link Boolean} determing whether to fork the redis server or not.
     */
    public void start(final Boolean isForked) throws InterruptedException {
        // Only execute the command handler in a single thread
        final RedisCommandHandler commandHandler = new RedisCommandHandler(new SimpleRedisServer());


        // Configure the server.
        final ServerBootstrap b = new ServerBootstrap();
        final DefaultEventExecutorGroup group = new DefaultEventExecutorGroup(1);
        getPluginContext().put(REDIS_GROUP_CONTEXT_PROPERTY_NAME, group);

        try {
            b.group(new NioEventLoopGroup(), new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 100)
                .localAddress(getPort())
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            final ChannelPipeline p = ch.pipeline();
                            p.addLast(new RedisCommandDecoder());
                            p.addLast(new RedisReplyEncoder());
                            p.addLast(group, commandHandler);
                        }
                    });

            final StringBuffer message = new StringBuffer();

            // Start the server.
            if (isForked) {
                message.append("Forking Redis");
            }
            else {
                message.append("Starting Redis");
            }
            
            message.append("(port=").append(getPort()).append(") server...");
            getLog().info(message.toString());
            
            final ChannelFuture f = b.bind();

            // Wait until the server socket is closed.
            if (!isForked) {
                f.sync();
                f.channel().closeFuture().sync();
            }
        } finally {
            // Shut down all event loops to terminate all threads.
            group.shutdownGracefully();
        }
    }

    public abstract void setPort(final int port);
    
    public abstract int getPort();
}

