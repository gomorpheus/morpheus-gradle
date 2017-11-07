/*
* Copyright 2014 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.morpheus.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.bundling.Jar
/**
 * This is the Gradle Plugin implementation of Morpheus continuous build. It facilities deployment to Morpheus Instances.
 *
 * task: morpheusDeploy
 *
 * @author David Estes
*/
class MorpheusPlugin implements Plugin<Project> {
	void apply(Project project) {
	    def defaultConfiguration = project.extensions.create('morpheus', MorpheusExtension)

	    project.tasks.create('morpheusDeploy', MorpheusDeploy)

        def morpheusTask = project.tasks.getByName('morpheusDeploy')

        project.afterEvaluate {
        	def morpheusConfig = project.extensions.getByType(MorpheusExtension)

        	morpheusTask.configure {
        		resolvers = morpheusConfig.resolvers
        		morpheusUser = morpheusConfig.morpheusUser
        		morpheusPassword = morpheusConfig.morpheusPassword
        		applianceUrl = morpheusConfig.applianceUrl
        		instance = morpheusConfig.instance
                deployConfiguration = morpheusConfig.deployConfiguration
                deploymentName = morpheusConfig.deploymentName ?: project.name
                deploymentVersion = project.version
        	}
    	}

	}
}