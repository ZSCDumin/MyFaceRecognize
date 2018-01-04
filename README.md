# README

人脸识别（Face Recognize），是一项提取人脸特征信息进行智能分析的生物识别技术，本项目是基于科大讯飞SDK为基础，增加的业务功能，识别率高达99.4%。

科大讯飞开放平台地址：[http://www.xfyun.cn](http://www.xfyun.cn/)

【主要功能】<br/>
- 【人脸模型查询和删除】
- 【声纹模型查询和删除】
- 【人脸、声纹模型注册】
- 【人脸、声纹识别认证】

【安全性问题】<br>
目前在人脸识别中，最重要的问题，可能是图片的可复制重复使用的问题。MSC  SDK（即讯飞SDK）在人脸识别时,并不是直接使用摄像头等硬件，只要应用传入二进制的图片数据即可。所以在验证时传入图片也能通过验证。在我们项目中对此安全问题提供了解决方案，强制用户必须使用摄像头，同时用户也必须做声纹验证，这样可以提供更高的安全性。

# 效果图
![查询界面](https://github.com/hjgang/MyFaceRecognize/blob/master/images/device-2018-01-04-131145.png)
![注册人脸模型](https://github.com/hjgang/MyFaceRecognize/blob/master/images/device-2018-01-04-131906.png)
![识别认证](https://github.com/hjgang/MyFaceRecognize/blob/master/images/device-2018-01-04-131057.png)

# License

```text
Copyright 2018 kingkong

出现问题可联系QQ：709872217（注明来自github）
```
