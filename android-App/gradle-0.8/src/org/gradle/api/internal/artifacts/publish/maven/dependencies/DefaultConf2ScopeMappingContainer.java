/*
 * Copyright 2007-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.internal.artifacts.publish.maven.dependencies;

import org.gradle.api.InvalidUserDataException;
import org.gradle.api.artifacts.maven.Conf2ScopeMapping;
import org.gradle.api.artifacts.maven.Conf2ScopeMappingContainer;
import org.gradle.api.artifacts.Configuration;
import org.gradle.util.WrapUtil;

import java.util.*;

/**
 * @author Hans Dockter
 */
public class DefaultConf2ScopeMappingContainer implements Conf2ScopeMappingContainer {
    private Map<Configuration, Conf2ScopeMapping> mappings = new HashMap<Configuration, Conf2ScopeMapping>();

    private boolean skipUnmappedConfs = true;
                  
    public DefaultConf2ScopeMappingContainer() {
    }

    public DefaultConf2ScopeMappingContainer(Map<Configuration, Conf2ScopeMapping> mappings) {
        this.mappings.putAll(mappings);
    }

    public Conf2ScopeMapping getMapping(Configuration... configurations) {
        Conf2ScopeMapping mappingWithHighestPriority = getMappingWithHighestPriority(configurations);
        if (mappingWithHighestPriority == null) {
            return null;
        }
        return mappingWithHighestPriority;
    }

    private Conf2ScopeMapping getMappingWithHighestPriority(Configuration[] configurations) {
        Set<Conf2ScopeMapping> result = getMappingsWithHighestPriority(configurations);
        if (result.size() > 1) {
            throw new InvalidUserDataException("The configuration to scope mapping is not unique. The following configurations " +
                    "have the same priority: " + result);
        }
        return result.size() == 0 ? null : result.iterator().next();
    }

    private Set<Conf2ScopeMapping> getMappingsWithHighestPriority(Configuration[] configurations) {
        return findHighestPriorityMappingsForMappedConfigurations(configurations);
    }

    private Set<Conf2ScopeMapping> findHighestPriorityMappingsForMappedConfigurations(Configuration[] configurations) {
        Integer lastPriority = null;
        Set<Conf2ScopeMapping> result = new HashSet<Conf2ScopeMapping>();
        for (Conf2ScopeMapping conf2ScopeMapping : getMappingsForConfigurations(configurations)) {
            if (lastPriority != null && lastPriority.equals(conf2ScopeMapping.getPriority())) {
                result.add(conf2ScopeMapping);
            } else if (lastPriority == null || lastPriority < conf2ScopeMapping.getPriority()) {
                lastPriority = conf2ScopeMapping.getPriority();
                result = WrapUtil.toSet(conf2ScopeMapping);
            }
        }
        return result;
    }

    private List<Conf2ScopeMapping> getMappingsForConfigurations(Configuration[] configurations) {
        List<Conf2ScopeMapping> existingMappings = new ArrayList<Conf2ScopeMapping>();
        for (Configuration configuration : configurations) {
            if (mappings.get(configuration) != null) {
                existingMappings.add(mappings.get(configuration));
            } else {
                existingMappings.add(new Conf2ScopeMapping(null, configuration, null));
            }
        }
        return existingMappings;
    }

    public Conf2ScopeMappingContainer addMapping(int priority, Configuration configuration, String scope) {
        mappings.put(configuration, new Conf2ScopeMapping(priority, configuration, scope));
        return this;
    }

    public Map<Configuration, Conf2ScopeMapping> getMappings() {
        return mappings;
    }

    public boolean isSkipUnmappedConfs() {
        return skipUnmappedConfs;
    }

    public void setSkipUnmappedConfs(boolean skipUnmappedConfs) {
        this.skipUnmappedConfs = skipUnmappedConfs;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DefaultConf2ScopeMappingContainer that = (DefaultConf2ScopeMappingContainer) o;

        if (!mappings.equals(that.mappings)) return false;

        return true;
    }

    public int hashCode() {
        return mappings.hashCode();
    }
}
