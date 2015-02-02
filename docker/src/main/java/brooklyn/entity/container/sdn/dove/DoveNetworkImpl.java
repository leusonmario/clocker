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

import java.net.InetAddress;
import java.util.Collection;
import java.util.Map;

import org.jclouds.net.domain.IpPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import brooklyn.entity.Entity;
import brooklyn.entity.basic.Entities;
import brooklyn.entity.container.docker.DockerHost;
import brooklyn.entity.container.sdn.SdnAgent;
import brooklyn.entity.container.sdn.SdnProvider;
import brooklyn.entity.container.sdn.SdnProviderImpl;
import brooklyn.entity.proxying.EntitySpec;
import brooklyn.location.basic.SshMachineLocation;
import brooklyn.util.collections.MutableList;
import brooklyn.util.net.Cidr;

import com.google.common.collect.ImmutableList;

public class DoveNetworkImpl extends SdnProviderImpl implements DoveNetwork {

    private static final Logger LOG = LoggerFactory.getLogger(SdnProvider.class);

    @Override
    public void init() {
        LOG.info("Starting Dove SDN VE network id {}", getId());
        super.init();

        EntitySpec<?> agentSpec = EntitySpec.create(getConfig(SdnProvider.SDN_AGENT_SPEC, EntitySpec.create(DoveAgent.class)))
                .configure(DoveAgent.SDN_PROVIDER, this);

        setAttribute(SdnProvider.SDN_AGENT_SPEC, agentSpec);
    }

    @Override
    public Collection<IpPermission> getIpPermissions() {
        Collection<IpPermission> permissions = MutableList.of();
        return permissions;
    }

    public void addHost(Entity item) {
        SshMachineLocation machine = ((DockerHost) item).getDynamicLocation().getMachine();
        EntitySpec<?> spec = EntitySpec.create(getAttribute(SDN_AGENT_SPEC))
                .configure(DoveAgent.DOCKER_HOST, (DockerHost) item);
        DoveAgent agent = (DoveAgent) getAgents().addChild(spec);
        Entities.manage(agent);
        getAgents().addMember(agent);
        agent.start(ImmutableList.of(machine));
        if (LOG.isDebugEnabled()) LOG.debug("{} added dove agent {}", this, agent);
    }

    public void removeHost(Entity item) {
        SdnAgent agent = item.getAttribute(SdnAgent.SDN_AGENT);
        if (agent == null) {
            LOG.warn("{} cannot find dove agent: {}", this, item);
            return;
        }
        agent.stop();
        getAgents().removeMember(agent);
        Entities.unmanage(agent);
        if (LOG.isDebugEnabled()) LOG.debug("{} removed dove agent {}", this, agent);
    }

    @Override
    public InetAddress getNextContainerAddress(String networkId) {
        synchronized (addressMutex) {
            Cidr cidr = getAttribute(NETWORKS).get(networkId);
            Map<String, Integer> allocations = getAttribute(NETWORK_ALLOCATIONS);
            Integer allocated = allocations.get(networkId);
            if (allocated == null) allocated = 1;
            InetAddress next = cidr.addressAtOffset(allocated + 1);
            allocations.put(networkId, allocated + 1);
            setAttribute(NETWORK_ALLOCATIONS, allocations);
            return next;
        }
    }

}
