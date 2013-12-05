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
 * Goal which touches a timestamp file.
 *
 */
@Mojo(name = "start", defaultPhase = LifecyclePhase.NONE, requiresProject = false)
public class StartRedisMojo extends AbstractMojo {
    public static final String REDIS_GROUP_CONTEXT_PROPERTY_NAME = StartRedisMojo.class.getName()
        + File.pathSeparator + "redis";

    /**
     * Redis server port number.
     */
    @Parameter( defaultValue = "${redis.port}", property = "port", required = false )
    private int port;

    public void execute()
        throws MojoExecutionException {
        try {
            start();
        }
        catch (Exception e) {
            throw new MojoExecutionException("Unable to start redis server", e);
        }
    }

    /**
     * Start the redis server
     *
     */
    public void start() throws InterruptedException {
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

            // Start the server.
            getLog().info("Starting Redis(port=" + port + ") server...");
            
            final ChannelFuture f = b.bind();

            // Add non-forked version later
            // Wait until the server socket is closed.
            // f.sync();
            // f.channel().closeFuture().sync();
        } finally {
            // Shut down all event loops to terminate all threads.
            // group.shutdownGracefully();
        }
    }

    public void setPort(final int port) {
        this.port = port;
    }
    
    public int getPort() {
        return port;
    }
}

