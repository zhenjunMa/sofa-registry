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
package com.alipay.sofa.registry.jdbc.repository.impl;

import com.alipay.sofa.registry.jdbc.config.DefaultCommonConfig;
import com.alipay.sofa.registry.jdbc.domain.ProvideDataDomain;
import com.alipay.sofa.registry.jdbc.mapper.ProvideDataMapper;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.meta.ProvideDataRepository;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version $Id: ProvideDataJdbcRepository.java, v 0.1 2021年03月13日 19:20 xiaojian.xj Exp $
 */
public class ProvideDataJdbcRepository implements ProvideDataRepository {

  private static final Logger LOG = LoggerFactory.getLogger("META-PROVIDEDATA", "[ProvideData]");

  @Autowired private ProvideDataMapper provideDataMapper;

  @Autowired private DefaultCommonConfig defaultCommonConfig;

  @Override
  public boolean put(String key, String value) {

    ProvideDataDomain data = new ProvideDataDomain(defaultCommonConfig.getClusterId(), key, value);
    provideDataMapper.save(data);
    if (LOG.isInfoEnabled()) {
      LOG.info(
          "put provideData, dataCenter: {}, key: {}, value: {}",
          defaultCommonConfig.getClusterId(),
          key,
          value);
    }
    return true;
  }

  @Override
  public DBResponse get(String key) {
    ProvideDataDomain data = provideDataMapper.query(defaultCommonConfig.getClusterId(), key);
    if (data == null) {
      return DBResponse.notfound().build();
    }
    return DBResponse.ok(data.getDataValue()).build();
  }

  @Override
  public boolean remove(String key) {
    provideDataMapper.remove(defaultCommonConfig.getClusterId(), key);
    if (LOG.isInfoEnabled()) {
      LOG.info(
          "remove provideData, dataCenter: {}, key: {}", defaultCommonConfig.getClusterId(), key);
    }
    return true;
  }
}