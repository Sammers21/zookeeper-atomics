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

package com.github.Sammers21;

import org.apache.curator.test.TestingServer;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Base class for testing Zookeeper
 */
public class ZkVarTestBase {

    private static final Logger log = Logger.getLogger(ZkVarTestBase.class);

    public TestingServer zkTestServer;
    public ZkVariables zkVariables;
    public int port;

    /**
     * Starting ZooKeeper server
     */
    @Before
    public void startZookeeper() throws Exception {
        logDelmer();
        port = findFreePort();
        zkTestServer = new TestingServer(port);
        log.info("Embedded ZooKeeper server listening on port = " + port);
        zkVariables = new ZkVariables(
                "",
                "localhost:" + port
        );
    }

    /**
     * Stopping ZooKeeper server
     */
    @After
    public void stopZookeeper() throws IOException {
        zkVariables.close();
        zkTestServer.stop();
        logDelmer();
    }

    void logDelmer() {
        log.info("\n---------------------------------------------------------------------------\n");
    }


    /**
     * Returns a free port number on localhost.
     * <p>
     * Heavily inspired from org.eclipse.jdt.launching.SocketUtil (to avoid a dependency to JDT just because of this).
     * Slightly improved with close() missing in JDT. And throws exception instead of returning -1.
     *
     * @return a free port number on localhost
     * @throws IllegalStateException if unable to find a free port
     */
    private static int findFreePort() {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(0);
            socket.setReuseAddress(true);
            int port = socket.getLocalPort();
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore IOException on close()
            }
            return port;
        } catch (IOException e) {
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
        throw new IllegalStateException("Could not find a free TCP/IP port to start embedded Jetty HTTP Server on");
    }
}
