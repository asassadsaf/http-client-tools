package com.fkp.tools.okhttp3.util;

import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author fkp
 * @version 1.0
 * @description
 * @date 2024/6/15 23:34
 */
@Component
public class OkHttpUtils {

    @Autowired
    private OkHttpClient okHttpClient;


}
