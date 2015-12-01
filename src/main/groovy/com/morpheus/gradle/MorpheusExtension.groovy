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

/**
* Allows configuration of the Gradle Morpheus Continuous Build Plugin
*
* @author David Estes
*/
class MorpheusExtension {
	String applianceUrl="https://v2.gomorpheus.com"
	String morpheusUser
	String morpheusPassword
	String instance
	Map<String,String> deployConfiguration

	List<Resolver> resolvers = []

	void from(String resolverPath) {
		resolvers += new Resolver(resolverPath: resolverPath)
	}

	void from(Closure resolverConfig) {
		def resolver = new Resolver()
		resolverConfig.delegate = resolver
		resolverConfig.call()
		resolvers += resolver
	}
}