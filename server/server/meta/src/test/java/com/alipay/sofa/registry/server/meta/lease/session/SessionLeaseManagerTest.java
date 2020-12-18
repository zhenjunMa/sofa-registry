/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.registry.server.meta.lease.session;

import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.lease.Lease;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class SessionLeaseManagerTest extends AbstractTest {

    private SessionLeaseManager manager = new SessionLeaseManager();

    @Test
    public void testIsLeader() throws InterruptedException {
        int leaseNum = 1000;
        for (int i = 0; i < leaseNum; i++) {
            manager.register(new Lease<SessionNode>(
                new SessionNode(randomURL(randomIp()), getDc()), 1));
        }
        Thread.sleep(1010);
        int size = manager.getClusterMembers().size();
        Assert.assertEquals(size, manager.getExpiredLeases().size());
        manager.isLeader();
        Thread.sleep(20);
        Assert.assertEquals(0, manager.getExpiredLeases().size());
    }

    @Test
    public void testCopyMySelf() {
        assertNotNull(manager.copyMySelf());
    }

    @Test
    public void testSnapshotProcess() {
        new ConcurrentExecutor(1000, executors)
                .execute(()->manager.register(new Lease<SessionNode>(new SessionNode(randomURL(randomIp()), getDc()), 1)));
        manager.copy().save(SessionLeaseManager.SESSION_LEASE_MANAGER);
        SessionLeaseManager loadManager = new SessionLeaseManager();
        loadManager.load(SessionLeaseManager.SESSION_LEASE_MANAGER);
        Assert.assertEquals(manager.getClusterMembers().size(), loadManager.getClusterMembers().size());
    }
}