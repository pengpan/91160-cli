# 91160-cli

![](https://github.com/pengpan/91160-cli/workflows/Java%20CI%20with%20Maven/badge.svg)

## 申明

- 本项目仅供学习研究，禁止商用！

## 功能

- [x] 可指定医生
- [x] 可指定就诊人
- [x] 可指定挂号时间
- [x] 定时挂号
- [ ] IP代理刷号

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
weeks=7
# 时间段编号[可多选，如(am,pm)]
days=am
# 刷号休眠时间[单位:秒]
sleepTime=15
# 是否开启定时挂号[true/false]
enableAppoint=false
# 定时挂号时间[格式: 2022-06-01 15:00:00]
appointTime=
# 是否开启多线程挂号(仅在定时挂号开启时生效)[true/false]
enableMultithreading=false
# 是否开启代理[true/false]
enableProxy=false
# 获取代理URL(可参考https://github.com/jhao104/proxy_pool搭建代理池)[格式: http://127.0.0.1:5010/get]
getProxyURL=
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