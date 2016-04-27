package com.morpheus.gradle

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.GradleException
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.file.FileCollection
import com.morpheus.sdk.MorpheusClient;
import com.morpheus.sdk.BasicCredentialsProvider;
import com.morpheus.sdk.provisioning.*;
import com.morpheus.sdk.deployment.AppDeploy;
import com.morpheus.sdk.deployment.CreateDeployRequest;
import com.morpheus.sdk.deployment.RunDeployRequest;
import com.morpheus.sdk.deployment.RunDeployResponse;
import com.morpheus.sdk.deployment.UploadFileRequest;
import com.morpheus.sdk.deployment.CreateDeployResponse;
import org.gradle.api.tasks.util.PatternSet;

/*
 * Copyright 2014 original authors
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

/**
 * A Gradle task for uploading a build to morpheus for deployment
 *
 * @author David Estes
 */
@CompileStatic
class MorpheusDeploy extends DefaultTask {
    @Delegate MorpheusExtension morpheusExtension = new MorpheusExtension()


	@Input
    String getMorpheusUser() {
        morpheusExtension.morpheusUser
    }

    void setMorpheusUser(String morpheusUser) {
        morpheusExtension.morpheusUser = morpheusUser
    }


    @Input
    @Optional
    Map<String,String> getDeployConfiguration() {
        morpheusExtension.deployConfiguration
    }

    void setDeployConfiguration(Map<String,String> deployConfiguration) {
        morpheusExtension.deployConfiguration = deployConfiguration
    }

	@Input
    String getMorpheusPassword() {
        morpheusExtension.morpheusPassword
    }

    void setMorpheusPassword(String morpheusPassword) {
        morpheusExtension.morpheusPassword = morpheusPassword
    }

	@Input
    @Optional
    String getApplianceUrl() {
        morpheusExtension.applianceUrl
    }

    void setApplianceUrl(String applianceUrl) {
        morpheusExtension.applianceUrl = applianceUrl
    }

	@Input
    String getInstance() {
        morpheusExtension.instance
    }

    void setInstance(String instance) {
        morpheusExtension.instance = instance
    }

    @InputFiles
    FileTree getSource() {
    	FileTree src = null
    	morpheusExtension.resolvers.each { Resolver resolver ->
    		def resolverFile = project.file(resolver.resolverPath)
    		
    		if(resolverFile.exists() && resolverFile.directory) {
    			def pattern = new PatternSet()
    			if(resolver.includes != null) {
    				pattern.setIncludes(resolver.includes)
    			}
    			if(resolver.excludes != null) {
    				pattern.setExcludes(resolver.excludes)
    			}

    			FileTree currentTree = getProject().files(resolver.resolverPath).getAsFileTree().matching(pattern)
    			if(src) {
    				src += currentTree
    			} else {
    				src = currentTree
    			}
    		}
    	}
        return src
    }


    @TaskAction
    @CompileDynamic
    void deploy() {
    	BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider(morpheusUser,morpheusPassword);
    	MorpheusClient client = new MorpheusClient(credentialsProvider).setEndpointUrl(this.applianceUrl);
    	AppDeploy appDeploy = new AppDeploy();
        if(this.getDeployConfiguration()) {
            appDeploy.setConfigOptions(this.getDeployConfiguration().collectEntries{entry -> [entry.key,entry.value?.toString()]})
        }
    	ListInstancesResponse listInstancesResponse = client.listInstances(new ListInstancesRequest().name(this.getInstance()));
	    	if(listInstancesResponse.instances != null && listInstancesResponse.instances.size() > 0) {
	    		Long instanceId = listInstancesResponse.instances.get(0).id;
	    		CreateDeployResponse response = client.createDeployment(new CreateDeployRequest().appDeploy(appDeploy).instanceId(instanceId));
	    		Long appDeployId = response.appDeploy.id;
                appDeploy = response.appDeploy;
                
	    		// Time to find the files to upload
	    		morpheusExtension.resolvers.each { Resolver resolver ->
	    			def resolverFile = project.file(resolver.resolverPath)
	    			String destination = resolver.destinationPath ?: ''
                    def pattern = new PatternSet()
					if(resolverFile.exists() && resolverFile.directory) {
						
						if(resolver.includes != null) {
							pattern.setIncludes(resolver.includes)
						}
						if(resolver.excludes != null) {
							pattern.setExcludes(resolver.excludes)
						}
					}
					def rootURI = resolverFile.toURI()
	    			FileTree currentTree = getProject().files(resolver.resolverPath).getAsFileTree().matching(pattern)
	    			currentTree?.files?.each { file ->

	    				if(!file.isDirectory()) {
	    					if(destination) {
	    					destination += "/" + rootURI.relativize(file.getParentFile().toURI()).getPath()	
		    				} else {
		    					destination = rootURI.relativize(file.getParentFile().toURI()).getPath()	
		    				}
		    				UploadFileRequest fileUploadRequest = new UploadFileRequest().appDeployId(appDeployId).file(file).destination(destination);
                        	client.uploadDeploymentFile(fileUploadRequest);    
	    				}
	    			}
	    		}
	    		RunDeployResponse deployResponse = client.runDeploy(new RunDeployRequest().appDeploy(response.appDeploy));
	    	} else {
	    		throw new GradleException('Instance not found')
	    	}
    }

}