/**
 *
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
package com.nathaniel.app.servlet.support.util;

import com.nathaniel.app.core.util.ConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * spring-context配置类/配置文件加载工具
 *
 * @author nathaniel 2018-02-22
 */
public interface WebAppContextConfigLoader {
    Logger logger = LoggerFactory.getLogger(WebApplicationContext.class);
    String CONFIG_CLASS_FILE_DIR = "META-INF/app/app.configuration";
    String MAIN_CONFIG_CLASS_PARAM = "mainConfigClass";

    /**
     * 加载spring扩展配置组件
     *
     * @return
     */
    default List<Class<?>> loadConfigClass() {
        return ConfigurationLoader.loadConfigurationClass(CONFIG_CLASS_FILE_DIR).stream().filter(it -> {
            if (it.isAnnotationPresent(Configuration.class)) {
                return true;
            } else {
                logger.warn("{}没有被{}annotation注解,忽略此配置", it.getName(), Configuration.class.getSimpleName());
                return false;
            }
        }).collect(Collectors.toList());
    }

    /**
     * 注册配置组件类/设置配置文件到给定的annotationApplicationContext
     *
     * @param paramsWrapper
     * @param context
     */
    default void registerConfigClass(WebConfigParamsWrapper paramsWrapper, AnnotationConfigWebApplicationContext context) {
        Optional.ofNullable(paramsWrapper.getConfigParam(MAIN_CONFIG_CLASS_PARAM)).ifPresent(mainConfigClassName -> {
            try {
                Class<?> mainConfigClass = ClassUtils.forName(mainConfigClassName, WebAppContextConfigLoader.class.getClassLoader());
                logger.info("注册app自定义配置类:{}", mainConfigClass.getName());
                context.register(mainConfigClass);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });

        loadConfigClass().forEach(configClass -> {
            logger.info("注册annotation配置类:{}", configClass.getName());
            context.register(configClass);
        });

    }
}
