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


import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ZkVarTest extends ZkVarTestBase {

    private static final Logger log = Logger.getLogger(ZkVarTest.class);

    @Test
    public void shouldReturnNullIfVarIsNotExist() {
        ZkAtomicVar var = zkVariables.get("hello");
        assertTrue(var == null);
    }

    @Test
    public void shouldCreateNonExistVar() {
        ZkAtomicVar var = zkVariables.getOrCreate("hello", "How are you?");
        assertTrue(var != null);
        assertTrue(var.getAsString().equals("How are you?"));
    }

    @Test
    public void shouldWorksConcurrently() {
        ZkVariables zkVariablesOneMore = null;
        try {
            zkVariablesOneMore = new ZkVariables(
                    "",
                    "localhost:" + port
            );


            ZkAtomicVar var = this.zkVariables.getOrCreate("hello", "How are you?");
            ZkAtomicVar var1 = zkVariablesOneMore.get("hello");

            assertTrue(var1.getAsString().equals("How are you?"));

            for (int counter = 1; counter < 1_000; counter++) {
                String stForm = String.valueOf(counter);
                var.changeValueTo(stForm);

                String asString = var1.getAsString();
                assertTrue(asString.equals(stForm));
            }

        } finally {
            zkVariablesOneMore.close();
        }
    }


}
