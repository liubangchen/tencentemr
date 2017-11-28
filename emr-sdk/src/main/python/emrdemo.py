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
action: 对应接口的接口名，请参考wiki文档上对应接口的接口名
'''
action = 'EmrDescribeCluster'

'''
config: 云API的公共参数
'''
config = {
    'Region': 'ap-shanghai',
    'secretId': 'abc',
    'secretKey': 'efg',
}

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