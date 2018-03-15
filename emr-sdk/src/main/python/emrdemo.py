#!/usr/bin/python
# -*- coding: utf-8 -*-

# 引入云API入口模块
from QcloudApi.qcloudapi import QcloudApi
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

'''
action = 'EmrDescribeCluster'

'''
config: 云API的公共参数
'''
config = {
    'Region': 'ap-beijing',
    'secretId': '123',
    'secretKey': '123',
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

try:
    service = EMRQcloudApi(module, config)
    print(service.generateUrl(action, action_params))
    print(service.call(action, action_params))
except Exception as e:
    import traceback

    print('traceback.format_exc():\n%s' % traceback.format_exc())
