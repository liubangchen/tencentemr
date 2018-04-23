#!/usr/bin/python
# -*- coding: utf-8 -*-

# 引入云API入口模块
from QcloudApi.qcloudapi import QcloudApi
import json
from emr import Emr


class EMRQcloudApi(QcloudApi):
    def _factory(self, module, config):
        return Emr(config)


'''
安装
$ pip install qcloudapi-sdk-python
$ git clone https://github.com/QcloudApi/qcloudapi-sdk-python
$ cd qcloudapi-sdk-python
$ python setup.py install
'''

module = 'emr'

'''
参数说明详见
https://cloud.tencent.com/document/api/589/10216
action: 对应接口的接口名，请参考wiki文档上对应接口的接口名

EmrDescribeCluster 查询集群列表
EmrDescribeClusterNode 查询集群节点信息
EmrScaleoutCluster 扩容接口
EmrDestroyTaskNode 缩容接口
UpdateServiceParamsForMC 参数更新接口

'''
action = 'EmrDescribeCluster'
clusterId = 'emr-xxxxxxxx'
appId = '1234567890'
operateUin = '1223333333'
ownerUin = '122222222'

'''
config: 云API的公共参数
'''
config = {
    'Region': 'gz',
    'secretId': 'secret_id',
    'secretKey': 'secret_key',
}
'''
查询集群信息参数
{
 'ClusterId':'emr-123456',
 'NodeFlag':'all'
}

扩容参数
{
   'ClusterId':'emr-123456',
   'CoreNodes':0,
   'TaskNodes':1,
   'NodeChargeType':0
}

缩容
{
  'ClusterId':'emr-123456',
  'IPs.0':'192.168.0.1'
}
'''
# 接口参数
action_params = {
}

service = EMRQcloudApi(module, config)

def sendRequest(action, action_params):
    try:
        print(service.generateUrl(action, action_params))
        print(service.call(action, action_params))
    except Exception as e:
        import traceback
        print('traceback.format_exc():\n%s' % traceback.format_exc())

def restartService():
    print('restart cluster service')
    action_params = {
        'clusterId': clusterId,
        'appId': appId,
        'serviceGroup': 1,
        'serviceType': 1,
        'operateUin': operateUin,
        'ownerUin': ownerUin,
        'Region':'gz'
    }
    sendRequest('RestartServiceForMC', action_params)

def scaloutClusterAndRunScripts():
    print('scalout cluster and run sripts')
    scriptItem1 = {
        'FilePath':'frank/test1.sh',
        'Region':'ap-guangzhou',
        'Bucket':'huanan',
        'Args':["12", "13", "15"]
    }
    scriptItem2 = {
        'FilePath':'frank/test2.py',
        'Region':'ap-guangzhou',
        'Bucket':'huanan',
        'Args':["12","16"]
    }
    cosInfo = {
        'secretId':'secret_id',
        'secretKey':'secret_key'
    }
    action_params = {
        'ClusterId': clusterId,
        'CoreNodes':0,
        'TaskNodes':1,
        'NodeChargeType':0,
        'Region':'gz',
        'Files.0':json.dumps(scriptItem1),
        'Files.1':json.dumps(scriptItem2),
        'CosInfo':json.dumps(cosInfo)
    }
    sendRequest('EmrScaleoutCluster', action_params)
   
def describeCluster():
    print('restart cluster service')
    action_params = {
        'ClusterId': clusterId,
        'NodeFlag':'all'
    }
    sendRequest('EmrDescribeCluster', action_params) 

def updateParams():
    print('update cluster params')
    config = {
        'dfs.datanode.handler.count':'112',
        'foo-add-key':'foo-value'
    }

    configStr = json.dumps(config)
    action_params = {
         'clusterId': clusterId,
         'appId': appId,
         'serviceType':1,
         'log':'test',
         'remark':'foo test',
         'operateUin': operateUin,
         'ownerUin': ownerUin,
         'op':'update',
         'Region':'gz',
         'paramsArray.0':configStr,
         'fileNameArray.0':'hdfs-site.xml'
    }
    sendRequest('UpdateServiceParamsForMC', action_params)

def main():
   #updateParams()
   #describeCluster()
   #restartService()
   scaloutClusterAndRunScripts()

if __name__ == "__main__":
    main()
