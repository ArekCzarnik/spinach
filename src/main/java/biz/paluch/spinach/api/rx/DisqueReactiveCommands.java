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
package biz.paluch.spinach.api.rx;

import rx.Observable;
import biz.paluch.spinach.api.DisqueConnection;

/**
 * Reactive commands for Disque. This API is thread-safe.
 * 
 * @param <K> Key type.
 * @param <V> Value type.
 * @author Mark Paluch
 */
public interface DisqueReactiveCommands<K, V> extends DisqueJobReactiveCommands<K, V>, DisqueQueueReactiveCommands<K, V>,
        DisqueServerReactiveCommands<K, V>, DisqueClusterReactiveCommands<K, V> {

    /**
     * Authenticate to the server.
     *
     * @param password the password
     * @return String simple-string-reply
     */
    Observable<String> auth(String password);

    /**
     * Close the connection. The connection will become not usable anymore as soon as this method was called.
     */
    void close();

    /**
     *
     * @return the underlying connection.
     */
    DisqueConnection<K, V> getConnection();

    /**
     * 
     * @return true if the connection is open (connected and not closed).
     */
    boolean isOpen();

    /**
     * Ping the server.
     *
     * @return simple-string-reply
     */
    Observable<String> ping();

    /**
     * Close the connection.
     * 
     * @return String simple-string-reply always OK.
     */
    Observable<String> quit();

}
