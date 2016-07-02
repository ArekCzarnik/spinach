/*
 * Copyright 2016 the original author or authors.
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
package biz.paluch.spinach.support;

import java.util.concurrent.TimeUnit;

import com.lambdaworks.redis.resource.ClientResources;
import com.lambdaworks.redis.resource.DefaultClientResources;

/**
 * Client-Resources suitable for testing. Uses {@link TestEventLoopGroupProvider} to preserve the event
 * loop groups between tests. Every time a new {@link TestClientResources} instance is created, shutdown hook is added
 * {@link Runtime#addShutdownHook(Thread)}.
 * 
 * @author Mark Paluch
 */
public class TestClientResources {

    public static ClientResources create() {
        final DefaultClientResources resources = new DefaultClientResources.Builder().eventLoopGroupProvider(
                new TestEventLoopGroupProvider()).build();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    resources.shutdown(100, 100, TimeUnit.MILLISECONDS).get(10, TimeUnit.SECONDS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return resources;
    }
}
