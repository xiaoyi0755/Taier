/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.dtstack.engine.sql.flink.table.side;

import com.dtstack.engine.sql.flink.ECacheType;
import com.dtstack.engine.sql.flink.classloader.ClassLoaderManager;
import com.dtstack.engine.sql.flink.table.AbstractSideTableParser;
import com.dtstack.engine.sql.flink.table.AbstractTableParser;
import com.dtstack.engine.sql.flink.util.PluginUtil;

/**
 * get specify side parser
 * Date: 2018/7/25
 * Company: www.dtstack.com
 *
 * @author xuchao
 */

public class StreamSideFactory {

    private static final String CURR_TYPE = "side";

    public static AbstractTableParser getSqlParser(String pluginType, String sqlRootDir, String cacheType, String pluginLoadMode) throws Exception {

        String sideOperator = ECacheType.ALL.name().equalsIgnoreCase(cacheType) ? "all" : "async";
        String pluginJarPath = PluginUtil.getSideJarFileDirPath(pluginType, sideOperator, "side", sqlRootDir, pluginLoadMode);
        String className = PluginUtil.getSqlParserClassName(pluginType, CURR_TYPE);

        return ClassLoaderManager.newInstance(pluginJarPath, (cl) -> {
            Class<?> sideParser = cl.loadClass(className);
            if (!AbstractSideTableParser.class.isAssignableFrom(sideParser)) {
                throw new RuntimeException("class " + sideParser.getName() + " not subClass of AbsSideTableParser");
            }
            return sideParser.asSubclass(AbstractTableParser.class).newInstance();
        });
    }
}
