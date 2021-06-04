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
package com.alipay.sofa.registry.test.client;

import com.alipay.sofa.registry.client.api.registration.PublisherRegistration;
import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.server.meta.resource.model.ClientOffAddressModel;
import com.alipay.sofa.registry.test.BaseIntegrationTest;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author xiaojian.xj
 * @version $Id: ClientManagerTest.java, v 0.1 2021年05月31日 11:40 xiaojian.xj Exp $
 */
@RunWith(SpringRunner.class)
public class ClientManagerTest extends BaseIntegrationTest {

  private String localAddress = sessionChannel.getLocalAddress().getHostString();
  private final String CLIENT_OFF_STR = "1.1.1.1;2.2.2.2;" + localAddress;
  private final String CLIENT_OPEN_STR = "2.2.2.2;3.3.3.3;" + localAddress;

  private final Set<String> CLIENT_OFF_SET = Sets.newHashSet(CLIENT_OFF_STR.split(";"));
  private final Set<String> CLIENT_OPEN_SET = Sets.newHashSet(CLIENT_OPEN_STR.split(";"));

  @Test
  public void testClientOff() throws InterruptedException, TimeoutException {
    String dataId = "test-meta-client-off-dataId-" + System.currentTimeMillis();
    String value = "test meta client off";

    DataInfo dataInfo =
        new DataInfo(ValueConstants.DEFAULT_INSTANCE_ID, dataId, ValueConstants.DEFAULT_GROUP);

    /** register */
    PublisherRegistration registration = new PublisherRegistration(dataId);
    com.alipay.sofa.registry.client.api.Publisher register =
        registryClient1.register(registration, value);
    Thread.sleep(2000L);

    // check session
    Assert.assertTrue(isExist(sessionDataStore.getDatas(dataInfo.getDataInfoId()), localAddress));

    // check data
    Assert.assertTrue(
        isExist(localDatumStorage.getAllPublisher().get(dataInfo.getDataInfoId()), localAddress));

    /** client off */
    CommonResponse response = clientManagerResource.clientOff(CLIENT_OFF_STR);
    Assert.assertTrue(response.isSuccess());

    // check session client off list
    waitConditionUntilTimeOut(
        () -> fetchClientOffAddressService.getClientOffAddress().equals(CLIENT_OFF_SET), 5000);

    register.republish(value);
    Thread.sleep(2000L);

    // check session local cache
    Assert.assertFalse(isExist(sessionDataStore.getDatas(dataInfo.getDataInfoId()), localAddress));
    // check data publisher
    Assert.assertFalse(
        isExist(localDatumStorage.getAllPublisher().get(dataInfo.getDataInfoId()), localAddress));

    /** client open */
    response = clientManagerResource.clientOpen(CLIENT_OPEN_STR);
    Assert.assertTrue(response.isSuccess());

    SetView<String> difference = Sets.difference(CLIENT_OFF_SET, CLIENT_OPEN_SET);
    waitConditionUntilTimeOut(
        () -> fetchClientOffAddressService.getClientOffAddress().equals(difference), 5000);

    GenericResponse<ClientOffAddressModel> query = clientManagerResource.query();
    Assert.assertTrue(query.isSuccess());
    Assert.assertEquals(
        query.getData().getIps(), fetchClientOffAddressService.getClientOffAddress());

    Thread.sleep(3000);

    // check session local cache
    Assert.assertTrue(isExist(sessionDataStore.getDatas(dataInfo.getDataInfoId()), localAddress));
    Assert.assertTrue(
        isExist(localDatumStorage.getAllPublisher().get(dataInfo.getDataInfoId()), localAddress));
  }
}
