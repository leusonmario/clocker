/*
 * Copyright 2014 by Cloudsoft Corporation Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package brooklyn.entity.container.sdn.dove;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import brooklyn.entity.container.sdn.SdnAgentImpl;

/**
 * A single Docker container.
 */
public class DoveAgentImpl extends SdnAgentImpl implements DoveAgent {

    private static final Logger LOG = LoggerFactory.getLogger(DoveAgent.class);

    @Override
    public Class getDriverInterface() {
        return DoveAgentDriver.class;
    }

    @Override
    public String getDmcAddress() {
        return getAttribute(SDN_PROVIDER).getConfig(DoveNetwork.DOVE_CONTROLLER).getHostAddress();
    }

    @Override
    public String getAgentAddress() {
        return getAttribute(SDN_AGENT_ADDRESS).getHostAddress();
    }

}
