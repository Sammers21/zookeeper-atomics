/*
 * Copyright 2017 Pavel Drankov.
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

package com.github.sammers21;

import org.apache.log4j.Logger;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Represents a set of variables located in subZNodes of some ZNode
 * <p>
 * For example: we have a route to a zNode "hello/vars". Creating {@link ZkVariables} instance via
 * {@code new ZkVariables(hello/vars", String)} we will assume that all of our variables will be ...
 * stored in children zNodes of zNode located at "hello/vars".
 */
public class ZkVariables {

    private static final Logger log = Logger.getLogger(ZkVariables.class);

    // route to the parent zNode
    public String zNodeName;
    // comma separated host:port pairs, each corresponding to a zk server
    private String zkConnectString;
    // zk client
    private ZooKeeper zooKeeper;

    /**
     * @param zNodeName       name of node which children will store variables
     * @param zkConnectString comma separated host:port pairs, each corresponding to a zk
     *                        server. e.g. "127.0.0.1:3000,127.0.0.1:3001,127.0.0.1:3002"
     */
    public ZkVariables(String zNodeName, String zkConnectString) {
        this.zNodeName = zNodeName;
        this.zkConnectString = zkConnectString;
        connectToZk();
    }

    /**
     * Connecting to Zookeeper in async way
     */
    private void connectToZk() {
        CountDownLatch connectionLatch = new CountDownLatch(1);
        try {
            zooKeeper = new ZooKeeper(zkConnectString, 2000, we -> {
                if (we.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    connectionLatch.countDown();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            connectionLatch.await();
            log.info("Connected to Zookeeper");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * Get variable from ZooKeeper by name
     *
     * @param variableName a name of variable
     * @return a reference to the variable and null if not exists
     */
    public ZkAtomicVar get(String variableName) {

        // check for existanse
        if (!isVarExists(variableName)) {
            return null;
        }

        ZkAtomicVar zkAtomicVar = new ZkAtomicVar(variableName, zooKeeper, this);
        zkAtomicVar.update();

        return zkAtomicVar;
    }

    /**
     * Check variable for existence
     *
     * @param variableName a name of variable
     * @return is variable exists?
     */
    public boolean isVarExists(String variableName) {
        Stat exists = null;
        try {
            exists = zooKeeper.exists(zNodeName + "/" + variableName, null);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            return exists != null;
        }
    }

    /**
     * Trying to get variable from zookeeper of create if such variable is not exists
     *
     * @param variableName  a name of variable
     * @param variableValue a value for a variable if such variable is not exists
     * @return a reference to the variable
     */
    public ZkAtomicVar getOrCreate(String variableName, String variableValue) {

        ZkAtomicVar zkAtomicVar = get(variableName);

        while (zkAtomicVar == null) {
            create(variableName, variableValue);
            zkAtomicVar = get(variableName);
        }

        return zkAtomicVar;
    }

    /**
     * See {@link ZkVariables#create(String, byte[])} for details
     */
    public boolean create(String variableName, String variableValue) {
        return create(variableName, variableValue.getBytes());
    }

    /**
     * Create a variable
     *
     * @param variableName  a name of the variable
     * @param variableValue a value of the variable
     * @return true if variable was created and false if it was existed before
     */
    public boolean create(String variableName, byte[] variableValue) {

        try {
            zooKeeper.create(
                    zNodeName + "/" + variableName,
                    variableValue,
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT
            );
        } catch (KeeperException.NodeExistsException e) {
            return false;
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Killing all threads the object manipulating and marking ZooKeeper session as invalid
     */
    public void close() {
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
