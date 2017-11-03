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
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/**
 * Representation of variable located at some zNode : {@code path()}
 */
public class ZkAtomicVar {

    private static final Logger log = Logger.getLogger(ZkAtomicVar.class);

    // variable name
    private String name;
    // value of variable
    private byte[] value;
    // link to a zookeeper client
    private ZooKeeper zooKeeper;
    // link to the zKVariables which hold namespace info
    private ZkVariables zkVariables;
    // information about zNode where the variable is stored
    private Stat stat = new Stat();
    // synchronization lock
    private final Object lock = new Object();

    /**
     * @return current variable value
     */
    private byte[] getValue() {
        update();
        return value;

    }

    /**
     * @param value new value of variable
     */
    private void setValue(byte[] value) {
        this.value = value;
    }

    /**
     * Change value of variable to a given
     *
     * @param val given new value
     */
    public void changeValueTo(String val) {
        changeValueTo(val.getBytes());
    }

    /**
     * Change value of variable to a given
     *
     * @param val given new value
     */
    public void changeValueTo(byte[] val) {
        boolean retry = true;
        while (retry) {
            try {
                synchronized (lock) {
                    zooKeeper.setData(path(), val, stat.getVersion());
                    retry = false;
                    setValue(val);
                }
            } catch (KeeperException e) {
                update();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @return string representation of data the variable hold
     */
    public String getAsString() {
        return new String(getAsByteArr());
    }

    /**
     * @return Data the variable hold
     */
    public byte[] getAsByteArr() {
        return getValue();
    }

    protected ZkAtomicVar(String name, ZooKeeper zooKeeper, ZkVariables zkVariables) {
        this.name = name;
        this.zooKeeper = zooKeeper;
        this.zkVariables = zkVariables;
    }

    /**
     * Force variable to check for updates
     *
     * @param path variable path
     */
    private void update(String path) {
        try {
            synchronized (lock) {
                byte[] data = zooKeeper.getData(
                        path,
                        null,
                        stat);
                setValue(data);
            }
        } catch (KeeperException | InterruptedException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Force variable to check for updates
     */
    public void update() {
        update(path());
    }

    @Override
    public String toString() {
        return getAsString();
    }

    /**
     * @return zNode variable path
     */
    private String path() {
        return zkVariables.zNodeName + "/" + name;
    }

}
