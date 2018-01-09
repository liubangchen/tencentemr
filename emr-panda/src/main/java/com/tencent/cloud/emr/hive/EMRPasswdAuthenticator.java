package com.tencent.cloud.emr.hive;

import org.apache.hadoop.conf.Configuration;

import javax.security.sasl.AuthenticationException;

import org.apache.hive.service.auth.PasswdAuthenticationProvider;

/**
 * Created by liubangchen on 2018/1/9.
 */
public class EMRPasswdAuthenticator implements PasswdAuthenticationProvider {

    private Configuration conf;

    public void Authenticate(String userName, String passwd)
            throws AuthenticationException {

    }

    public Configuration getConf() {
        if (this.conf == null) {
            this.conf = new Configuration();
        }

        return conf;
    }

    public void setConf(Configuration arg0) {
        this.conf = arg0;
    }
}
