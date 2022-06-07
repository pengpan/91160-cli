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

## 使用

1. 搭建Java运行环境，最低版本支持1.8

2. 下载`91160-cli.jar`，[下载地址](https://github.com/pengpan/91160-cli/releases)

3. 初始化配置

```shell
$ java -jar 91160-cli.jar init
```

4. 查看配置

```properties
# 91160账号
userName=[your userName]
# 91160密码
password=[your password]
# 就诊人编号
memberId=12345678
# 城市编号
cityId=5
# 医院编号
unitId=21
# 大科室编号
bigDeptId=1
# 小科室编号
deptId=556
# 医生编号
doctorId=1690
# 需要周几的号[可多选，如(6,7)]
weeks=6,7
# 时间段编号[可多选，如(am,pm)]
days=am,pm
# 刷号休眠时间[单位:秒]
sleepTime=5
# 刷号起始日期(表示刷该日期后一周的号,为空取当前日期)[格式: 2022-06-01]
brushStartDate=
# 是否开启定时挂号[true/false]
enableAppoint=false
# 定时挂号时间[格式: 2022-06-01 15:00:00]
appointTime=
# 是否开启代理[true/false]
enableProxy=false
# 代理文件路径[格式: /dir/proxy.txt]
proxyFilePath=proxy.txt
# 获取代理方式[ROUND_ROBIN(轮询)/RANDOM(随机)]
proxyMode=ROUND_ROBIN
```

6. 开始挂号

```shell
$ java -jar 91160-cli.jar register -c config.properties
```

5. 查看日志

```text
2022-05-26 00:22:12.152  INFO - --> GET https://user.91160.com/login.html
2022-05-26 00:22:12.488  INFO - <-- 200 OK https://user.91160.com/login.html (332ms, unknown-length body)
2022-05-26 00:22:12.549  INFO - --> POST https://user.91160.com/login.html (467-byte body)
2022-05-26 00:22:13.243  INFO - <-- 302 Found https://user.91160.com/login.html (692ms, unknown-length body)
2022-05-26 00:22:13.244  INFO - --> GET https://www.91160.com/client/login.html
2022-05-26 00:22:15.087  INFO - <-- 302 https://www.91160.com/client/login.html (1842ms, unknown-length body)
2022-05-26 00:22:15.088  INFO - 挂号开始
2022-05-26 00:22:15.096  INFO - $.sch.1699.am.4
2022-05-26 00:22:15.096  INFO - $.sch.1699.am.5
2022-05-26 00:22:15.097  INFO - $.sch.1699.am.6
2022-05-26 00:22:15.097  INFO - $.sch.1699.am.0
2022-05-26 00:22:15.097  INFO - $.sch.1699.pm.4
2022-05-26 00:22:15.097  INFO - $.sch.1699.pm.5
2022-05-26 00:22:15.097  INFO - $.sch.1699.pm.6
2022-05-26 00:22:15.097  INFO - $.sch.1699.pm.0
2022-05-26 00:22:15.098  INFO - 努力刷号中...
2022-05-26 00:22:15.255  INFO - --> GET https://gate.91160.com/guahao/v1/pc/sch/dep
2022-05-26 00:22:16.006  INFO - <-- 200 https://gate.91160.com/guahao/v1/pc/sch/dep (750ms, unknown-length body)
2022-05-26 00:22:16.089  INFO - {"left_num":1,"schedule_id":"628c7442687bcb522d0d4fcd","time_type":"am"}
2022-05-26 00:22:16.090  INFO - --> GET https://user.91160.com/member.html
2022-05-26 00:22:16.620  INFO - <-- 200 OK https://user.91160.com/member.html (529ms, unknown-length body)
2022-05-26 00:22:16.652  INFO - --> GET https://www.91160.com/guahao/ystep1/uid-21/depid-556/schid-628c7442687bcb522d0d4fcd.html
2022-05-26 00:22:17.545  INFO - <-- 200 https://www.91160.com/guahao/ystep1/uid-21/depid-556/schid-628c7442687bcb522d0d4fcd.html (892ms, unknown-length body)
2022-05-26 00:22:17.630  INFO - --> POST https://www.91160.com/guahao/ysubmit.html (1534-byte body)
2022-05-26 00:22:19.385  INFO - <-- 302 https://www.91160.com/guahao/ysubmit.html (1755ms, unknown-length body)
2022-05-26 00:22:19.387  INFO - --> GET https://www.91160.com/guahao/ysuccess/yuyue_id-436015454.html
2022-05-26 00:22:20.615  INFO - <-- 200 https://www.91160.com/guahao/ysuccess/yuyue_id-436015454.html (1228ms, unknown-length body)
2022-05-26 00:22:20.620  INFO - 挂号成功
2022-05-26 00:22:20.620  INFO - 挂号结束
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

## 请作者喝咖啡

![image][wechat_pay]

[wechat_pay]:data:image/png;base64,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
