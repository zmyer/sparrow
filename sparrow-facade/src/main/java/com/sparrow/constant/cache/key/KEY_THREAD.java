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

package com.sparrow.constant.cache.key;

import com.sparrow.constant.SPARROW_MODULE;
import com.sparrow.constant.cache.KEY;

public class KEY_THREAD {
    /**
     * 上一次显示时间
     */
    public static final KEY.Business SHOWTIME = new KEY.Business(SPARROW_MODULE.BLOG, "THREAD", "SHOWTIME");

    /**
     * 显示的时间间隔
     */
    public static final KEY.Business SPLIT = new KEY.Business(SPARROW_MODULE.BLOG, "THREAD", "SPLIT");

    /**
     * 帖了的队列 <p> Order Set <p> id time
     */
    public static final KEY.Business QUEUE = new KEY.Business(SPARROW_MODULE.BLOG, "THREAD", "QUEUE", "SHOWTIME");
    /**
     * 帖子队列实体内容 <p> hash <p> id content
     */
    public static final KEY.Business QUEUE_ENTITY = new KEY.Business(SPARROW_MODULE.BLOG, "THREAD", "QUEUE", "ENTITY");
}