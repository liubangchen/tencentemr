package com.tencent.cloud.emr.hive;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.authentication.client.AuthenticationException;
import org.apache.hive.service.auth.PasswdAuthenticationProvider;

/**
 * Created by liubangchen on 2018/1/9.
 */
public class EMRPasswdAuthenticator extends PasswdAuthenticationProvider {

    private Configuration conf;

    @Override
    public void Authenticate(String userName, String passwd)
            throws AuthenticationException {

    }

    @Override
    public Configuration getConf() {
        if (this.conf == null) {
            this.conf = new Configuration();
        }

        return conf;
    }

    @Override
    public void setConf(Configuration arg0) {
        this.conf = arg0;
    }
}
