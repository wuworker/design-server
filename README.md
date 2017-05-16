## 程序设计说明
毕业设计实现的功能为：使用手机进行灯光的PWM调节，开关，以及定时功能，通过服务端进行数据转发，在任意时间地点都能进行远程灯的实时监控。  

这是毕业设计的服务端程序，主要用于将app和单片机的数据进行转发。  

单片机程序代码地址：https://github.com/wuworker/design-mcu  

app代码地址：https://github.com/wuworker/design-app  


## 通信协议

### 1、数据格式

**一条数据 最少 = 14 字节，最大=30字节**

|  数据目的 | 数据来源 | 命令 | 数据      | 结束  |
|----------|---------|------|----------|------|
|  target | origin   | cmd  |  data     | end |
|	48位  |  48位    |  8位 | 0 位 ~ 128位 |  8位 |
```
结束符为0x0a,也就是'\n'
```

### 2、命令

#### 全部命令
|  命令  |   格式  |  说明  |
|--------|--------|-------|
| OK     |  0x11  | 正确响应       |
| STATUS |  0x12  | 获取设备状态|
| IS_APP |  0x21  | 说明是app注册     |
| IS_MCU |  0x22  | 说明是单片机注册   |
| ONLINE |  0x31  | 单片机在线        |
| ADD_LED  | 0x41 | 设置管理设备    |
| UPING    |  0x51 | 上线通知        |
| DOWNING  |  0x52 | 下线通知      |
| ON     | 0x61 | 开灯            |
| OFF    |  0x62 | 关灯          |
| TIME_ON | 0x63 | 定时开        |
|TIME_OFF | 0x64 | 定时关        |
| TIME_CLR | 0x65 | 取消定时     |
|TIME_OVER | 0x66 | 定时任务完成 |
