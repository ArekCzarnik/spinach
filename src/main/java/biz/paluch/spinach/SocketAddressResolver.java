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

package biz.paluch.spinach;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import com.lambdaworks.redis.ConnectionPoint;
import com.lambdaworks.redis.resource.DnsResolver;

/**
 * Resolves a {@link com.lambdaworks.redis.RedisURI} to a {@link java.net.SocketAddress}.
 * 
 * @author Mark Paluch
 */
class SocketAddressResolver {

    /**
     * Resolves a {@link ConnectionPoint} to a {@link java.net.SocketAddress}.
     * 
     * @param inetSocketAddress must not be {@literal null}
     * @param dnsResolver must not be {@literal null}
     * @return the resolved {@link SocketAddress}
     */
    public static SocketAddress resolve(InetSocketAddress inetSocketAddress, DnsResolver dnsResolver) {

        try {
            InetAddress inetAddress = dnsResolver.resolve(inetSocketAddress.getHostString())[0];
            return new InetSocketAddress(inetAddress, inetSocketAddress.getPort());
        } catch (UnknownHostException e) {
            return new InetSocketAddress(inetSocketAddress.getHostString(), inetSocketAddress.getPort());
        }

    }
}