# 91160-cli

## 申明

- 本项目仅供学习研究，禁止商用！

## 如何使用

1. [下载jar包](https://github.com/pengpan/91160-cli/releases)
2. 初始化配置`java -jar 91160-cli.jar init`
3. 开始挂号`java -jar 91160-cli.jar register -c config.properties`

## 挂号日志

```text
$ java -jar 91160-cli.jar register -c config.properties
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