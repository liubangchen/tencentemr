package com.tencent.cloud.emr;

import com.emc.ecs.nfsclient.nfs.Nfs;
import com.emc.ecs.nfsclient.nfs.NfsFsInfoRequest;
import com.emc.ecs.nfsclient.nfs.NfsMkdirRequest;
import com.emc.ecs.nfsclient.nfs.io.Nfs3File;
import com.emc.ecs.nfsclient.nfs.nfs3.Nfs3;
import com.emc.ecs.nfsclient.nfs.nfs3.Nfs3FsInfoResponse;
import com.emc.ecs.nfsclient.rpc.CredentialUnix;

import java.util.List;

/**
 * Created by liubangchen on 2018/3/26.
 */
public class NfsClient {


    public static void main(String[] args)throws Exception{
        Nfs3 nfs=new Nfs3("172.21.16.37:2049","/",new CredentialUnix(), 3);
        Nfs3File file =new Nfs3File(nfs,"/");
        List<String> f=file.list();
        for(String filename:f){
            System.out.println(filename);
        }
    }

}
