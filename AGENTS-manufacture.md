# 讯方 manufacture 模块开发经验

## 项目概况
- 若依（Ruoyi）微服务架构：Spring Boot 2.7.18 + Spring Cloud 2021.0.9 + Nacos
- Java 11 + Maven 3.9.9（安装在 `C:\Tools\apache-maven-3.9.9`）
- GroupId: `com.xunfang`, Version: `3.6.6`

## 创建的文件清单（14个）

### 模块配置
- `xunfang-modules/xunfang-manufacture/pom.xml` — 依赖 common-core/redis/swagger/httpclient/fastjson(1.1.43)/org.json(20090211)
- `xunfang-modules/pom.xml` — 已添加 `<module>xunfang-manufacture</module>`
- 注意：parent pom 只管理 fastjson2(2.0.57)，fastjson v1 需在子 pom 显式声明版本

### 启动类 + 配置
- `.../manufacture/XunFangManufactureApplication.java` — `@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)`
- `.../resources/bootstrap.yml` — server.port=9205，Nacos 127.0.0.1:8848，Redis localhost:6379/pass=123456

### 工具类 (`com.xunfang.manufacture.util`)
- `DMEUtil.java` — iDME 工具。关键常量：
  - `projectUrl` = `http://642f01d8-...huaweicloud-idme.com/rdm_9fc851035ebc468fb3e71455d6664f24_app/services`
  - `apiExecute` = `/dynamic/api/`
  - `executeApiUrl` = `apiExecute`（规约别名）
  - 凭据：`gzlg017` / `Hngy@123456` / `sziit2024`
  - Token 通过 Redis 缓存（key: `gzlg017sziit2024_dmetoken`），有效期 1h
- `RequestUtil.java` — 静态 HTTP 方法：`requestsPost(url, body, token)`
- `TokenAndProject.java` — `getToken()`, `getProjectId()`
- `RedisCache1.java` — `@Component`，依赖 `RedisTemplate`

### 领域模型
- `XfSupplier.java` — extends BaseEntity。字段：id/supplierCode/supplierName/linkMan/linkPhone/linkEmail/supplierType/address/scopeOfSupply/cooperativeStatus/createTime/updateTime
- `XfPurchaseOrder.java` — extends BaseEntity。额外查询辅助字段：purchaseDateStart/purchaseDateEnd

### 服务层
- `IXfSupplierService.java` / `IXfPurchaseOrderService.java` — 各6个方法接口
- `XfSupplierServiceImpl.java` — iDME实体名 `Xfsupplier17`（注意小写s）
- `XfPurchaseOrderServiceImpl.java` — iDME实体名 `XfPurchaseOrder17`

### 控制器
- `XfSupplierController.java` — `@RequestMapping("/manufacture/supplier")`
- `XfPurchaseOrderController.java` — `@RequestMapping("/manufacture/order")`

## 关键教训

### 1. iDME 实体名必须与线上一致
- ❌ 原按文档用 `XfSupplier01` / `XfPurchaseOrder01`
- ✅ 实际线上是 `Xfsupplier17` / `XfPurchaseOrder17`（小写s，数字17）

### 2. iDME URL 路径
- `projectUrl`（到 `/services`）+ `apiExecute`（`/dynamic/api/`）+ `实体名/操作`
- apiExecute 末尾有 `/`，实体路径不要前导 `/`（否则产生 `//`）
- 正确：`/dynamic/api/Xfsupplier17/get`
- 错误：`/dynamic/api//Xfsupplier17/get`

### 3. iDME 凭据权限
- `comp10100/indus@10000/SZIIT2024` 无权访问 `rdm_9fc...` 应用
- ✅ 使用 `gzlg017/Hngy@123456/sziit2024`

### 4. fastjson 版本陷阱
- 项目依赖 fastjson2(2.0.57)，但规约代码用 `com.alibaba.fastjson.JSONObject`（v1 API）
- common-core 已依赖 fastjson 1.1.43，manufacture 模块通过传递依赖可用
- ⚠️ fastjson 1.1.43 没有 `JSONArray.toJavaList(Class)` 方法
- ✅ 替代方案：`JSONObject.parseArray(JSONObject.toJSONString(dataArr), Xxx.class)`

### 5. `result` 为 null 导致 NPE
- iDME 返回的 `result` 可能为 null（如权限错误时）
- ❌ `"SUCCESS".equals(result.toString())` → NPE
- ✅ `result != null && "SUCCESS".equals(result.toString())`

### 6. Spring Boot 启动
- 必须排除 `DataSourceAutoConfiguration`（manufacture 模块无数据库）
- 需要 `bootstrap.yml` 配置 Nacos + Redis
- 启动类需要 `@EnableCustomConfig` + `@EnableRyFeignClients`

### 7. Maven 打包
- 安装到了 `C:\Tools\apache-maven-3.9.9`
- 打包命令：`mvn package -pl xunfang-modules/xunfang-manufacture -am -DskipTests`
- 运行前杀进程释放 JAR：`taskkill /F /IM java.exe`

### 8. PowerShell 注意事项
- PowerShell 中 `-D` JVM 参数会被错误解析
- ✅ 使用 `cmd /c "java -Dfile.encoding=utf-8 -jar ..."` 包装

## API 端点（端口 9205）

| 方法 | 供应商 | 采购订单 |
|------|--------|----------|
| POST | `/manufacture/supplier` | `/manufacture/order` |
| GET list | `/manufacture/supplier/list?pageNum=&pageSize=` | `/manufacture/order/list?pageNum=&pageSize=` |
| GET {id} | `/manufacture/supplier/{id}` | `/manufacture/order/{id}` |
| PUT | `/manufacture/supplier` | `/manufacture/order` |
| DELETE single | `/manufacture/supplier/delete/{id}` | `/manufacture/order/delete/{id}` |
| DELETE batch | `/manufacture/supplier/{id1,id2}` | `/manufacture/order/{id1,id2}` |

## 辅助脚本
- `post测试/get_app_token.py` — 获取应用 JWT Token（需手动输入验证码）
- `post测试/test_manufacture_api.py` — 全量 API 调测（交互式菜单 / 一键全流程）

## 基础设施
- Nacos: `http://127.0.0.1:8848/nacos/`
- Redis: `localhost:6379`，密码 `123456`
- Gateway: 端口 8080，路由通过 Nacos 自动发现
- 服务注册名：`xunfang-manufacture`（与 spring.application.name 一致）
