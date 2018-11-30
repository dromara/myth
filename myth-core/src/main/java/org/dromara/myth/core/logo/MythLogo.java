/*
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.dromara.myth.core.logo;

import org.dromara.myth.common.constant.CommonConstant;
import org.dromara.myth.common.utils.VersionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Myth logo.
 *
 * @author xiaoyu
 */
public class MythLogo {

    private static final String MYTH_LOGO = "\n" +
            " ____    ____           _   __       \n" +
            "|_   \\  /   _|         / |_[  |      \n" +
            "  |   \\/   |    _   __`| |-'| |--.   \n" +
            "  | |\\  /| |   [ \\ [  ]| |  | .-. |  \n" +
            " _| |_\\/_| |_   \\ '/ / | |, | | | |  \n" +
            "|_____||_____|[\\_:  /  \\__/[___]|__] \n" +
            "               \\__.'                 \n";

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MythLogo.class);

    public void logo() {
        String bannerText = buildBannerText();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(bannerText);
        } else {
            System.out.print(bannerText);
        }
    }

    private String buildBannerText() {
        return CommonConstant.LINE_SEPARATOR
                + CommonConstant.LINE_SEPARATOR
                + MYTH_LOGO
                + CommonConstant.LINE_SEPARATOR
                + " :: Myth :: (v" + VersionUtils.getVersion(getClass(), "1.0.0") + ")"
                + CommonConstant.LINE_SEPARATOR;
    }

}
