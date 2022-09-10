# 91160-cli

![](https://github.com/pengpan/91160-cli/workflows/Java%20CI%20with%20Maven/badge.svg)

## 申明

- 本项目仅供学习研究，禁止商用！

## 功能

- [x] 可指定医生
- [x] 可指定就诊人
- [x] 可指定挂号时间
- [x] 定时挂号
- [x] 代理刷号
- [x] 自定义刷号起始日期
- [x] 刷号失败重试

## 如何使用

1. 搭建Java运行环境，最低版本支持1.8，已有请跳过
- [Java下载](https://www.java.com/zh-CN/download)
- [1分钟设置Java环境变量](https://www.bilibili.com/video/BV1vy4y127mL)

2. 下载`91160-cli-{version}.zip`并解压
- [下载地址](https://github.com/pengpan/91160-cli/releases)

3. 初始化配置（仅需运行一次，运行结束后将生成配置文件`config.properties`，再次运行将覆盖配置）
```shell
$ java -jar 91160-cli.jar init
```

4. 开始挂号
```shell
$ java -jar 91160-cli.jar register -c config.properties
```

## 设置刷号休眠时间

操作：编辑配置文件`config.properties`，加入`sleepTime`，默认`5000ms`，可适当降低

```properties
# 刷号休眠时间[单位:毫秒]
sleepTime=5000
```

## 设置刷号起始日期

操作：编辑配置文件`config.properties`，加入`brushStartDate`，默认为空，格式`yyyy-MM-dd`

```properties
# 刷号起始日期(表示刷该日期后一周的号,为空取当前日期)[格式: 2022-06-01]
brushStartDate=
```

## 设置定时挂号

- 场景：今天6月1号，得知张医生下午3点系统会自动放号，现在还没到时间，我想到了这个时间立马开抢，但我又怕到时候忘记
- 操作：编辑配置文件`config.properties`，加入`enableAppoint=true`，加入`appointTime=2022-06-01 15:00:00`，运行挂号程序

```properties
# 是否开启定时挂号[true/false]
enableAppoint=false
# 定时挂号时间[格式: 2022-06-01 15:00:00]
appointTime=
```

## 设置刷号模式

- 场景：在官网上，科室排班页没号，医生详情页却有号，明明有号，程序却还在空刷，什么鬼
- 分析：最开始，有且只有科室排班页有刷号接口（通道1），程序也一直在用，后面医生详情页也提供了刷号接口（通道2），程序没做支持，再后面程序升级，同时支持两个刷号接口
- 操作：编辑配置文件`config.properties`，加入`brushChannel=`，默认为空表示支持 通道1+通道2 轮询刷号，也可单独这是其中一种

```properties
# 刷号通道[CHANNEL_1(通道1)/CHANNEL_2(通道2)]
brushChannel=
```

## 使用代理

1. 新建`proxy.txt`文件
2. 写入代理信息，格式: `(http|socks)@ip:port`，每行一条

```text
http@127.0.0.1:1087
socks@127.0.0.1:1086
```

3. 编辑配置文件`config.properties`，开启并配置代理文件路径

```properties
# 是否开启代理[true/false]
enableProxy=true
# 代理文件路径[格式: /dir/proxy.txt]
proxyFilePath=[代理文件路径]
# 获取代理方式[ROUND_ROBIN(轮询)/RANDOM(随机)]
proxyMode=ROUND_ROBIN
```

4. 当开启代理后，在循环刷号时会从代理文件中随机选取一条代理，并通过该代理发起请求

## Q&A

### Windows终端中文乱码如何解决？

1. 打开终端，先运行命令：`chcp 65001`，设置编码为`UTF-8`；
2. 执行jar时加入`-Dfile.encoding=utf-8`，即`java -Dfile.encoding=utf-8 -jar 91160-cli.jar init|register`；
3. 终端不要用`powershell`，要用`cmd.exe`；

## 加入群聊

加群备注`91160`，作者会在第一时间拉你进群

![微信交流群](imgs/wechat_group_final.png)

## 赞赏作者

如果您觉得`91160-cli`对你有帮助，可以请作者喝杯咖啡哦～

![赞赏码](imgs/reward.png)

## More

如果有好的想法和建议，请联系作者
