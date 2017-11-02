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
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class ZkAtomicVar {

    private static final Logger log = Logger.getLogger(ZkAtomicVar.class);

    private String name;
    private byte[] value;
    private ZooKeeper zooKeeper;
    private ZkVariables zkVariables;
    private Stat stat = new Stat();
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


    public void changeValueTo(String val) {
        changeValueTo(val.getBytes());
    }

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

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public String getAsString() {
        return new String(getValue());
    }

//    boolean compareAndSet(String expect, String update) {
//
//    }

    protected ZkAtomicVar(String name, ZooKeeper zooKeeper, ZkVariables zkVariables) {
        this.name = name;
        this.zooKeeper = zooKeeper;
        this.zkVariables = zkVariables;
    }

    protected Watcher watcher() {
        return event -> {
//            if (event.getType() == Watcher.Event.EventType.NodeDataChanged) {
//                String path = event.getPath();
//                log.info("data changed at " + path);
//                update(path);
//                log.info("current value of variable " + name + " = " + getAsString());
//            }
        };
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
                        watcher(),
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
